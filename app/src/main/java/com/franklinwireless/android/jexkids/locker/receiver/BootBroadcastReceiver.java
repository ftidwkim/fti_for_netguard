package com.franklinwireless.android.jexkids.locker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;

import com.franklinwireless.android.jexkids.locker.base.AppConstants;
import com.franklinwireless.android.jexkids.locker.services.BackgroundManager;
import com.franklinwireless.android.jexkids.locker.services.LoadAppListService;
import com.franklinwireless.android.jexkids.locker.services.LockService;
import com.franklinwireless.android.jexkids.locker.utils.LogUtil;
import com.franklinwireless.android.jexkids.locker.utils.MainUtil;


public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(@NonNull Context context, Intent intent) {
        LogUtil.i("Boot service....");
        //TODO: pie compatable done
        BackgroundManager.getInstance().init(context).startService(LoadAppListService.class);
        if (MainUtil.getInstance().getBoolean(AppConstants.LOCK_STATE, false)) {
//            BackgroundManager.getInstance().init(context).startService(LockService.class);
            BackgroundManager.getInstance().init(context).startAlarmManager();
        }
    }
}
