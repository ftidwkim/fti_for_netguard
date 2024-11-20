package com.franklinwireless.android.jexkids.locker.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.franklinwireless.android.jexkids.MyApp
import com.franklinwireless.android.jexkids.locker.base.AppConstants
import com.franklinwireless.android.jexkids.locker.db.CommLockInfoManager
import com.franklinwireless.android.jexkids.locker.protect.UnlockAppActivity


class LockReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val msharedpref = context.getSharedPreferences(
            "login",
            Context.MODE_PRIVATE
        )

        val packageName = msharedpref.getString("LAST_OPENED_APP", "")
        if (packageName != null && !packageName.isEmpty()) {
            val mLockInfoManager = CommLockInfoManager(context)
            if (getHomes(packageName,context).contains(packageName) || packageName.contains("launcher")) {
                mLockInfoManager.unlockCommApplication(packageName);
            } else {
                if (mLockInfoManager.isLockedPackageName(packageName)) {
                    var appName = "App";
                    try {
                        val packageManager = context.packageManager;
                        var appInfo: ApplicationInfo
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            appInfo = packageManager
                                .getApplicationInfo(
                                    packageName,
                                    PackageManager.ApplicationInfoFlags.of(0)
                                )
                        } else {
                            appInfo = packageManager
                                .getApplicationInfo(
                                    packageName,
                                    PackageManager.MATCH_UNINSTALLED_PACKAGES
                                )
                        }
                        appName = packageManager.getApplicationLabel(appInfo).toString();
                        passwordLock(packageName, appName, context)
                    } catch (e: Exception) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private fun passwordLock(packageName: String, appName: String, context: Context) {
//        Data.Builder data = new Data.Builder();
////Add parameter in Data class. just like bundle. You can also add Boolean and Number in parameter.
//        data.putString("packageName", packageName);
//        data.putString("appName", appName);
        MyApp.getInstance().clearAllActivity()
        val intent = Intent(context, UnlockAppActivity::class.java)
        intent.putExtra(AppConstants.LOCK_PACKAGE_NAME, packageName)
        intent.putExtra(AppConstants.LOCK_FROM, AppConstants.LOCK_FROM_FINISH)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private fun getHomes(packageName:String, context: Context): List<String> {
        val names: MutableList<String> = ArrayList()
        val launcherIntent: Intent? =
            context.packageManager.getLaunchIntentForPackage(packageName)
        if (launcherIntent != null) {
            if (launcherIntent.hasCategory(Intent.CATEGORY_HOME)) {
                names.add(packageName)
            }
        }
        val packageManager: PackageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        for (ri in resolveInfo) {
            names.add(ri.activityInfo.packageName)
        }
        return names
    }

}