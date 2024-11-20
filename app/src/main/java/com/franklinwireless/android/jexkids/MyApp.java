package com.franklinwireless.android.jexkids;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.franklinwireless.android.jexkids.locker.base.BaseActivity;
import com.franklinwireless.android.jexkids.locker.protect.UnlockAppActivity;
import com.franklinwireless.android.jexkids.locker.utils.MainUtil;
import com.google.firebase.FirebaseApp;

import org.litepal.LitePalApplication;

import java.util.ArrayList;
import java.util.List;



public class MyApp extends LitePalApplication {

    private static MyApp application;
    private static List<BaseActivity> activityList;

    public static Long lastLockedTime;

    public static String lastLockedPkgName;


    public static MyApp getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        FirebaseApp.initializeApp(this);
        MainUtil.getInstance().init(application);
        activityList = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannels();
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
        return activity instanceof UnlockAppActivity;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannels() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel foreground = new NotificationChannel("foreground", getString(R.string.channel_foreground), NotificationManager.IMPORTANCE_MIN);
        foreground.setSound(null, Notification.AUDIO_ATTRIBUTES_DEFAULT);
        nm.createNotificationChannel(foreground);

        NotificationChannel notify = new NotificationChannel("notify", getString(R.string.channel_notify), NotificationManager.IMPORTANCE_DEFAULT);
        notify.setSound(null, Notification.AUDIO_ATTRIBUTES_DEFAULT);
        notify.setBypassDnd(true);
        nm.createNotificationChannel(notify);

        NotificationChannel access = new NotificationChannel("access", getString(R.string.channel_access), NotificationManager.IMPORTANCE_DEFAULT);
        access.setSound(null, Notification.AUDIO_ATTRIBUTES_DEFAULT);
        access.setBypassDnd(true);
        nm.createNotificationChannel(access);
    }
}
