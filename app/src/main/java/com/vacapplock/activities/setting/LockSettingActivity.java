package com.vacapplock.activities.setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.vacapplock.R;
import com.vacapplock.base.AppConstants;
import com.vacapplock.base.BaseActivity;
import com.vacapplock.services.BackgroundManager;
import com.vacapplock.services.LockService;
import com.vacapplock.utils.SpUtil;
import com.vacapplock.utils.SystemBarHelper;
import com.vacapplock.widget.SelectLockTimeDialog;


public class LockSettingActivity extends BaseActivity implements View.OnClickListener, DialogInterface.OnDismissListener, CompoundButton.OnCheckedChangeListener {

    public static final String ON_ITEM_CLICK_ACTION = "on_item_click_action";
    private static final int REQUEST_CHANGE_PWD = 3;

    private Switch cbLockSwitch;

    private LockSettingReceiver mLockSettingReceiver;
    private SelectLockTimeDialog dialog;
    private RelativeLayout mTopLayout;
    private ImageView btnBack;
    private TextView tvVersion;

    @Override
    public int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        cbLockSwitch = findViewById(R.id.checkbox_app_lock_on_off);
        btnBack = findViewById(R.id.btn_back);
        tvVersion = findViewById(R.id.tv_version);

        //
        mTopLayout = findViewById(R.id.top_layout);
        mTopLayout.setPadding(0, SystemBarHelper.getStatusBarHeight(this), 0, 0);

    }

    @Override
    protected void initData() {
        mLockSettingReceiver = new LockSettingReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ON_ITEM_CLICK_ACTION);
        registerReceiver(mLockSettingReceiver, filter);
        dialog = new SelectLockTimeDialog(this, "");
        dialog.setOnDismissListener(this);
        boolean isLockOpen = SpUtil.getInstance().getBoolean(AppConstants.LOCK_STATE);
        cbLockSwitch.setChecked(isLockOpen);


        String versionName = null;
        try {
            versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        tvVersion.setText("version " + versionName);


    }

    @Override
    protected void initAction() {

        cbLockSwitch.setOnCheckedChangeListener(this);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean b) {
        if (buttonView.getId() == R.id.checkbox_app_lock_on_off) {
            SpUtil.getInstance().putBoolean(AppConstants.LOCK_STATE, b);
            if (b) {
                BackgroundManager.getInstance().init(LockSettingActivity.this).stopService(LockService.class);
                BackgroundManager.getInstance().init(LockSettingActivity.this).startService(LockService.class);

                BackgroundManager.getInstance().init(LockSettingActivity.this).startAlarmManager();

            } else {
                BackgroundManager.getInstance().init(LockSettingActivity.this).stopService(LockService.class);
                BackgroundManager.getInstance().init(LockSettingActivity.this).stopAlarmManager();
            }


        }

    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {

    }

    @Override
    public void onClick(View v) {

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLockSettingReceiver);
    }

    private class LockSettingReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            String action = intent.getAction();
            if (action.equals(ON_ITEM_CLICK_ACTION)) {

                dialog.dismiss();
            }
        }
    }

}
