package com.vacapplock.activities.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.vacapplock.R;
import com.vacapplock.activities.lock.PinSelfUnlockActivity;
import com.vacapplock.base.AppConstants;
import com.vacapplock.base.BaseActivity;
import com.vacapplock.receiver.MyDeviceAdminReceiver;
import com.vacapplock.services.BackgroundManager;
import com.vacapplock.services.LoadAppListService;
import com.vacapplock.services.LockService;
import com.vacapplock.utils.AppUtils;
import com.vacapplock.utils.LockUtil;
import com.vacapplock.utils.SpUtil;
import com.vacapplock.utils.ToastUtil;
import com.vacapplock.widget.DialogPermission;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SplashActivity extends BaseActivity {
    private static final int RESULT_ACTION_USAGE_ACCESS_SETTINGS = 1;
    private static final int RESULT_ACTION_ACCESSIBILITY_SETTINGS = 3;
    private static final int REQUEST_CODE_ENABLE_ADMIN = 100;

    private ImageView mImgSplash, mImgLogo;

    private static final String VERSION_CHECK_URL = "http://10.50.4.21:8095/api/VersionAndroid/GetVersionAndroid?androidApp=VACAppLock";

    private OkHttpClient client = new OkHttpClient();

    @Nullable
    private ObjectAnimator animator;

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminComponent;

    @Override
    public int getLayoutId() {
        return R.layout.activity_splash;
    }


    @Override
    protected void initViews(Bundle savedInstanceState) {
        AppUtils.hideStatusBar(getWindow(), true);
        mImgSplash = findViewById(R.id.img_splash);
        mImgLogo = findViewById(R.id.image_logo);

//
    }

    @Override
    protected void initData() {
        // Khởi tạo DevicePolicyManager và ComponentName
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponent = new ComponentName(this, MyDeviceAdminReceiver.class);

        Glide.with(this)
                .load(R.drawable.vac_loading)
                        .into(mImgLogo);

        SpUtil.getInstance().putBoolean(AppConstants.IS_REQUESTING_PERMISSIONS, true);

        requestAdminPermission();
    }

    private void requestAdminPermission() {
        SharedPreferences prefs = getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        boolean adminPermissionRequested = prefs.getBoolean(AppConstants.ADMIN_PERMISSION_REQUESTED, false);

        if (!adminPermissionRequested) {
            if (!mDevicePolicyManager.isAdminActive(mAdminComponent)) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminComponent);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Yêu cầu cấp quyền quản trị viên để bảo vệ thiết bị của bạn.");
                startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
            } else {
                prefs.edit().putBoolean(AppConstants.ADMIN_PERMISSION_REQUESTED, true).apply();
                initAppLogic();
            }
        } else {
            initAppLogic();
        }
    }

    private void initAppLogic() {
        //startService(new Intent(this, LoadAppListService.class));
        BackgroundManager.getInstance().init(this).startService(LoadAppListService.class);

        //start lock services if  everything is already  setup
        if (SpUtil.getInstance().getBoolean(AppConstants.LOCK_STATE, false)) {
            BackgroundManager.getInstance().init(this).startService(LockService.class);
        }

        animator = ObjectAnimator.ofFloat(mImgSplash, "alpha", 0.5f, 1);
        animator.setDuration(1500);
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                boolean isFirstLock = SpUtil.getInstance().getBoolean(AppConstants.LOCK_IS_FIRST_LOCK, true);
                if (isFirstLock) {
                    showDialog();
                } else {
                    Intent intent = new Intent(SplashActivity.this, PinSelfUnlockActivity.class);
                    intent.putExtra(AppConstants.LOCK_PACKAGE_NAME, AppConstants.APP_PACKAGE_NAME);
                    intent.putExtra(AppConstants.LOCK_FROM, AppConstants.LOCK_FROM_LOCK_MAIN_ACTIVITY);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Thêm flag nếu cần
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();

                }
            }
        });
    }

    private void showDialog() {
        if (!LockUtil.isStatAccessPermissionSet(SplashActivity.this) && LockUtil.isNoOption(SplashActivity.this)) {
            DialogPermission dialog = new DialogPermission(SplashActivity.this);
            dialog.show();
            dialog.setOnClickListener(new DialogPermission.onClickListener() {
                @Override
                public void onClick() {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        startActivityForResult(intent, RESULT_ACTION_USAGE_ACCESS_SETTINGS);
                    }
                }
            });
        } else {
            gotoPinSelfUnlockActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SharedPreferences prefs = getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE);
        if (requestCode == RESULT_ACTION_USAGE_ACCESS_SETTINGS) {
            if (LockUtil.isStatAccessPermissionSet(SplashActivity.this)) {
                gotoPinSelfUnlockActivity();
            } else {
                ToastUtil.showToast("Permission denied");
                finish();
            }
        } else if (requestCode == RESULT_ACTION_ACCESSIBILITY_SETTINGS) {
            SpUtil.getInstance().putBoolean(AppConstants.LOCK_STATE, true);
            gotoPinSelfUnlockActivity();
        } else if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            if (resultCode == RESULT_OK) {
                ToastUtil.showToast("Quyền quản trị viên đã được cấp");
                // Đánh dấu cờ admin_permission_requested
                prefs.edit().putBoolean(AppConstants.ADMIN_PERMISSION_REQUESTED, true).apply();
                initAppLogic();
            } else {
                ToastUtil.showToast("Quyền quản trị viên chưa được cấp");
                finish();
            }
        }
    }

    public boolean isAccessibilityEnabled() {
        int accessibilityEnabled = 0;
        final String ACCESSIBILITY_SERVICE = "io.github.subhamtyagi.privacyapplock/com.lzx.lock.service.LockAccessibilityService";
        try {
            accessibilityEnabled = Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            //setting not found so your phone is not supported
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessabilityService = mStringColonSplitter.next();
                    if (accessabilityService.equalsIgnoreCase(ACCESSIBILITY_SERVICE)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void gotoPinSelfUnlockActivity() {
        SpUtil.getInstance().putBoolean(AppConstants.LOCK_STATE, true);
        Intent intent = new Intent(SplashActivity.this, PinSelfUnlockActivity.class);
        intent.putExtra(AppConstants.LOCK_PACKAGE_NAME, AppConstants.APP_PACKAGE_NAME);
        intent.putExtra(AppConstants.LOCK_FROM, AppConstants.LOCK_FROM_LOCK_MAIN_ACTIVITY);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void initAction() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        animator = null;
    }
}
