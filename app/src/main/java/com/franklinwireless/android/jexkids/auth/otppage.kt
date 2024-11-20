package com.franklinwireless.android.jexkids.auth

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.franklinwireless.android.jexkids.R
import com.franklinwireless.android.jexkids.restservice.RestAPI
import com.franklinwireless.android.jexkids.restservice.ServiceBuilder
import com.franklinwireless.android.jexkids.auth.model.UserInfo
import com.franklinwireless.android.jexkids.locker.permission.AppPermission
import com.franklinwireless.android.jexkids.restservice.APIConstants
import com.goodiebag.pinview.Pinview
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class otppage : AppCompatActivity() {
    //private var pinview: Pinview? = null
    private var btn_submit: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otppage)
        //pinview = findViewById<Pinview>(R.id.pinview)
        btn_submit = findViewById<Button>(R.id.btn_verification)
        /*pinview!!.setPinViewEventListener { pinview, fromUser ->
            val pin = pinview.value

            if (pin.length == 6) {
                val inputMethodManager: InputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            }
        }

        pinview?.apply { setTextColor(Color.BLACK)}*/
        val otp_view = findViewById<Pinview>(R.id.otp_view)
//        otp_view.setOnFinishListener {
//            //Log.i("MainActivity", it)
//            //Toast.makeText(this, it, Toast.LENGTH_LONG).show()
//        }
//        otp_view.setOnCharacterUpdatedListener {
//            if(it)
//                Log.i("MainActivity", "The view is filled")
//            else
//                Log.i("MainActivity", "The view is NOT Filled")
//        }

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels

        otp_view.fitsSystemWindows
        btn_submit?.setOnClickListener{
            val mProgressDialog = ProgressDialog(this)
            mProgressDialog.setTitle("Loading")
            mProgressDialog.setMessage("Please wait")
            mProgressDialog.show()
            val deviceId: String = Settings.Secure.getString(
                this.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            Log.i("Device ID",deviceId)
            var textt = otp_view.value.toString().uppercase()
            val sharedPreference =  getSharedPreferences("login", Context.MODE_PRIVATE)
            val kidsname = sharedPreference.getString("kidsname",null)
            //val apiService = AddUser()
            val userInfo = UserInfo(deviceId,textt,kidsname)
            val retrofit = ServiceBuilder.buildService(RestAPI::class.java)
            retrofit.addUser(userInfo).enqueue(
                object : Callback<ResponseBody?> {
                    override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                        if (call.isCanceled) {
                            mProgressDialog.dismiss()
                            return
                        }
                    }
                    override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                        if(response.isSuccessful) {
                            val addedUser = response.body()?.string()

                            if (addedUser != null) {
                                if(addedUser.contains("errorType")) {
                                    var json = JSONObject(addedUser)
                                    var status = json.getString("errorMessage")
                                    Toast.makeText(
                                        applicationContext,
                                        status.toString(),
                                        Toast.LENGTH_LONG
                                    ).show()
                                    mProgressDialog.dismiss()
                                } else {
                                    APIConstants.FIRST_TIME=1;
                                    val sharedPreference =  getSharedPreferences("login",Context.MODE_PRIVATE)
                                    var editor = sharedPreference.edit()
                                    editor.putBoolean("loginstatus",true)
                                    editor.putString("uniquecode", textt.toString())
                                    editor.commit()
                                    mProgressDialog.dismiss()
                                    val mainIntent = Intent(applicationContext, AppPermission::class.java)
                                    startActivity(mainIntent)
                                    finish()
                                }
                            }
                        }
                    }
                }
            )

            /*apiService.addUser(userInfo) {
                if (it != null) {
                    Toast.makeText(applicationContext,it.toString(),Toast.LENGTH_LONG).show()
                    val sharedPreference =  getSharedPreferences("login",Context.MODE_PRIVATE)
                    var editor = sharedPreference.edit()
                    editor.putBoolean("loginstatus",true)
                    editor.putString("uniquecode", textt.toString())
                    editor.commit()
                } else {
                    Toast.makeText(applicationContext,"Error for registering new user",Toast.LENGTH_LONG).show()
                }
            }*/
        }
    }
}