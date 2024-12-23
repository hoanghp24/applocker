package com.vacapplock;

import android.app.Application;
import com.vacapplock.activities.lock.PinUnlockActivity;
import com.vacapplock.base.BaseActivity;
import com.vacapplock.utils.SpUtil;

import java.util.ArrayList;
import java.util.List;

public class MainApplication extends Application {

    private static MainApplication application;
    private static List<BaseActivity> activityList;

    public static MainApplication getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        // Initialize Shared Preferences Utility
        SpUtil.getInstance().init(application);

        // Initialize the activity list
        activityList = new ArrayList<>();
    }

    public void doForCreate(BaseActivity activity) {
        activityList.add(activity);
    }

    public void doForFinish(BaseActivity activity) {
        activityList.remove(activity);
    }

    public void clearAllActivity() {
        try {
            for (BaseActivity activity : activityList) {
                if (activity != null && !clearAllWhiteList(activity))
                    activity.clear();
            }
            activityList.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean clearAllWhiteList(BaseActivity activity) {
        return activity instanceof PinUnlockActivity;
    }
}
