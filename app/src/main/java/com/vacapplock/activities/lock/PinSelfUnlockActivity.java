package com.vacapplock.activities.lock;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.vacapplock.R;
import com.vacapplock.activities.main.MainActivity;
import com.vacapplock.activities.setting.LockSettingActivity;
import com.vacapplock.api.ApiService;
import com.vacapplock.api.login.PinApi;
import com.vacapplock.base.AppConstants;
import com.vacapplock.db.CommLockInfoManager;
import com.vacapplock.model.AppVersion;
import com.vacapplock.utils.LogUtil;

import android.Manifest;

import java.io.File;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PinSelfUnlockActivity extends AppCompatActivity implements PinApi.OnPinVerificationListener {

    private static final int REQUEST_PERMISSION_CODE = 10;
    private TextView passcodeDot1, passcodeDot2, passcodeDot3, passcodeDot4;
    private StringBuilder currentPin = new StringBuilder();
    private String correctPin = AppConstants.PASSWORD_UNLOCK;
    private CommLockInfoManager mManager;
    private String actionFrom;
    private String pkgName;
    private String downloadUrl, releaseNotes;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_self_unlock);

        mManager = new CommLockInfoManager(this);
        pkgName = getIntent().getStringExtra(AppConstants.LOCK_PACKAGE_NAME);
        actionFrom = getIntent().getStringExtra(AppConstants.LOCK_FROM);

        if (pkgName == null) {
            pkgName = AppConstants.APP_PACKAGE_NAME;
        }
        if (actionFrom == null) {
            actionFrom = AppConstants.LOCK_FROM_LOCK_MAIN_ACTIVITY;
        }
        passcodeDot1 = findViewById(R.id.passcodeDot1);
        passcodeDot2 = findViewById(R.id.passcodeDot2);
        passcodeDot3 = findViewById(R.id.passcodeDot3);
        passcodeDot4 = findViewById(R.id.passcodeDot4);

        setNumberClickListeners();

        String currentVersion = null;
        try {
            currentVersion = getPackageManager()
                    .getPackageInfo(getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }


        checkAppVersion(currentVersion);
    }

    private void setNumberClickListeners() {
        int[] numberIds = {
                R.id.number0, R.id.number1, R.id.number2, R.id.number3,
                R.id.number4, R.id.number5, R.id.number6,
                R.id.number7, R.id.number8, R.id.number9
        };

        for (int id : numberIds) {
            findViewById(id).setOnClickListener(this::onNumberClick);
        }

        findViewById(R.id.numberB).setOnClickListener(v -> onBackspaceClick());
    }

    private void onNumberClick(View view) {
        if (currentPin.length() < 4) {
            currentPin.append(((TextView) view).getText().toString());
            updatePinDots();
            if (currentPin.length() == 4) {
                verifyPin();
            }
        }
    }

    private void onBackspaceClick() {
        if (currentPin.length() > 0) {
            currentPin.deleteCharAt(currentPin.length() - 1);
            updatePinDots();
        }
    }

    private void updatePinDots() {
        passcodeDot1.setBackgroundResource(currentPin.length() > 0 ? R.drawable.passcode_dot_filled : R.drawable.passcode_dot_empty);
        passcodeDot2.setBackgroundResource(currentPin.length() > 1 ? R.drawable.passcode_dot_filled : R.drawable.passcode_dot_empty);
        passcodeDot3.setBackgroundResource(currentPin.length() > 2 ? R.drawable.passcode_dot_filled : R.drawable.passcode_dot_empty);
        passcodeDot4.setBackgroundResource(currentPin.length() > 3 ? R.drawable.passcode_dot_filled : R.drawable.passcode_dot_empty);
    }

    private void verifyPin() {
        PinApi pinApi = new PinApi(this);
        pinApi.verifyPin(currentPin.toString());
    }

//    private void verifyPin() {
//        if (currentPin.toString().equals(correctPin)) {
//            onPinCorrect();
//        } else {
//            onPinIncorrect();
//        }
//    }

    public void onPinCorrect() {
        if (actionFrom.equals(AppConstants.LOCK_FROM_LOCK_MAIN_ACTIVITY)) {
            Intent intent = new Intent(PinSelfUnlockActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } else if (actionFrom.equals(AppConstants.LOCK_FROM_FINISH)) {
            mManager.unlockCommApplication(pkgName);
            finish();
        } else if (actionFrom.equals(AppConstants.LOCK_FROM_SETTING)) {
            startActivity(new Intent(PinSelfUnlockActivity.this, LockSettingActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }

    public void onPinIncorrect() {
        Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
        currentPin.setLength(0);
        updatePinDots();
    }

    public void checkAppVersion(String currentVersion) {
        ApiService apiService = ApiService.apiService;

        Call<List<AppVersion>> call = apiService.checkVersion("VACAppLock");
        call.enqueue(new Callback<List<AppVersion>>() {
            @Override
            public void onResponse(Call<List<AppVersion>> call, Response<List<AppVersion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AppVersion> appVersions = response.body();
                    AppVersion latestVersion = appVersions.get(0);

                    if (!latestVersion.getVersion().equals(currentVersion)) {
                        showUpdateDialog(latestVersion);
                    }
                } else {
                    Log.e("TAG", "Request failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<AppVersion>> call, Throwable t) {
                Log.e("TAG", "Error occurred: ", t);
            }
        });
    }

    private void showUpdateDialog(AppVersion appVersion) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_update, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
        messageTextView.setText("A new version " + appVersion.getVersion() + " is available. Please update to continue.");

        Button updateButton = dialogView.findViewById(R.id.button_update);
        TextView textProgress = dialogView.findViewById(R.id.text_progress);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();


        updateButton.setOnClickListener(v -> {
            updateButton.setVisibility(View.GONE);
            textProgress.setVisibility(View.VISIBLE);
            checkPermissionDownload(appVersion.getUrl(), appVersion.getReleaseNotes());
        });
    }

    private void checkPermissionDownload(String url, String note) {
        downloadUrl = url;
        releaseNotes = note;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Trên Android 13 trở lên, yêu cầu quyền đọc phương tiện
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_DENIED) {

                String[] permissions = {
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO
                };
                requestPermissions(permissions, REQUEST_PERMISSION_CODE);
            } else {
                startDownload(downloadUrl);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Trên Android 6 trở lên, yêu cầu quyền ghi vào bộ nhớ ngoài (Android 6-12)
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission, REQUEST_PERMISSION_CODE);
            } else {
                startDownload(downloadUrl);
            }
        } else {
            // Android dưới 6, không cần yêu cầu quyền, tiếp tục tải xuống
            startDownload(downloadUrl);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                startDownload(downloadUrl);
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startDownload(String url) {
        LogUtil.i("Original URL: " + url);

        String encodedUrl = encodeUrl(url);
        LogUtil.i("Encoded URL: " + encodedUrl);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(encodedUrl));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setTitle(releaseNotes + ".apk");
        request.setDescription("Downloading file...");

        // Đặt tên tệp cụ thể
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, releaseNotes + ".apk");
        request.setMimeType("application/vnd.android.package-archive");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            long downloadId = downloadManager.enqueue(request);
            LogUtil.i("Download ID: " + downloadId);

            // Đăng ký BroadcastReceiver để tự động cài đặt APK sau khi tải xuống
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (id == downloadId) {
                        // Lấy đường dẫn của tệp tải xuống
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), releaseNotes + ".apk");

                        Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);

                        // Tạo Intent để cài đặt tệp APK
                        Intent installIntent = new Intent(Intent.ACTION_VIEW);
                        installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        // Khởi chạy trình cài đặt APK
                        startActivity(installIntent);

                    }
                }
            }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
    }

    private String encodeUrl(String url) {
        try {
            return url.replace("|", "%7C");
        } catch (Exception e) {
            Log.e("Encode Error", "Error encoding URL", e);
            return url;
        }
    }

}
