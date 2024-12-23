package com.vacapplock.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "comm_lock_info.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "comm_lock_info";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PACKAGE_NAME = "package_name";
    public static final String COLUMN_APP_NAME = "app_name";
    public static final String COLUMN_IS_LOCKED = "is_locked";
    public static final String COLUMN_IS_FAVITER_APP = "is_faviter_app";
    public static final String COLUMN_IS_SET_UNLOCK = "is_set_unlock";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PACKAGE_NAME + " TEXT, " +
                COLUMN_APP_NAME + " TEXT, " +
                COLUMN_IS_LOCKED + " INTEGER, " +
                COLUMN_IS_FAVITER_APP + " INTEGER, " +
                COLUMN_IS_SET_UNLOCK + " INTEGER)";
        db.execSQL(createTable);

        String createFaviterInfoTable = "CREATE TABLE faviter_info (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "packageName TEXT)";
        db.execSQL(createFaviterInfoTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
