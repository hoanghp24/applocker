package com.vacapplock.base;

import java.util.Arrays;
import java.util.List;

public class AppConstants {

    public static final String PREFS_NAME = "AppPrefs";
    public static final String ADMIN_PERMISSION_REQUESTED = "admin_permission_requested";
    public static final String IS_REQUESTING_PERMISSIONS = "isRequestingPermissions";
    //intent constant
    public static final String APP_PACKAGE_NAME = "APPLICATION_ID";
    public static final String LOCK_PACKAGE_NAME = "lock_package_name";
    public static final String LOCK_FROM = "lock_from";
    public static final String LOCK_FROM_FINISH = "lock_from_finish";
    public static final String LOCK_FROM_SETTING = "lock_from_setting";
    public static final String LOCK_FROM_LOCK_MAIN_ACTIVITY = "lock_from_lock_main_activity";

    //shared prefs constant
    public static final String LOCK_STATE = "app_lock_state";//boolean
    public static final String LOCK_FAVITER_NUM = "lock_faviter_num";//int
    public static final String LOCK_SYS_APP_NUM = "lock_sys_app_num";//int
    public static final String LOCK_USER_APP_NUM = "lock_user_app_num";//int
    public static final String LOCK_IS_INIT_FAVITER = "lock_is_init_faviter";//boolean
    public static final String LOCK_IS_INIT_DB = "lock_is_init_db";//boolean
    public static final String LOCK_IS_HIDE_LINE = "lock_is_hide_line";//boolean
    public static final String LOCK_IS_FIRST_LOCK = "is_lock"; //boolean
    public static final String LOCK_AUTO_SCREEN = "lock_auto_screen";//boolean
    public static final String LOCK_AUTO_SCREEN_TIME = "lock_auto_screen_time"; //boolean
    public static final String LOCK_CURR_MILLISECONDS = "lock_curr_milliseconds";//long
    public static final String LOCK_APART_MILLISECONDS = "lock_apart_milliseconds";//long
    public static final String LOCK_APART_TITLE = "lock_apart_title";//string
    public static final String LOCK_LAST_LOAD_PKG_NAME = "last_load_package_name";//string
    public static final String LOCK_AUTO_RECORD_PIC = "AutoRecordPic";//boolean
    public static final String PATTERN_VIBRATION="pattern_vibration";

    public static final String PASSWORD_UNLOCK = "1111";

    // List of apps that ignored lock
    public static final List<String> EXCLUDE_LOCK_PACKAGES = Arrays.asList(
            "com.microsoft.office.excel",
            "com.vacapplock",
            "com.example.vac_management_app",
            "com.vacapp",
            "com.symbol.datawedge",
            "ost.com.vn.m1"
    );

}
