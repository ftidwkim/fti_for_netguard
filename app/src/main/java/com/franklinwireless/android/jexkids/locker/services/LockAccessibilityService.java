package com.franklinwireless.android.jexkids.locker.services;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.franklinwireless.android.jexkids.MyApp;
import com.franklinwireless.android.jexkids.home.Homepage;
import com.franklinwireless.android.jexkids.locker.base.AppConstants;
import com.franklinwireless.android.jexkids.locker.db.CommLockInfoManager;
import com.franklinwireless.android.jexkids.locker.protect.UnlockAppActivity;
import com.franklinwireless.android.jexkids.locker.receiver.LockRestarterBroadcastReceiver;
import com.franklinwireless.android.jexkids.locker.utils.MainUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

public class LockAccessibilityService extends AccessibilityService {


    private static String packageName;
    private static LockAccessibilityService mInstance = null;

    public static final String LOCK_ACTION = "LOCK_ACTION";
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
    private LockReceiver mLockReceiver;
    private CommLockInfoManager mLockInfoManager;
    public Homepage mainActivity;


    @Nullable
    private ActivityManager activityManager;

    public LockAccessibilityService() {

    }

    public static LockAccessibilityService getInstance() {
        if (mInstance == null) {
            synchronized (LockAccessibilityService.class) {
                mInstance = new LockAccessibilityService();
            }
        }
        return mInstance;
    }


    @Override
    public void onAccessibilityEvent(@NonNull AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null && event.getClassName() != null) {
                packageName = event.getPackageName().toString();
                if (packageName != null) {
                    Log.e("foreground event", event.getPackageName().toString());
                    if (getHomes().contains(packageName) || packageName.contains("launcher")){
                        mLockInfoManager.unlockCommApplication(packageName);
                    }else {
                        SharedPreferences msharedpref = getApplicationContext().getSharedPreferences("login",
                                Context.MODE_PRIVATE);
                        msharedpref.edit().putString("LAST_OPENED_APP",packageName).commit();
                        if (mLockInfoManager.isLockedPackageName(packageName)) {
                            String appName = "App";
                            try {
                                PackageManager packageManager = getPackageManager();
                                ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
                                appName = packageManager.getApplicationLabel(appInfo).toString();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            passwordLock(packageName, appName);
                        }else {
                            msharedpref = getApplicationContext().getSharedPreferences("login",
                                    Context.MODE_PRIVATE);
                            long appLimit = msharedpref.getLong(packageName+"_applimit",0);
                            if (appLimit != 0){
                                long time = System.currentTimeMillis();
                                Calendar cal = Calendar.getInstance();
                                cal.set(Calendar.HOUR_OF_DAY, 0);
                                cal.set(Calendar.MINUTE, 0);
                                cal.set(Calendar.SECOND,0);
                                cal.set(Calendar.MILLISECOND, 0);
                                Date todayDate = cal.getTime();
                                long appTotalTime = getTimeSpent(this, packageName,todayDate.getTime(),time);
                                if (appTotalTime > appLimit){
                                    mLockInfoManager.lockCommApplication(packageName);
                                    String appName = "App";
                                    try {
                                        PackageManager packageManager = getPackageManager();
                                        ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
                                        appName = packageManager.getApplicationLabel(appInfo).toString();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    passwordLock(packageName, appName);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d("Accessibility","Service Connected");
        lockState = MainUtil.getInstance().getBoolean(AppConstants.LOCK_STATE);
        mLockInfoManager = new CommLockInfoManager(this);
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            sUsageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        }
        mLockReceiver = new LockReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(LOCK_ACTION);
        registerReceiver(mLockReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent =new Intent(this, LockRestarterBroadcastReceiver.class);
        intent.putExtra("type","accessservice");
        sendBroadcast(intent);
        unregisterReceiver(mLockReceiver);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        Intent restartServiceTask = new Intent(getApplicationContext(),this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent =PendingIntent.getService(getApplicationContext(), 1, restartServiceTask, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent);

        super.onTaskRemoved(rootIntent);
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
        Intent launcherIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launcherIntent != null) {
            if (launcherIntent.hasCategory(Intent.CATEGORY_HOME)) {
                names.add(packageName);
            }
        }
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }

    private void passwordLock(String packageName, String appName) {
//        Data.Builder data = new Data.Builder();
////Add parameter in Data class. just like bundle. You can also add Boolean and Number in parameter.
//        data.putString("packageName", packageName);
//        data.putString("appName", appName);
        MyApp.getInstance().clearAllActivity();
        Intent intent = new Intent(getApplicationContext(), UnlockAppActivity.class);

        intent.putExtra(AppConstants.LOCK_PACKAGE_NAME, packageName);
        intent.putExtra(AppConstants.LOCK_FROM, AppConstants.LOCK_FROM_FINISH);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
    }

    public Integer getTimeSpent(Context context, String packageName, long beginTime, long endTime) {
        UsageEvents.Event currentEvent;
        List<UsageEvents.Event> allEvents = new ArrayList<>();
        HashMap<String, Integer> appUsageMap = new HashMap<>();

        UsageStatsManager usageStatsManager = (UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE);
        UsageEvents usageEvents = usageStatsManager.queryEvents(beginTime, endTime);

        while (usageEvents.hasNextEvent()) {
            currentEvent = new UsageEvents.Event();
            usageEvents.getNextEvent(currentEvent);
            if(currentEvent.getPackageName().equals(packageName) || packageName == null) {
                if (currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED
                        || currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED) {
                    allEvents.add(currentEvent);
                    String key = currentEvent.getPackageName();
                    if (appUsageMap.get(key) == null)
                        appUsageMap.put(key, 0);
                }
            }
        }

        for (int i = 0; i < allEvents.size() - 1; i++) {
            UsageEvents.Event E0 = allEvents.get(i);
            UsageEvents.Event E1 = allEvents.get(i + 1);

            if (E0.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED
                    && E1.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED
                    && E0.getClassName().equals(E1.getClassName())) {
                int diff = (int)(E1.getTimeStamp() - E0.getTimeStamp());
//                    diff /= 1000;
                Integer prev = appUsageMap.get(E0.getPackageName());
                if(prev == null) prev = 0;
                appUsageMap.put(E0.getPackageName(), prev + diff);
            }
        }
        if (allEvents.size() > 0) {
            UsageEvents.Event lastEvent = allEvents.get(allEvents.size() - 1);
            if (lastEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                int diff = (int) System.currentTimeMillis() - (int) lastEvent.getTimeStamp();
//                diff /= 1000;
                Integer prev = appUsageMap.get(lastEvent.getPackageName());
                if (prev == null) prev = 0;
                appUsageMap.put(lastEvent.getPackageName(), prev + diff);
            }
            return appUsageMap.get(lastEvent.getPackageName());
        }else {
            return 0;
        }
    }
}
