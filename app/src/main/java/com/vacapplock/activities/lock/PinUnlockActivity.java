package com.vacapplock.activities.lock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.vacapplock.R;
import com.vacapplock.activities.main.MainActivity;
import com.vacapplock.api.login.PinApi;
import com.vacapplock.base.AppConstants;
import com.vacapplock.base.BaseActivity;
import com.vacapplock.db.CommLockInfoManager;
import com.vacapplock.services.LockService;
import com.vacapplock.utils.LockUtil;
import com.vacapplock.utils.LogUtil;
import com.vacapplock.utils.SpUtil;
import com.vacapplock.widget.UnLockMenuPopWindow;


public class PinUnlockActivity extends BaseActivity implements PinApi.OnPinVerificationListener {
    public static final String FINISH_UNLOCK_THIS_APP = "finish_unlock_this_app";
    private TextView passcodeDot1, passcodeDot2, passcodeDot3, passcodeDot4;
    private StringBuilder currentPin = new StringBuilder();
    private String correctPin = AppConstants.PASSWORD_UNLOCK;
    private String actionFrom;
    private String pkgName;
    private GestureUnlockReceiver mGestureUnlockReceiver;


    private PackageManager packageManager;
    private CommLockInfoManager mLockInfoManager;
    private UnLockMenuPopWindow mPopWindow;
    private ApplicationInfo appInfo;
    private Drawable iconDrawable;
    private String appLabel;
    private ScrollView mUnLockLayout;
    private RelativeLayout mPinUnlock;

    private ImageView appIcon;
    private TextView appName;


    @Override
    public int getLayoutId() {
        return R.layout.activity_pin_unlock;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mUnLockLayout = findViewById(R.id.unlock_layout);
        passcodeDot1 = findViewById(R.id.passcodeDot1);
        passcodeDot2 = findViewById(R.id.passcodeDot2);
        passcodeDot3 = findViewById(R.id.passcodeDot3);
        passcodeDot4 = findViewById(R.id.passcodeDot4);

        appIcon = findViewById(R.id.app_icon);
        appName = findViewById(R.id.app_name);



    }

    @Override
    protected void initData() {
        pkgName = getIntent().getStringExtra(AppConstants.LOCK_PACKAGE_NAME);
        actionFrom = getIntent().getStringExtra(AppConstants.LOCK_FROM);

        pkgName = getIntent().getStringExtra(AppConstants.LOCK_PACKAGE_NAME);
        actionFrom = getIntent().getStringExtra(AppConstants.LOCK_FROM);
        packageManager = getPackageManager();
        mLockInfoManager = new CommLockInfoManager(this);
        mPopWindow = new UnLockMenuPopWindow(this, pkgName, true);

        mGestureUnlockReceiver = new GestureUnlockReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(FINISH_UNLOCK_THIS_APP);
        registerReceiver(mGestureUnlockReceiver, filter);
        initLayoutBackground();
        setNumberClickListeners();

    }

    private void initLayoutBackground() {
        try {
            appInfo = packageManager.getApplicationInfo(pkgName, PackageManager.GET_UNINSTALLED_PACKAGES);
            if (appInfo != null) {
                iconDrawable = packageManager.getApplicationIcon(appInfo);
                appLabel = packageManager.getApplicationLabel(appInfo).toString();
                appName.setText(appLabel);
                appIcon.setImageDrawable(iconDrawable);
                final Drawable icon = packageManager.getApplicationIcon(appInfo);
                mUnLockLayout.setBackgroundDrawable(icon);
                mUnLockLayout.getViewTreeObserver().addOnPreDrawListener(
                        new ViewTreeObserver.OnPreDrawListener() {
                            @Override
                            public boolean onPreDraw() {
                                mUnLockLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                                mUnLockLayout.buildDrawingCache();
                                int width = mUnLockLayout.getWidth(), height = mUnLockLayout.getHeight();
                                if (width == 0 || height == 0) {
                                    Display display = getWindowManager().getDefaultDisplay();
                                    Point size = new Point();
                                    display.getSize(size);
                                    width = size.x;
                                    height = size.y;
                                }
                                Bitmap bmp = LockUtil.drawableToBitmap(icon, width, height);
                                try {
                                    LockUtil.blur(PinUnlockActivity.this, LockUtil.big(bmp), mUnLockLayout, width, height);
                                } catch (IllegalArgumentException ignore) {

                                }
                                return true;
                            }
                        });
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initAction() {

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
            startActivity(new Intent(PinUnlockActivity.this, MainActivity.class));
            finish();
        } else {
            SpUtil.getInstance().putLong(AppConstants.LOCK_CURR_MILLISECONDS, System.currentTimeMillis());
            SpUtil.getInstance().putString(AppConstants.LOCK_LAST_LOAD_PKG_NAME, pkgName);

            //Send the last unlocked time to the app lock service
            Intent intent = new Intent(LockService.UNLOCK_ACTION);
            intent.putExtra(LockService.LOCK_SERVICE_LASTTIME, System.currentTimeMillis());
            intent.putExtra(LockService.LOCK_SERVICE_LASTAPP, pkgName);
            sendBroadcast(intent);

            mLockInfoManager.unlockCommApplication(pkgName);
            finish();
        }
    }

    public void onPinIncorrect() {
        Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
        currentPin.setLength(0);
        updatePinDots();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (actionFrom.equals(AppConstants.LOCK_FROM_FINISH)) {
            LockUtil.goHome(this);
        } else if (actionFrom.equals(AppConstants.LOCK_FROM_LOCK_MAIN_ACTIVITY)) {
            finish();
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGestureUnlockReceiver);
    }

    private class GestureUnlockReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            String action = intent.getAction();
            if (action.equals(FINISH_UNLOCK_THIS_APP)) {
                finish();
            }
        }
    }
}