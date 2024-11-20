package com.franklinwireless.android.jexkids.locker.services;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.franklinwireless.android.jexkids.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

public class APIWorker extends Worker {

    private NotificationManager notificationManager;
    private static final String NOTIFICATION_CHANNEL_ID = "1227";

    public APIWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        notificationManager = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
    }

    @NonNull
    private ForegroundInfo createForegroundInfo(@NonNull String progress) {
        // Build a notification using bytesRead and contentLength

        Context context = getApplicationContext();
        String id = "1227";
        String title = "JexKids";
        // This PendingIntent can be used to cancel the worker
        PendingIntent intent = WorkManager.getInstance(context)
                .createCancelPendingIntent(getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }

        Notification notification = new NotificationCompat.Builder(context, id)
                .setContentTitle(progress)
                .setTicker(title)
                .setSmallIcon(R.drawable.jexkidslogo)
                .setOngoing(true)
                // Add the cancel action to the notification which can
                // be used to cancel the worker if required
                .build();

        return new ForegroundInfo(1227, notification);
    }

    @NonNull
    @Override
    public ForegroundInfo getForegroundInfo() {
        Context context = getApplicationContext();
        String id = "1227";
        String title = "JexKids";
        // This PendingIntent can be used to cancel the worker
        PendingIntent intent = WorkManager.getInstance(context)
                .createCancelPendingIntent(getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }

        Notification notification = new NotificationCompat.Builder(context, id)
                .setContentTitle("JexKids App Running")
                .setTicker(title)
                .setSmallIcon(R.drawable.jexkidslogo)
                .setOngoing(true)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                // Add the cancel action to the notification which can
                // be used to cancel the worker if required
                .build();

        return new ForegroundInfo(1227, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        Context context = getApplicationContext();
        String id = "1227";
        String title = "JexKids";
        // Create a Notification channel
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "App lock background task ", importance);
        notificationManager.createNotificationChannel(notificationChannel);
        Notification notification = new NotificationCompat.Builder(context, id)
                .setContentTitle("JexKids App Running")
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
        String progress = "JexKids App Running";
        setForegroundAsync(createForegroundInfo(progress));
        Timer mTimer = new Timer();
        CountDownLatch countDownLatch = new CountDownLatch(3);
//        getApplicationContext().stopService(new Intent(getApplicationContext(),APIService.class));
//        getApplicationContext().startService(new Intent(getApplicationContext(),APIService.class));
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getApplicationContext().stopService(new Intent(getApplicationContext(),APIService.class));
                getApplicationContext().startService(new Intent(getApplicationContext(),APIService.class));
                countDownLatch.countDown();
            }
        }, 0, 60000); //60000*5
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Result.success();
    }
}

