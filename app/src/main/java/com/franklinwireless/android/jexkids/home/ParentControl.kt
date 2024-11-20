package com.franklinwireless.android.jexkids.home

import android.Manifest
import android.app.AppOpsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.franklinwireless.android.jexkids.R
import com.franklinwireless.android.jexkids.locker.base.AppConstants
import com.franklinwireless.android.jexkids.locker.db.CommLockInfoManager
import com.franklinwireless.android.jexkids.locker.gesture.presenter.LockMainPresenter
import com.franklinwireless.android.jexkids.locker.model.CommLockInfo
import com.franklinwireless.android.jexkids.locker.services.APIService
import com.franklinwireless.android.jexkids.locker.services.BackgroundManager
import com.franklinwireless.android.jexkids.locker.services.LoadAppListService
//import com.franklinwireless.android.jexkids.locker.services.LockService
import com.franklinwireless.android.jexkids.locker.utils.MainUtil
import org.json.JSONArray
import kotlin.system.exitProcess

class ParentControl : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 1
    private var data: List<CommLockInfo>? = null
    var array: JSONArray? = JSONArray()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parent_control)

        MainUtil.getInstance().putBoolean(AppConstants.LOCK_STATE,true)
        MainUtil.getInstance().putBoolean(AppConstants.LOCK_AUTO_SCREEN, true)
        MainUtil.getInstance().putBoolean(AppConstants.LOCK_IS_HIDE_LINE, true)

        MainUtil.getInstance().putLong(AppConstants.LOCK_APART_MILLISECONDS, 0L)
        MainUtil.getInstance().putBoolean(AppConstants.LOCK_AUTO_SCREEN_TIME, false)

//        startService(Intent(this, APIService::class.java))
//        if (!BackgroundManager.getInstance().init(this).isServiceRunning(LockService::class.java)) {
//            BackgroundManager.getInstance().init(this).stopService(LockService::class.java)
//            BackgroundManager.getInstance().init(this).startService(LockService::class.java)
//        }
        BackgroundManager.getInstance().init(this).startAlarmManager()

        if(checkPermission()){
            BackgroundManager.getInstance().init(applicationContext).startService(LoadAppListService::class.java)
            //TelephonyManager telephonyManager = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            //imei = telephonyManager.getImei();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            } else {
            }
        }else{
            requestPermission();
        }
        if(checkPermission1())
        {

        }
        data = ArrayList()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        }
    }
    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.READ_PHONE_STATE
            )
        ) {
            Toast.makeText(
                applicationContext,
                "Give permission to check whether internet is of or on.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun checkPermission(): Boolean {
        //val result = ContextCompat.checkSelfPermission(
        //   applicationContext,
        //  Manifest.permission.READ_PHONE_STATE
        //)
        //return result == PackageManager.PERMISSION_GRANTED
        var granted = false
        var context = applicationContext
        val appOps = context
            .getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            "android:get_usage_stats",
            Process.myUid(), context.packageName
        )
        granted = if (mode == AppOpsManager.MODE_DEFAULT) {
            context.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) === PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }
        return granted
    }
    private fun checkPermission1(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.READ_PHONE_STATE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }
    private  fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flFragment,fragment)
        transaction.commit()
    }
    override fun onBackPressed() {
        finish();
        exitProcess(0);
    }
}