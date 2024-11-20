package com.franklinwireless.android.jexkids.locker.services;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.franklinwireless.android.jexkids.home.Homepage;
import com.franklinwireless.android.jexkids.locker.base.ApiInterface;
import com.franklinwireless.android.jexkids.locker.db.CommLockInfoManager;
import com.franklinwireless.android.jexkids.locker.gesture.contract.LockMainContract;
import com.franklinwireless.android.jexkids.locker.gesture.contract.MainContract;
import com.franklinwireless.android.jexkids.locker.gesture.presenter.LockMainPresenter;
import com.franklinwireless.android.jexkids.locker.model.CommLockInfo;
import com.android.volley.Request;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import eu.faircode.netguard.Rule;
import eu.faircode.netguard.ServiceSinkhole;

public class APIService extends Service implements LockMainContract.View {
    //public static final long NOTIFY_INTERVAL = 10 * 60 * 1000; // 10 minutes
    //public static final long NOTIFY_INTERVAL = 1000;

    LockMainPresenter mLockMainPresenter;
    private CommLockInfoManager mLockInfoManager;
    @Nullable
    private List<CommLockInfo> data;
    private HashMap<String, JSONObject> usages = new HashMap<>();
    JSONArray array;
    ArrayList<String> arr = new ArrayList<String>();
    ArrayList<String> arr1 = new ArrayList<String>();


    private String getDateTime() {
        // get date time in custom format
        SimpleDateFormat sdf = new SimpleDateFormat("[yyyy/MM/dd - HH:mm:ss]");
        return sdf.format(new Date());
    }

    @Override
    public void loadAppInfoSuccess(List<CommLockInfo> list) {
        mLockInfoManager = new CommLockInfoManager(getApplicationContext());
        data = list;
        getParentList(list);
    }

    public void getParentList(List<CommLockInfo> list) {
        array = new JSONArray();
        usages = new HashMap<>();

        Log.d("Daniel Debug", "getParentList() Start");

        for (CommLockInfo info : list) {
            String PackageName = "Nothing";
            long TimeInforground = 500;
            int minutes = 0, seconds = 0, hours = 0;
            UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(getApplication().USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date todayDate = cal.getTime();
            List<String> pNames = new ArrayList<>();
            //SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();

            PackageName = info.getPackageName();

            if (!pNames.contains(PackageName)) {
                pNames.add(PackageName);
                TimeInforground = getTimeSpent(getApplicationContext(), PackageName, todayDate.getTime(), time);

                //minutes = (int) ((TimeInforground / (1000 * 60)) % 60);
                //seconds = (int) (TimeInforground / 1000) % 60;
                //hours = (int) ((TimeInforground / (1000 * 60 * 60)) % 24);
                //long installTimeInMilliseconds; // install time is conveniently provided in milliseconds

                PackageManager packageManager = getPackageManager();
                Date installDate = null;
                String installDateString = "";

                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(info.getPackageName(), 0);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    installDateString = dateFormat.format(new Date(packageInfo.firstInstallTime));
                } catch (PackageManager.NameNotFoundException e) {
                    installDate = new Date(0);
                    installDateString = installDate.toString();
                }

                if (info.getPackageName().equals(PackageName)) {
                    try {
                        JSONObject objp = new JSONObject();
                        objp.put("appname", info.getAppName());
                        objp.put("packagename", info.getPackageName());
                        objp.put("usage_time", TimeInforground);
                        objp.put("blocked_status", info.isLocked());
                        objp.put("sysapp", info.isSysApp());
                        objp.put("installed_time", installDateString);
                        usages.put(info.getPackageName(), objp);
                        array.put(objp.toString());

//                                      Bitmap bitmap = ((BitmapDrawable)packageManager.getApplicationIcon(appInfo)).getBitmap();
//                                    String imgn=getStringImage(bitmap);
                        arr.add(info.getPackageName());
//                                    arr1.add(bitmap.toString());
                    } catch (Exception e) {
                        Log.e("error", e.getMessage());
                    }
                }
            }
        }

        goto_parent(list);
        Log.d("Daniel Debug", "getParentList() End");
    }

    public Integer getTimeSpent(Context context, String packageName, long beginTime, long endTime) {
        UsageEvents.Event currentEvent;
        List<UsageEvents.Event> allEvents = new ArrayList<>();
        HashMap<String, Integer> appUsageMap = new HashMap<>();

        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        UsageEvents usageEvents = usageStatsManager.queryEvents(beginTime, endTime);

        while (usageEvents.hasNextEvent()) {
            currentEvent = new UsageEvents.Event();
            usageEvents.getNextEvent(currentEvent);
            if (currentEvent.getPackageName().equals(packageName) || packageName == null) {
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
                int diff = (int) (E1.getTimeStamp() - E0.getTimeStamp());
//                    diff /= 1000;
                Integer prev = appUsageMap.get(E0.getPackageName());
                if (prev == null) prev = 0;
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
        } else {
            return 0;
        }
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    public void goto_parent(List<CommLockInfo> list) {
        try {
            String imei = "";

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                imei = Settings.Secure.getString(
                        getContentResolver(),
                        Settings.Secure.ANDROID_ID);
            } else {
                final TelephonyManager mTelephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (mTelephony.getDeviceId() != null) {
                    imei = mTelephony.getDeviceId();
                } else {
                    imei = Settings.Secure.getString(
                            getContentResolver(),
                            Settings.Secure.ANDROID_ID);
                }
            }

            if (array != null && array.length() > 0) {
                final String fcm = FirebaseInstanceId.getInstance().getToken().toString();
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                String URL = ApiInterface.URL_BASE + ApiInterface.CHILD_APP_LIST;
                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String strDate = sdf.format(c.getTime());
                String deviceId = Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                SharedPreferences sharedPref = getSharedPreferences("login", 0);
                String ucode = sharedPref.getString("uniquecode", null);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("unique_id", ucode);
                jsonBody.put("fcm_token", fcm);
                jsonBody.put("command", "sendConfigurationForKidsApp");
                jsonBody.put("imei", imei);
                jsonBody.put("deviceid", deviceId);
                jsonBody.put("app_list", array.toString());
                jsonBody.put("timestamp", strDate);
                jsonBody.put("app_platform", "ANDROID");
                final String requestBody = jsonBody.toString();

                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getChildDetails(list);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<String, String>();
                        //headers.put("Authorization", fcm);
                        headers.put("authorizationToken", ucode);
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return requestBody == null ? null : requestBody.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                            return null;
                        }
                    }

                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        String responseString = "";
                        if (response != null) {
                            responseString = String.valueOf(response.statusCode);
                            // can get more details such as response.headers
                        }
                        return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                    }
                };

                requestQueue.add(stringRequest);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void UploadImage() {
        try {
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String URL = ApiInterface.UPLOAD_IMAGE;
            StringRequest request = new StringRequest(Request.Method.POST, URL, new com.android.volley.Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject api_response = new JSONObject(response);
                        String message = api_response.getString("Message");
                        //Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {

                    }
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
                }
            }) {

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    for (int i = 0; i < arr.size(); i++) {
                        params.put(arr.get(i), arr1.get(i));
                    }
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "multipart/form-data");
                    return headers;
                }
            };
            queue.add(request);
        } catch (Exception e) {

        }
    }

    public void getChildDetails(List<CommLockInfo> list) {
        try {

            String URL = ApiInterface.URL_BASE + ApiInterface.GET_CHILD_DETAILS;
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("login", 0);
            String uniquecode = sharedPref.getString("uniquecode", null);
            Date date = new Date();
            String modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("unique_code", uniquecode);
            jsonBody.put("usage_date", modifiedDate);
            jsonBody.put("command", "getChildDetails");

            final String requestBody = jsonBody.toString();
            StringRequest request = new StringRequest(Request.Method.POST, URL, new com.android.volley.Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response != null) {
                        try {
                            JSONArray arrRsp = new JSONArray(response);
                            String totalusage = null;
                            for (int i = 0; i < arrRsp.length(); i++) {
                                JSONObject object = arrRsp.getJSONObject(i);
                                String parental_controlid = object.getString("parental_control_id");
                                SharedPreferences msharedpref = getApplicationContext().getSharedPreferences("login",
                                        Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = msharedpref.edit();
                                editor.putString("parental_controlid", parental_controlid);
                                editor.commit();
                                String details = object.getString("details");
                                JSONObject json = new JSONObject(details);
                                totalusage = json.getString("total_usage_reaming").toString();

                            }
                            if (totalusage != null && list != null) {
                                int nAppCount = list.size();
                                if (nAppCount > 0) {
                                    if (totalusage.equalsIgnoreCase("Daily Limit Reached")) {
                                        setDailyLimit(list, nAppCount);
                                    } else {
                                        for (CommLockInfo info : list) {
                                            mLockInfoManager.unlockCommApplication(info.getPackageName());
                                            info.setLocked(false);
                                        }
                                        setAppLimit(list, nAppCount);
                                    }
                                }
                            }
                            //finish();
                        } catch (Exception e) {
                        }
                    }
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //Toast.makeText(getActivity(), "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("authorizationToken", uniquecode);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };
            queue.add(request);
        } catch (Exception e) {
        }
    }

    private boolean setAppUnlock(@NonNull CommLockInfo info) {
        String strPackageName = info.getPackageName().toString();
        if(!Rule.isPackageNetworkBlocked(strPackageName, getApplicationContext())) {
            return false;
        }

        info.setLocked(false);
        mLockInfoManager.unlockCommApplication(strPackageName);
        Rule.unblockPackageNetwork(strPackageName, getApplicationContext());

        Log.d("Daniel Debug", "setAppUnlock - Unlocked : " + strPackageName);

        return true;
    }

    private boolean setAppLock(@NonNull CommLockInfo info) {
        boolean bChanged = false;
        Boolean result = false;
        String strAppName;
        String strPackageName;

        strPackageName = info.getPackageName().toString();
        strAppName = info.getAppName().toUpperCase().toString();

        if (!strAppName.isEmpty()) {
            if (strAppName.contains("DIALER")) {
                result = true;
            } else if (strAppName.contains("PHONE")) {
                result = true;
            } else if (strAppName.contains("CONTACT")) {
                result = true;
            } else if (strAppName.contains("SETTING")) {
                result = true;
            } else if (strAppName.contains("GOOGLE")) {
                result = true;
//                } else if (strAppName.contains("PLAY STORE")) {
//                    result = true;
            } else if (strAppName.contains("JEXTREAM")) {
                result = true;
            }
        }

        if (!strPackageName.isEmpty()) {
            if (result) {
                // Unlock as a specified app like system
                if(Rule.isPackageNetworkBlocked(strPackageName, getApplicationContext())) {
                    bChanged = true;

                    mLockInfoManager.unlockCommApplication(strPackageName);
                    info.setLocked(false);

                    Rule.unblockPackageNetwork(strPackageName, getApplicationContext());
                }
            } else {
                if(!Rule.isPackageNetworkBlocked(strPackageName, getApplicationContext())) {
                    bChanged = true;

                    mLockInfoManager.lockCommApplication(strPackageName);
                    info.setLocked(true);

                    Rule.blockPackageNetwork(strPackageName, getApplicationContext());

                    //FIXME: TEST MESSAGE
                    Toast.makeText(getApplicationContext(), info.getAppName() + "is blocked by parent.", Toast.LENGTH_LONG).show();
                }

                Log.d("Daniel Debug", "setAppLock - Locked : " + strPackageName);
            }
        }

        return bChanged;
    }

    private void closeForegroundApp(String strPackageName) {
            /* Not working right now.....

            Log.d("Daniel Debug", "closeForegroundApp Start");

            boolean closedApp = false;

            ActivityManager activityManager = (ActivityManager) getSystemService( Activity.ACTIVITY_SERVICE );
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();

            // First trial to close the foreground app
            for(ActivityManager.RunningAppProcessInfo appProcess : appProcesses){
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                    if (appProcess.processName.equalsIgnoreCase("com.android.launcher") ||
                        appProcess.processName.equalsIgnoreCase("com.franklinwireless.android.jexkids"))
                        continue;
                    if (appProcess.processName.equalsIgnoreCase(strPackageName)) {
                        Log.d("Daniel Debug", "Closed App 1st: " + appProcess.processName + " " + Long.toString(appProcess.pid));
                        android.os.Process.killProcess(appProcess.pid);
                        closedApp = true;
                    }
                }
            }
            if (!closedApp) {
                // Second trial to close or restart PackageName
                activityManager.restartPackage(strPackageName);
                // activityManager.killBackgroundProcesses(strPackageName);
                Log.d("Daniel Debug", "Closed App 2nd: " + strPackageName);
            }

            Log.d("Daniel Debug", "closeForegroundApp End");
             */
    }

    private void setDailyLimit(List<CommLockInfo> list, int nAppCount) {
        Log.d("Daniel Debug", "setDailyLimit Start");

        boolean bShouldReloadService = false;
        for (CommLockInfo info : list) {
            if(setAppLock(info)) {
                bShouldReloadService = true;
            }
        }

        if(bShouldReloadService) {
           ServiceSinkhole.reload("rule changed", APIService.this, false);
        }

        Log.d("Daniel Debug", "setDailyLimit End");
    }

    private void setAppLimit(List<CommLockInfo> list, int nAppCount) {
        try {
            Log.d("Daniel Debug", "setAppLimit Start");

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String URL = ApiInterface.URL_BASE + ApiInterface.APP_CONFIGURATION;
            String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("login", 0);
            String uniquecode = sharedPref.getString("uniquecode", null);
            String parental_controlid = sharedPref.getString("parental_controlid", null);
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("unique_id", uniquecode);
            jsonBody.put("command", "getAppsLimitList");
            jsonBody.put("parental_control_id", parental_controlid);
            jsonBody.put("deviceid", deviceId);
            jsonBody.put("app_platform", "ANDROID");
            //jsonBody.put("AuthorizationToken", deviceId);
            final String requestBody = jsonBody.toString();

            StringRequest request = new StringRequest(Request.Method.POST, URL, new com.android.volley.Response.Listener<String>() {

                //@RequiresApi(api=Build.VERSION_CODES.O)
                @Override
                public void onResponse(String response) {
                    boolean bShouldReloadService = false;

                    List<String> checkPackagesBlocked = Rule.getPackagesBlocked();

                    try {
                        // Response to get the AppLimit data
                        String responseUtf8 = new String(response.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                        JSONArray arrRsp = new JSONArray(responseUtf8);
                        int nOneTime = 0;
                        for (int i = 0; i < arrRsp.length(); i++) {
                            JSONObject object = arrRsp.getJSONObject(i);
                            String strAppRemainingTime = "Nothing";
                            String strAppStatus = "Nothing";
                            String strAppName = "Nothing";

                            if (object == null && object.length() == 0) {
                                Log.d("Daniel Debug", "object length is 0!!");
                                continue;
                            }
                            if (object.has("app_name")) {
                                strAppName = object.getString("app_name");
                            }
                            if (object.has("app_remaining_time")) {
                                strAppRemainingTime = object.getString("app_remaining_time");
                            }
                            if (object.has("app_status")) {
                                strAppStatus = object.getString("app_status");
                            }
                            Log.d("Daniel Debug", "Read Object " + strAppRemainingTime + " " + strAppStatus + " " + strAppName);

                            if (nOneTime == 1) {
                                break;
                            }

                            for (CommLockInfo info : list) {
                                // Checking the Daily limit
                                if (strAppRemainingTime.equals("Data Limit Reached")) {
                                    Log.d("Danielf Debug", "Data Limit Reached");

                                    checkPackagesBlocked.clear();

                                    if(setAppLock(info)) {
                                        bShouldReloadService = true;
                                    }
                                    nOneTime = 1;
                                } else {
                                    // Checking the App limit only
                                    String strPackageName = info.getPackageName().toString();

                                    if (strPackageName.isEmpty() || !info.getAppName().equalsIgnoreCase(strAppName)) {
                                        continue;
                                    }

                                    checkPackagesBlocked.remove(strPackageName);

                                    if (strAppStatus.equals("BLOCKED")) {
                                        closeForegroundApp(strPackageName);

                                        if(setAppLock(info)) {
                                            bShouldReloadService = true;
                                        }

                                        Log.d("Daniel Debug", "App blocked " + strPackageName);
                                    } else if (strAppStatus.equals("ACTIVE") && !usages.isEmpty()) {
                                        long nAppUsedTime = usages.get(strPackageName).getLong("usage_time");
                                        long nAppTotalTimeLimit = object.getLong("time_in_total_seconds");

                                        Log.d("Daniel Debug", "nAppUsedTime : " + Long.toString(nAppUsedTime) + " nAppTotalTimeLimit : " + Long.toString(nAppTotalTimeLimit));

                                        if (nAppUsedTime > nAppTotalTimeLimit) {
                                            Log.d("Daniel Debug", "App limited " + strPackageName);
                                            closeForegroundApp(strPackageName);
                                            if(setAppLock(info)) {
                                                bShouldReloadService = true;
                                            }
                                        }else {
                                            if(setAppUnlock(info)) {
                                                bShouldReloadService = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        boolean bHasUnlockedService = checkUnblockedPackages(checkPackagesBlocked, list);

                        if(bShouldReloadService || bHasUnlockedService) {
                            ServiceSinkhole.reload("rule changed", APIService.this, false);
                        }

                        //Toast.makeText(getActivity(), list.get(0).getAppName()+","+syslist.get(1).blocked, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        // 06-30-2023 Daniel : Due to the error message as follows, it needs to hold up until the server will fix this issue
                        //                     Once the app just set up the daily limit only, the server deliver the error message,
                        //                     which means that the daily limit will not work.
                        //                     {"errorType":"Error","errorMessage":"No app limits exists!!","trace":["Error: No app limits exists!!",
                        // e.printStackTrace();

                        JSONObject object = null;
                        try {
                            object = new JSONObject(response);

                            String errorType = object.getString("errorType");
                            String errorMessage = object.getString("errorMessage");
                            if(errorType.equals("Error") && errorMessage.startsWith("No app limits exists")) {
                                bShouldReloadService = checkUnblockedPackages(checkPackagesBlocked, list);

                                if(bShouldReloadService) {
                                    ServiceSinkhole.reload("rule changed", APIService.this, false);
                                }
                            }
                        } catch (JSONException ex) {
                            //Do thing
                        }
                    }
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    //headers.put("Authorization", fcm);
                    headers.put("AuthorizationToken", uniquecode);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };
            queue.add(request);
        } catch (Exception e) {

        }
        Log.d("Daniel Debug", "setAppLimit End");
    }

    private boolean checkUnblockedPackages(List<String> checkPackagesBlocked, List<CommLockInfo> list) {
        boolean bShouldReloadService = false;

        for(String packageName : checkPackagesBlocked) {
            CommLockInfo found = null;
            for(CommLockInfo info : list) {
                if(info.getPackageName().equals(packageName)) {
                    found = info;
                    break;
                }
            }

            if(found != null && setAppUnlock(found)) {
                bShouldReloadService = true;
            }
        }
        return bShouldReloadService;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        SharedPreferences msharedpref = getApplicationContext().getSharedPreferences("login",
                Context.MODE_PRIVATE);
        Long lTime = msharedpref.getLong("LAST_UPLOAD_EXECUTED", 0);
        Long currTime = System.currentTimeMillis();
        if (lTime == 0 || (currTime - lTime) > 50000) {
            mLockMainPresenter = new LockMainPresenter(this, getApplicationContext());
            mLockMainPresenter.loadAppInfo(getApplicationContext());
            msharedpref.edit().putLong("LAST_UPLOAD_EXECUTED", currTime).commit();
        } else {
            this.stopSelf();
        }
    }
}