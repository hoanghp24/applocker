package com.vacapplock.utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.vacapplock.R;

@RequiresApi(api = Build.VERSION_CODES.O)
public class NotificationUtil {

    private static final String NOTIFICATION_CHANNEL_ID = "10101";


    @SuppressLint("ForegroundServiceType")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createNotification(Service mContext,String title, String message) {
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        int importance = NotificationManager.IMPORTANCE_NONE;
        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "VAC Lock background task ", importance);
        mNotificationManager.createNotificationChannel(notificationChannel);

        Notification.Builder mBuilder = new Notification.Builder(mContext);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);


        mContext.startForeground(145,mBuilder.build());
    }

    public static void cancelNotification(Service mContext){
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(145);
    }


}