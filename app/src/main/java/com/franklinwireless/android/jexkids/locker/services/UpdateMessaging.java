package com.franklinwireless.android.jexkids.locker.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import com.franklinwireless.android.jexkids.home.Homepage;
import com.franklinwireless.android.jexkids.R;
//import com.franklinwireless.android.jexkids.adapters.MainAdapter;
import com.franklinwireless.android.jexkids.home.ParentControl;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class UpdateMessaging extends FirebaseMessagingService{//implements LockMainContract.View
    //public LockMainPresenter mLockMainPresenter;
    //public CommLockInfoManager mLockInfoManager;
    //MainActivity mainActivity;
    public UpdateMessaging() {
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //super.onMessageReceived(remoteMessage);
        if(remoteMessage.getData()!=null) {
            //String id = remoteMessage.getNotification().getChannelId();
            //showNotification( remoteMessage.getData().get("body"), remoteMessage.getData().get("title"),"fcm_default_channel");
            /*mLockMainPresenter = new LockMainPresenter(this, getApplicationContext());
            mLockInfoManager = new CommLockInfoManager(getApplicationContext());
            mLockMainPresenter.loadAppInfo(getApplicationContext());*/
            //Toast.makeText(this, remoteMessage.getData().get("title"), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void handleIntent(Intent intent) {
        // 06-28-2023 Daniel : Disabled the firebase notification during the app is in background.
        //super.handleIntent(intent);
        if(intent.getExtras()!=null)
        {
            String title = intent.getStringExtra("gcm.notification.title");
            String body = intent.getStringExtra("gcm.notification.body");
            if (title!=null && body!=null) {
                // 06-29-2023 Daniel : Disabled the "UPDATE CONFIGURATION" notification
                if (!title.equalsIgnoreCase("configuration")) {
                    showNotification(body, title, "fcm_default_channel");
                }
            }
        }
    }

    private void showNotification(String message, String title, String id) {

        Intent i=new Intent(this, Homepage.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_IMMUTABLE);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int importance = NotificationManager.IMPORTANCE_HIGH;


        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
            Notification notification = notificationBuilder
                    .setAutoCancel(true)
                    .setContentTitle(title).setContentText(message)
                    .setSmallIcon(R.drawable.logo_splash)
                    .setChannelId("fcm_default_channel")
                    .setAutoCancel(true)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setContentIntent(pendingIntent)
                    .build();
            notificationManager.notify(1, notification);

        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N | Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, id);
            Notification notification = notificationBuilder
                    .setAutoCancel(true)
                    .setContentTitle(title).setContentText(message)
                    .setSmallIcon(R.drawable.logo_splash)
                    .setChannelId("fcm_default_channel")
                    .setAutoCancel(true)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setContentIntent(pendingIntent)
                    .build();
            notificationManager.notify(1, notification);
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startService(new Intent(this, APIService.class));
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,id)
                    .setAutoCancel(true)
                    .setContentTitle(title).setContentText(message)
                    .setSmallIcon(R.drawable.logo_splash)
                    .setChannelId("fcm_default_channel")
                    .setAutoCancel(true)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setContentIntent(pendingIntent);

            NotificationChannel notificationChannel = new NotificationChannel(id, "Update Configuration", importance);
            //NotificationChannel notificationChannel = new NotificationChannel(id, message, importance);
            notificationManager.createNotificationChannel(notificationChannel);
            notificationManager.notify(1, builder.build());
            //mContext.startForeground(145,builder.build());
        }
        //NotificationManagerCompat.from(this).cancelAll();
        //Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /*@Override
    public void loadAppInfoSuccess(List<CommLockInfo> list) {
        try {
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String URL = ApiInterface.URL_BASE+ApiInterface.APP_CONFIGURATION;

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("unique_id", "103132");
            final String requestBody = jsonBody.toString();
            StringRequest request = new StringRequest(Request.Method.POST, URL, new com.android.volley.Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    LocalDate date = LocalDate.now();
                    DayOfWeek dow = date.getDayOfWeek();
                    String currentdate= dow.toString().substring(0, 3);
                    try {
                        JSONArray array = new JSONArray(response);
                        for (CommLockInfo info : list) {
                            int j=0;
                            for(int i=0; i < array.length(); i++)
                            {
                                if(j==0) {
                                        JSONObject object = array.getJSONObject(i);
                                        String app_name = object.getString("app_name");
                                        if (info.getAppName().equalsIgnoreCase(app_name)) {
                                            boolean bornot = object.getBoolean("is_blocked");
                                            if(bornot)
                                            {
                                                mLockInfoManager.lockCommApplication(info.getPackageName());
                                            }
                                            else
                                            {
                                                JSONArray ja = object.getJSONArray("week_days");
                                                int len = ja.length();
                                                for(int k=0;k<len;k++)
                                                {
                                                    String json = ja.get(k).toString();
                                                    if(currentdate.equalsIgnoreCase(json))
                                                    {
                                                        mLockInfoManager.lockCommApplication(info.getPackageName());
                                                    }
                                                }
                                            }

                                        }
                                        else if(i==(array.length()-1) && j==0)
                                        {
                                            mLockInfoManager.lockCommApplication(info.getPackageName());
                                        }

                                }

                            }
                        }
                        //Toast.makeText(getActivity(), list.get(0).getAppName()+","+syslist.get(1).blocked, Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Fail to get response = " + error, Toast.LENGTH_SHORT).show();
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
                    headers.put("authorizationToken", "103132");
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };
            queue.add(request);
        }
        catch (Exception e)
        {

        }
    }*/
}