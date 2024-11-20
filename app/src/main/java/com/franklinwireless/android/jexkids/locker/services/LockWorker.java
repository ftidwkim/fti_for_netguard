package com.franklinwireless.android.jexkids.locker.services;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.franklinwireless.android.jexkids.MyApp;
import com.franklinwireless.android.jexkids.R;
import com.franklinwireless.android.jexkids.home.Homepage;
import com.franklinwireless.android.jexkids.locker.base.AppConstants;
import com.franklinwireless.android.jexkids.locker.db.CommLockInfoManager;
import com.franklinwireless.android.jexkids.locker.protect.UnlockAppActivity;
import com.franklinwireless.android.jexkids.locker.receiver.LockRestarterBroadcastReceiver;
import com.franklinwireless.android.jexkids.locker.utils.MainUtil;
import com.franklinwireless.android.jexkids.locker.utils.NotificationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class LockWorker extends Worker {

    public static final String UNLOCK_ACTION = "UNLOCK_ACTION";
    public static final String LOCK_SERVICE_LASTTIME = "LOCK_SERVICE_LASTTIME";
    public static final String LOCK_SERVICE_LASTAPP = "LOCK_SERVICE_LASTAPP";
    private static final String TAG = "LockService";
    public static boolean isActionLock = false;
    public boolean threadIsTerminate = false;
    @Nullable
    public String savePkgName;
    UsageStatsManager sUsageStatsManager;
    Timer timer = new Timer();
    //private boolean isLockTypeAccessibility;
    private long lastUnlockTimeSeconds = 0;
    private String lastUnlockPackageName = "";
    private boolean lockState;
    private ServiceReceiver mServiceReceiver;
    private CommLockInfoManager mLockInfoManager;
    public Homepage mainActivity;


    @Nullable
    private ActivityManager activityManager;
    private NotificationManager notificationManager;
    private static final String NOTIFICATION_CHANNEL_ID = "1228";

    private String packageName ="";

    private String appName="";

    public LockWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        notificationManager = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
        packageName = workerParams.getInputData().getString("packageName");
        appName = workerParams.getInputData().getString("appName");
        lockState = MainUtil.getInstance().getBoolean(AppConstants.LOCK_STATE);
        mLockInfoManager = new CommLockInfoManager(getApplicationContext());
        activityManager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);

        mServiceReceiver = new ServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(UNLOCK_ACTION);
        getApplicationContext().registerReceiver(mServiceReceiver, filter);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            sUsageStatsManager = (UsageStatsManager) getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);
        }
        threadIsTerminate = true;
    }

    @NonNull
    private ForegroundInfo createForegroundInfo(@NonNull String progress) {
        // Build a notification using bytesRead and contentLength

        Context context = getApplicationContext();
        String title = appName + " Blocked";
        // This PendingIntent can be used to cancel the worker
        PendingIntent intent = WorkManager.getInstance(context)
                .createCancelPendingIntent(getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }

        Notification notification = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(progress)
                .setTicker(title)
                .setSmallIcon(R.drawable.jexkidslogo)
                .setOngoing(true)
                // Add the cancel action to the notification which can
                // be used to cancel the worker if required
                .build();

        return new ForegroundInfo(1, notification);
    }

    @NonNull
    @Override
    public ForegroundInfo getForegroundInfo() {
        Context context = getApplicationContext();
        String title = appName + " Blocked";
        // This PendingIntent can be used to cancel the worker
        PendingIntent intent = WorkManager.getInstance(context)
                .createCancelPendingIntent(getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }

        Notification notification = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setTicker(title)
                .setSmallIcon(R.drawable.jexkidslogo)
                .setOngoing(true)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                // Add the cancel action to the notification which can
                // be used to cancel the worker if required
                .build();

        return new ForegroundInfo(1, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        Context context = getApplicationContext();
        String title = appName + " Blocked";
        // Create a Notification channel
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "App lock background task ", importance);
        notificationManager.createNotificationChannel(notificationChannel);
        Notification notification = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setTicker(title)
                .setSmallIcon(R.drawable.jexkidslogo)
                .setOngoing(true)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                // Add the cancel action to the notification which can
                // be used to cancel the worker if required
                .build();
        notificationManager.notify(1, notification);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        String title = appName + " Blocked";
        setForegroundAsync(createForegroundInfo(title));
        MyApp.getInstance().clearAllActivity();
        Intent intent = new Intent(getApplicationContext(), UnlockAppActivity.class);

        intent.putExtra(AppConstants.LOCK_PACKAGE_NAME, packageName);
        intent.putExtra(AppConstants.LOCK_FROM, AppConstants.LOCK_FROM_FINISH);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
//        runForever();
        return Result.success();
    }

    @Override
    public void onStopped() {
        threadIsTerminate = false;
        timer.cancel();
        lockState = MainUtil.getInstance().getBoolean(AppConstants.LOCK_STATE);
        if (lockState) {
            Intent restartServiceTask = new Intent(getApplicationContext(), this.getClass());
            restartServiceTask.setPackage(getApplicationContext().getPackageName());
            PendingIntent restartPendingIntent = PendingIntent.getService(getApplicationContext(), 1495, restartServiceTask, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            myAlarmService.set(
                    AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + 1500,
                    restartPendingIntent);
//            Intent intent = new Intent(getApplicationContext(), LockRestarterBroadcastReceiver.class);
//            intent.putExtra("type", "lockservice");
//            getApplicationContext().sendBroadcast(intent);
            getApplicationContext().unregisterReceiver(mServiceReceiver);
        }
    }

    public class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            String action = intent.getAction();

            boolean isLockOffScreen = MainUtil.getInstance().getBoolean(AppConstants.LOCK_AUTO_SCREEN, false);
            boolean isLockOffScreenTime = MainUtil.getInstance().getBoolean(AppConstants.LOCK_AUTO_SCREEN_TIME, false);

            switch (action) {
                case UNLOCK_ACTION:
                    lastUnlockPackageName = intent.getStringExtra(LOCK_SERVICE_LASTAPP);
                    lastUnlockTimeSeconds = intent.getLongExtra(LOCK_SERVICE_LASTTIME, lastUnlockTimeSeconds);
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    MainUtil.getInstance().putLong(AppConstants.LOCK_CURR_MILLISECONDS, System.currentTimeMillis());

                    if (!isLockOffScreenTime && isLockOffScreen) {
                        String savePkgName = MainUtil.getInstance().getString(AppConstants.LOCK_LAST_LOAD_PKG_NAME, "");
                        if (!TextUtils.isEmpty(savePkgName)) {
                            if (isActionLock) {
                                mLockInfoManager.lockCommApplication(lastUnlockPackageName);
                            }
                        }
                    }
                    break;
            }
        }
    }
    private void runForever() {

        while (threadIsTerminate) {
            String packageName = getLauncherTopApp(getApplicationContext(), activityManager);
            if (lockState && !TextUtils.isEmpty(packageName) && !inWhiteList(packageName)) {
                boolean isLockOffScreenTime = MainUtil.getInstance().getBoolean(AppConstants.LOCK_AUTO_SCREEN_TIME, false);
                boolean isLockOffScreen = MainUtil.getInstance().getBoolean(AppConstants.LOCK_AUTO_SCREEN, false);
                savePkgName = MainUtil.getInstance().getString(AppConstants.LOCK_LAST_LOAD_PKG_NAME, "");

                if (isLockOffScreenTime && !isLockOffScreen) {
                    long time = MainUtil.getInstance().getLong(AppConstants.LOCK_CURR_MILLISECONDS, 0);
                    long leaverTime = MainUtil.getInstance().getLong(AppConstants.LOCK_APART_MILLISECONDS, 0);
                    if (!TextUtils.isEmpty(savePkgName) && !TextUtils.isEmpty(packageName) && !savePkgName.equals(packageName)) {
                        if (getHomes().contains(packageName) || packageName.contains("launcher")) {
                            boolean isSetUnLock = mLockInfoManager.isSetUnLock(savePkgName);
                            if (!isSetUnLock) {
                                if (System.currentTimeMillis() - time > leaverTime) {
                                    mLockInfoManager.lockCommApplication(savePkgName);
                                }
                            }
                        }
                    }
                }

                if (isLockOffScreenTime && isLockOffScreen) {
                    long time = MainUtil.getInstance().getLong(AppConstants.LOCK_CURR_MILLISECONDS, 0);
                    long leaverTime = MainUtil.getInstance().getLong(AppConstants.LOCK_APART_MILLISECONDS, 0);
                    if (!TextUtils.isEmpty(savePkgName) && !TextUtils.isEmpty(packageName) && !savePkgName.equals(packageName)) {
                        if (getHomes().contains(packageName) || packageName.contains("launcher")) {
                            boolean isSetUnLock = mLockInfoManager.isSetUnLock(savePkgName);
                            if (!isSetUnLock) {
                                if (System.currentTimeMillis() - time > leaverTime) {
                                    mLockInfoManager.lockCommApplication(savePkgName);
                                }
                            }
                        }
                    }
                }


                if (!isLockOffScreenTime && isLockOffScreen && !TextUtils.isEmpty(savePkgName) && !TextUtils.isEmpty(packageName)) {
                    if (!savePkgName.equals(packageName)) {
                        isActionLock = false;
                        if (getHomes().contains(packageName) || packageName.contains("launcher")) {
                            boolean isSetUnLock = mLockInfoManager.isSetUnLock(savePkgName);
                            if (!isSetUnLock) {
                                mLockInfoManager.lockCommApplication(savePkgName);
                            }
                        }
                    } else {
                        isActionLock = true;
                    }
                }
                if (!isLockOffScreenTime && !isLockOffScreen && !TextUtils.isEmpty(savePkgName) && !TextUtils.isEmpty(packageName) && !savePkgName.equals(packageName)) {
                    if (getHomes().contains(packageName) || packageName.contains("launcher")) {
                        boolean isSetUnLock = mLockInfoManager.isSetUnLock(savePkgName);
                        if (!isSetUnLock) {
                            mLockInfoManager.lockCommApplication(savePkgName);
                        }
                    }
                }
                if (mLockInfoManager.isLockedPackageName(packageName)) {
                    String appName = "App";
                    try {
                        PackageManager packageManager = getApplicationContext().getPackageManager();
                        ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
                        appName = packageManager.getApplicationLabel(appInfo).toString();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    passwordLock(packageName, appName);
                    continue;
                }
            }
            try {
                Thread.sleep(210);
            } catch (Exception ignore) {
            }
        }
    }

    private boolean inWhiteList(String packageName) {
        return packageName.equals(AppConstants.APP_PACKAGE_NAME);
    }

    public String getLauncherTopApp(@NonNull Context context, @NonNull ActivityManager activityManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            List<ActivityManager.RunningTaskInfo> appTasks = activityManager.getRunningTasks(1);
            if (null != appTasks && !appTasks.isEmpty()) {
                return appTasks.get(0).topActivity.getPackageName();
            }
        } else {
            long endTime = System.currentTimeMillis();
            long beginTime = endTime - 10000;
            String result = "";
            UsageEvents.Event event = new UsageEvents.Event();
            UsageEvents usageEvents = sUsageStatsManager.queryEvents(beginTime, endTime);
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    result = event.getPackageName();
                }
            }
            if (!android.text.TextUtils.isEmpty(result)) {
                return result;
            }
        }
        return "";
    }

    @NonNull
    private List<String> getHomes() {
        List<String> names = new ArrayList<>();
        PackageManager packageManager = getApplicationContext().getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }

    private void passwordLock(String packageName, String appName) {

    }
}




