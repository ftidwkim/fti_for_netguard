package com.franklinwireless.android.jexkids.auth

//import com.franklinwireless.android.jexkids.locker.services.LockService
import android.Manifest
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.franklinwireless.android.jexkids.R
import com.franklinwireless.android.jexkids.home.Homepage
import com.franklinwireless.android.jexkids.locker.base.AppConstants
import com.franklinwireless.android.jexkids.locker.password.DefinePasswordActivity
import com.franklinwireless.android.jexkids.locker.permission.AppPermission
import com.franklinwireless.android.jexkids.locker.services.BackgroundManager
import com.franklinwireless.android.jexkids.locker.services.LoadAppListService
import com.franklinwireless.android.jexkids.locker.utils.LockUtil
import com.franklinwireless.android.jexkids.locker.utils.MainUtil
import com.franklinwireless.android.jexkids.locker.utils.ToastUtil
import com.franklinwireless.android.jexkids.locker.widget.PermissionDialog
import com.franklinwireless.android.jexkids.restservice.APIConstants
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import java.util.*


class MainActivity : AppCompatActivity() {
    private val RESULT_ACTION_USAGE_ACCESS_SETTINGS = 1

    var devicePolicyManager: DevicePolicyManager? = null
    var demoDeviceAdmin: ComponentName? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Logger.addLogAdapter(AndroidLogAdapter())

        /*devicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        demoDeviceAdmin = ComponentName(this, UDeviceAdmin::class.java)
        Log.e("DeviceAdminActive==", "" + demoDeviceAdmin)

        val intent =
            Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN) // adds new device administrator

        intent.putExtra(
            DevicePolicyManager.EXTRA_DEVICE_ADMIN,
            demoDeviceAdmin
        ) //ComponentName of the administrator component.

        intent.putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            "Disable app"
        ) //dditional explanation

        startActivityForResult(intent, 1)
        startService(Intent(this, BackgroundService::class.java))*/
        BackgroundManager.getInstance().init(this).startService(LoadAppListService::class.java)

        //start lock services if  everything is already  setup
//        if (MainUtil.getInstance().getBoolean(AppConstants.LOCK_STATE, false)) {
//            BackgroundManager.getInstance().init(this).startService(LockService::class.java)
//        }
        /*Handler(Looper.getMainLooper()).postDelayed({
            val isFirstLock: Boolean =

                MainUtil.getInstance().getBoolean(AppConstants.LOCK_IS_FIRST_LOCK, true)
            if (isFirstLock) {
                showDialog()
            } else {

                //startActivity(Intent(this, MainActivity::class.java))
                //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            val sharedPreference =  getSharedPreferences("login", Context.MODE_PRIVATE)
            val loginstatus = sharedPreference.getBoolean("loginstatus",false)
            if(loginstatus) {
                val mainIntent = Intent(this, Homepage::class.java)
                startActivity(mainIntent)
                finish()
            }
            else {
                val mainIntent = Intent(this, Onboarding::class.java)
                startActivity(mainIntent)
                finish()
            }
        }, 4000)*/
        val sharedPreference =  getSharedPreferences("login", Context.MODE_PRIVATE)
        val loginstatus = sharedPreference.getBoolean("loginstatus",false)
        if (loginstatus) {
            Handler(Looper.getMainLooper()).postDelayed({
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
                val isFirstLock =
                    MainUtil.getInstance().getBoolean(AppConstants.LOCK_IS_FIRST_LOCK, true)
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
                    startActivity(Intent(this, AppPermission::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
                if(isFirstLock){
                    startActivity(Intent(this, AppPermission::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                } else {
                    startActivity(Intent(this, Homepage::class.java))
                    //startActivity(Intent(this, ParentControl::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
            }, 4000)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                //val ii = Intent(this, Onboarding::class.java)
                val ii = Intent(this, LanguageSelection::class.java)
                startActivity(ii)
                finish()
            }, 4000)
        }
    }

    private fun setLocale(activity: Activity, languageCode: String?) {
        val locale = Locale(languageCode?:"")
        Locale.setDefault(locale)
        val resources = activity.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
    private fun showDialog() {
        if (!LockUtil.isStatAccessPermissionSet(this) && LockUtil.isNoOption(this)) {
            val dialog = PermissionDialog(this)
            dialog.show()
            dialog.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    var intent: Intent? = null
                    intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    startActivityForResult(intent, RESULT_ACTION_USAGE_ACCESS_SETTINGS)
                }
            }
        } else {
            gotoCreatePwdActivity()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_ACTION_USAGE_ACCESS_SETTINGS) {
            if (LockUtil.isStatAccessPermissionSet(this)) {
                gotoCreatePwdActivity()
            } else {
                ToastUtil.showToast("Permission denied")
                finish()
            }
        }
    }

    private fun gotoCreatePwdActivity() {
        val intent2 = Intent(this, DefinePasswordActivity::class.java)
        startActivity(intent2)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
    override fun onBackPressed() {
        //super.onBackPressed()
    }
}