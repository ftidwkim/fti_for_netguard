package com.franklinwireless.android.jexkids.home

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.franklinwireless.android.jexkids.R
import com.franklinwireless.android.jexkids.locker.base.AppConstants
import com.franklinwireless.android.jexkids.locker.db.CommLockInfoManager
import com.franklinwireless.android.jexkids.locker.gesture.presenter.LockMainPresenter
import com.franklinwireless.android.jexkids.locker.model.CommLockInfo
import com.franklinwireless.android.jexkids.locker.services.APIWorker
import com.franklinwireless.android.jexkids.locker.services.BackgroundManager
import com.franklinwireless.android.jexkids.locker.services.LoadAppListService
import com.franklinwireless.android.jexkids.locker.utils.MainUtil
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import eu.faircode.netguard.ReceiverAutostart
import eu.faircode.netguard.ServiceSinkhole
import eu.faircode.netguard.Util
import org.json.JSONArray
import java.util.Locale
import java.util.concurrent.TimeUnit


class Homepage : AppCompatActivity() {

    /* NetGuard Stuffs */
    private val TAG = "HOMEPAGE"
    private var running = false
    var dialogVpn: AlertDialog? = null
    private var dialogDoze: AlertDialog? = null
    private var bInitialized = false

    companion object {
        public val REQUEST_VPN = 1
        public val REQUEST_INVITE = 2
        public val REQUEST_LOGCAT = 3
        public val REQUEST_ROAMING = 4
    }


    lateinit var bottomNav : BottomNavigationView
    private val RESULT_ACTION_USAGE_ACCESS_SETTINGS = 1
    private val PERMISSION_REQUEST_CODE = 1
    private var mLockMainPresenter: LockMainPresenter? = null
    private var data: List<CommLockInfo>? = null
    private var csize=0
    private val mLockInfoManager: CommLockInfoManager? = null
    var array: JSONArray? = JSONArray()
    var imei: String? = null
    var arr = ArrayList<String>()
    var arr1 = ArrayList<String>()
    var mProgressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(applicationContext)
        //TODO: pie compatable done
        //BackgroundManager.getInstance().init(applicationContext).startService(LoadAppListService::class.java)
        setContentView(R.layout.activity_homepage)
        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val language = sharedPreferences.getString("My_Lang", null)

        if (language != null) {
            if(language.toString().equals("en"))
            {
                setLocale(this,"en")
            }
            else if(language.toString().equals("es"))
            {
                setLocale(this,"es")
            }
            else if(language.toString().equals("ko"))
            {
                setLocale(this,"ko")
            }
        }

        MainUtil.getInstance().putBoolean(AppConstants.LOCK_STATE,true)
        MainUtil.getInstance().putBoolean(AppConstants.LOCK_AUTO_SCREEN, true)
        MainUtil.getInstance().putBoolean(AppConstants.LOCK_IS_HIDE_LINE, true)

        MainUtil.getInstance().putLong(AppConstants.LOCK_APART_MILLISECONDS, 0L)
        MainUtil.getInstance().putBoolean(AppConstants.LOCK_AUTO_SCREEN_TIME, false)

        // Get state

        goto_APIWorker();
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

        loadFragment(home())
        bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    loadFragment(home())
                    true
                }
                R.id.active -> {
                    Toast.makeText(applicationContext,"Coming Soon",Toast.LENGTH_LONG).show()
                    //loadFragment(active())
                    true
                }
                R.id.pending -> {
                    Toast.makeText(applicationContext,"Coming Soon",Toast.LENGTH_LONG).show()
                    //loadFragment(Pending())
                    true
                }
                R.id.claim -> {
                    Toast.makeText(applicationContext,"Coming Soon",Toast.LENGTH_LONG).show()
                    //loadFragment(claim())
                    true
                }
                else -> {
                    Toast.makeText(applicationContext,"Coming Soon",Toast.LENGTH_LONG).show()
                    //loadFragment(History())
                    true}
            }
        }

        running = true
    }

    override fun onResume() {
        super.onResume()
        if(bInitialized) {
            return;
        }
        bInitialized = true;

        prepareVpn()
    }

    private fun goto_APIWorker()
    {
        val request = OneTimeWorkRequestBuilder<APIWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        WorkManager.getInstance(applicationContext).enqueue(request)
//
        val constraints: Constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val periodicWorkRequest = PeriodicWorkRequest.Builder(
            APIWorker::class.java, 15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        val workManager = WorkManager.getInstance(applicationContext)
        workManager.enqueueUniquePeriodicWork(
            "APISERVICE",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }

    private fun setLocale(activity: Activity, languageCode: String?) {
        val locale = Locale(languageCode?:"")
        Locale.setDefault(locale)
        val resources = activity.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
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
    }

    private fun prepareVpn() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit().putBoolean("enabled", true).apply();

        // Upgrade
        val initialized = prefs.getBoolean("initialized", false)
        ReceiverAutostart.upgrade(initialized, this)

        try {
            val alwaysOn = Settings.Secure.getString(contentResolver, "always_on_vpn_app")
            Log.i(TAG, "Always-on=$alwaysOn")
            if (!TextUtils.isEmpty(alwaysOn)) if (packageName == alwaysOn) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                    prefs.getBoolean("filter", false)
                ) {
                    val lockdown =
                        Settings.Secure.getInt(contentResolver, "always_on_vpn_lockdown", 0)
                    Log.i(TAG, "Lockdown=$lockdown")
                    if (lockdown != 0) {
                        Toast.makeText(
                            this,
                            R.string.msg_always_on_lockdown,
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }
                }
            } else {
                Toast.makeText(this, R.string.msg_always_on, Toast.LENGTH_LONG).show()
                return
            }
        } catch (ex: Throwable) {
            Log.e(
                TAG, """
                 $ex
                 ${Log.getStackTraceString(ex)}
                 """.trimIndent()
            )
        }

        val filter: Boolean = prefs.getBoolean("filter", false)
        if (filter && Util.isPrivateDns(this)) Toast.makeText(
            this,
            R.string.msg_private_dns,
            Toast.LENGTH_LONG
        ).show()

        try {
            val prepare = VpnService.prepare(this)
            if (prepare == null) {
                Log.i(TAG, "Prepare done")
                onActivityResult(Homepage.REQUEST_VPN, RESULT_OK, null)

            } else {
                // Show dialog
                val inflater = LayoutInflater.from(this)
                val view = inflater.inflate(R.layout.vpn, null, false)
                dialogVpn = AlertDialog.Builder(this)
                    .setView(view)
                    .setCancelable(false)
                    .setPositiveButton(
                        android.R.string.yes
                    ) { dialog, which ->
                        if (running) {
                            Log.i(TAG, "Start intent=$prepare")
                            try {
                                // com.android.vpndialogs.ConfirmDialog required
                                startActivityForResult(prepare, Homepage.REQUEST_VPN)
                            } catch (ex: Throwable) {
                                Log.e(
                                    TAG,
                                    """
                                    $ex
                                    ${Log.getStackTraceString(ex)}
                                    """.trimIndent()
                                )
                                onActivityResult(Homepage.REQUEST_VPN, RESULT_CANCELED, null)
                                prefs.edit().putBoolean("enabled", false).apply()
                            }
                        }
                    }
                    .setOnDismissListener { dialogVpn = null }
                    .create()

                dialogVpn?.show()
            }
        } catch (ex: Throwable) {
            // Prepare failed
            Log.e(
                TAG, """
                 $ex
                 ${Log.getStackTraceString(ex)}
                 """.trimIndent()
            )
            prefs.edit().putBoolean("enabled", false).apply()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Homepage.REQUEST_VPN) {
            // Handle VPN approval
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            prefs.edit().putBoolean("enabled", resultCode == RESULT_OK).apply()
            if (resultCode == RESULT_OK) {
                ServiceSinkhole.start("prepared", this)

                //FIXME: Notify user with JexKids's message
//                val on = Toast.makeText(this, R.string.msg_on, Toast.LENGTH_LONG)
//                on.setGravity(Gravity.CENTER, 0, 0)
//                on.show()

                checkDoze()

            } else if (resultCode == RESULT_CANCELED) Toast.makeText(
                this,
                R.string.msg_vpn_cancelled,
                Toast.LENGTH_LONG
            ).show()
        } else if (requestCode == Homepage.REQUEST_INVITE) {
            // Do nothing
        } else if (requestCode == Homepage.REQUEST_LOGCAT) {
            // Send logcat by e-mail
            if (resultCode == RESULT_OK) {
                var target = data!!.data
                if (data!!.hasExtra("org.openintents.extra.DIR_PATH")) target =
                    Uri.parse(target.toString() + "/logcat.txt")
                Log.i(TAG, "Export URI=$target")
                Util.sendLogcat(target, this)
            }
        } else {
            Log.w(TAG, "Unknown activity result request=$requestCode")
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    private fun checkDoze() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val doze = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            if (Util.batteryOptimizing(this) && packageManager.resolveActivity(doze, 0) != null) {
                val prefs = PreferenceManager.getDefaultSharedPreferences(this)
                if (!prefs.getBoolean("nodoze", false)) {
                    val inflater = LayoutInflater.from(this)
                    val view = inflater.inflate(R.layout.doze, null, false)
                    val cbDontAsk = view.findViewById<CheckBox>(R.id.cbDontAsk)
                    dialogDoze = AlertDialog.Builder(this)
                        .setView(view)
                        .setCancelable(true)
                        .setPositiveButton(
                            android.R.string.yes
                        ) { dialog, which ->
                            prefs.edit().putBoolean("nodoze", cbDontAsk.isChecked).apply()
                            startActivity(doze)
                        }
                        .setNegativeButton(
                            android.R.string.no
                        ) { dialog, which ->
                            prefs.edit().putBoolean("nodoze", cbDontAsk.isChecked).apply()
                        }
                        .setOnDismissListener {
                            dialogDoze = null
                            checkDataSaving()
                        }
                        .create()
                    dialogDoze?.show()
                } else checkDataSaving()
            } else checkDataSaving()
        }
    }


    private fun checkDataSaving() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val settings = Intent(
                Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS,
                Uri.parse("package:$packageName")
            )
            if (Util.dataSaving(this) && packageManager.resolveActivity(settings, 0) != null) try {
                val prefs = PreferenceManager.getDefaultSharedPreferences(this)
                if (!prefs.getBoolean("nodata", false)) {
                    val inflater = LayoutInflater.from(this)
                    val view = inflater.inflate(R.layout.datasaving, null, false)
                    val cbDontAsk = view.findViewById<CheckBox>(R.id.cbDontAsk)
                    dialogDoze = AlertDialog.Builder(this)
                        .setView(view)
                        .setCancelable(true)
                        .setPositiveButton(
                            android.R.string.yes
                        ) { dialog, which ->
                            prefs.edit().putBoolean("nodata", cbDontAsk.isChecked).apply()
                            startActivity(settings)
                        }
                        .setNegativeButton(
                            android.R.string.no
                        ) { dialog, which ->
                            prefs.edit().putBoolean("nodata", cbDontAsk.isChecked).apply()
                        }
                        .setOnDismissListener { dialogDoze = null }
                        .create()
                    dialogDoze!!.show()
                }
            } catch (ex: Throwable) {
                Log.e(
                    TAG, """
                 $ex
                 ${ex.stackTrace}
                 """.trimIndent()
                )
            }
        }
    }


//    ServiceSinkhole.stop("switch off", this , false)



}

