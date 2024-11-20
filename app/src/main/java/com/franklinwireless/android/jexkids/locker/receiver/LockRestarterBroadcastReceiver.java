package com.franklinwireless.android.jexkids.locker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.franklinwireless.android.jexkids.locker.base.AppConstants;
import com.franklinwireless.android.jexkids.locker.services.BackgroundManager;
import com.franklinwireless.android.jexkids.locker.services.LockService;
import com.franklinwireless.android.jexkids.locker.utils.MainUtil;

public class LockRestarterBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean lockState= MainUtil.getInstance().getBoolean(AppConstants.LOCK_STATE);
        if (intent != null && lockState) {
            String type = intent.getStringExtra("type");
            if (type.contentEquals("lockservice")) {
//                BackgroundManager.getInstance().init(context).startService(LockService.class);
            }
            else if (type.contentEquals("startlockserviceFromAM")) {
//                if (!BackgroundManager.getInstance().init(context).isServiceRunning(LockService.class)) {
//                    BackgroundManager.getInstance().init(context).startService(LockService.class);
//                }
                BackgroundManager.getInstance().init(context).startAlarmManager();
            }
        }
    }
}
