package com.vacapplock.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vacapplock.base.AppConstants;
import com.vacapplock.model.CommLockInfo;
import com.vacapplock.utils.DataUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CommLockInfoManager {

    private DatabaseHelper dbHelper;
    private PackageManager mPackageManager;

    @NonNull
    private Comparator<CommLockInfo> commLockInfoComparator = new Comparator<CommLockInfo>() {
        @Override
        public int compare(CommLockInfo lhs, CommLockInfo rhs) {
            return lhs.getAppName().compareTo(rhs.getAppName()); // Sắp xếp theo tên ứng dụng
        }
    };

    public CommLockInfoManager(Context context) {
        dbHelper = new DatabaseHelper(context);
        mPackageManager = context.getPackageManager();
    }

    @SuppressLint("Range")
    public synchronized List<CommLockInfo> getAllCommLockInfos() {
        List<CommLockInfo> commLockInfos = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                CommLockInfo info = new CommLockInfo();
                info.setPackageName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PACKAGE_NAME)));
                info.setAppName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_APP_NAME)));
                info.setLocked(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_LOCKED)) > 0);
                info.setFaviterApp(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_FAVITER_APP)) > 0);
                info.setSetUnLock(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_SET_UNLOCK)) > 0);
                commLockInfos.add(info);
            }
            cursor.close();
        }

        Collections.sort(commLockInfos, commLockInfoComparator);
        return commLockInfos;
    }

    public synchronized void deleteCommLockInfoTable(@NonNull List<CommLockInfo> commLockInfos) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        for (CommLockInfo info : commLockInfos) {
            db.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper.COLUMN_PACKAGE_NAME + " = ?", new String[]{info.getPackageName()});
        }
    }

    public synchronized void instanceCommLockInfoTable(@NonNull List<ResolveInfo> resolveInfos) throws PackageManager.NameNotFoundException {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<CommLockInfo> list = new ArrayList<>();


        for (ResolveInfo resolveInfo : resolveInfos) {
            boolean isFaviterApp = isHasFaviterAppInfo(resolveInfo.activityInfo.packageName);

            boolean isLocked = !AppConstants.EXCLUDE_LOCK_PACKAGES.contains(resolveInfo.activityInfo.packageName);

            CommLockInfo commLockInfo = new CommLockInfo(resolveInfo.activityInfo.packageName, isLocked, isFaviterApp);
            ApplicationInfo appInfo = mPackageManager.getApplicationInfo(commLockInfo.getPackageName(), PackageManager.GET_UNINSTALLED_PACKAGES);
            String appName = mPackageManager.getApplicationLabel(appInfo).toString();

            if (!commLockInfo.getPackageName().equals(AppConstants.APP_PACKAGE_NAME)) {
                commLockInfo.setAppName(appName);
                commLockInfo.setSetUnLock(false);

                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_PACKAGE_NAME, commLockInfo.getPackageName());
                values.put(DatabaseHelper.COLUMN_APP_NAME, commLockInfo.getAppName());
                values.put(DatabaseHelper.COLUMN_IS_LOCKED, commLockInfo.isLocked() ? 1 : 0);
                values.put(DatabaseHelper.COLUMN_IS_FAVITER_APP, isFaviterApp ? 1 : 0);
                values.put(DatabaseHelper.COLUMN_IS_SET_UNLOCK, 0); // Giá trị mặc định
                db.insert(DatabaseHelper.TABLE_NAME, null, values);

                list.add(commLockInfo);
            }
        }
        list = DataUtil.clearRepeatCommLockInfo(list);
    }


    private boolean isHasFaviterAppInfo(String packageName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("faviter_info", null, "packageName = ?", new String[]{packageName}, null, null, null);
        boolean hasFaviter = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return false;
    }

    public void lockCommApplication(String packageName) {
        Log.d("CommLockInfoManager", "Gọi hàm khóa cho: " + packageName);
        updateLockStatus(packageName, true);
    }

    public void unlockCommApplication(String packageName) {
        Log.d("CommLockInfoManager", "Gọi hàm mở khóa cho: " + packageName);
        updateLockStatus(packageName, false);
    }

    private void updateLockStatus(String packageName, boolean isLock) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_IS_LOCKED, isLock ? 1 : 0);

        int rowsAffected = db.update(DatabaseHelper.TABLE_NAME, values, DatabaseHelper.COLUMN_PACKAGE_NAME + " = ?", new String[]{packageName});
        if (rowsAffected == 0) {
            Log.e("CommLockInfoManager", "Không thể cập nhật trạng thái khóa cho: " + packageName);
        }
    }

    @SuppressLint("Range")
    public boolean isSetUnLock(String packageName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, null, DatabaseHelper.COLUMN_PACKAGE_NAME + " = ?", new String[]{packageName}, null, null, null);
        boolean isSetUnlock = false;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                isSetUnlock = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_SET_UNLOCK)) > 0;
            }
            cursor.close();
        }
        return isSetUnlock;
    }

    @SuppressLint("Range")
    public boolean isLockedPackageName(String packageName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, null, DatabaseHelper.COLUMN_PACKAGE_NAME + " = ?", new String[]{packageName}, null, null, null);
        boolean isLocked = false;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                isLocked = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_LOCKED)) > 0;
            }
            cursor.close();
        }
        return isLocked;
    }

    @SuppressLint("Range")
    public List<CommLockInfo> queryBlurryList(String appName) {
        List<CommLockInfo> commLockInfos = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, null, "appName LIKE ?", new String[]{"%" + appName + "%"}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                CommLockInfo info = new CommLockInfo();
                info.setPackageName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PACKAGE_NAME)));
                info.setAppName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_APP_NAME)));
                info.setLocked(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_LOCKED)) > 0);
                info.setFaviterApp(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_FAVITER_APP)) > 0);
                info.setSetUnLock(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_SET_UNLOCK)) > 0);
                commLockInfos.add(info);
            }
            cursor.close();
        }
        return commLockInfos;
    }

    public void setIsUnLockThisApp(String packageName, boolean isSetUnLock) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_IS_SET_UNLOCK, isSetUnLock ? 1 : 0);
        db.update(DatabaseHelper.TABLE_NAME, values, DatabaseHelper.COLUMN_PACKAGE_NAME + " = ?", new String[]{packageName});
    }
}
