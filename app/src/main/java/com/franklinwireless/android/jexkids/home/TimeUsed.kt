package com.franklinwireless.android.jexkids.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.franklinwireless.android.jexkids.R
import com.franklinwireless.android.jexkids.home.adapter.TimeAdapter
import com.franklinwireless.android.jexkids.home.model.TimeModel
import com.franklinwireless.android.jexkids.home.model.UsageDetails
import com.franklinwireless.android.jexkids.restservice.RestAPI
import com.franklinwireless.android.jexkids.restservice.ServiceBuilder
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class TimeUsed : AppCompatActivity() {
    private var timeremaining: TextView? = null
    private var todayusage: TextView? = null
    private var timerecycler: RecyclerView? = null
    private var timea_used_progress: ProgressBar? = null
    private val timeList = ArrayList<TimeModel>()
    private lateinit var timeAdapter: TimeAdapter
    private  var backbtn: AppCompatImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_used)
        timeremaining = findViewById<TextView>(R.id.txt_timeremaining)
        todayusage = findViewById<TextView>(R.id.txt_todayusage)
        timerecycler = findViewById<RecyclerView>(R.id.time_used_rv)
        timea_used_progress = findViewById<ProgressBar>(R.id.timea_used_progress)
        backbtn = findViewById<AppCompatImageView>(R.id.backbtn)
        backbtn!!.setOnClickListener{
            val intent = Intent(applicationContext,Homepage::class.java)
            startActivity(intent)
        }
        timeAdapter = TimeAdapter(timeList)
        val layoutManager = LinearLayoutManager(this)
        timerecycler?.layoutManager = layoutManager
        timerecycler?.itemAnimator = DefaultItemAnimator()
        timerecycler?.adapter = timeAdapter
        getActivityList()
        //prepareActiveData()
    }
    /*private fun prepareActiveData() {
        var tlist = TimeModel("Facebook", 5545900,40)
        timeList.add(tlist)
        tlist = TimeModel("Whatsapp", 368560,60)
        timeList.add(tlist)
        tlist = TimeModel("Jextream", 254590,20)
        timeList.add(tlist)
        tlist = TimeModel("YouTube", 7545950,70)
        timeList.add(tlist)
        tlist = TimeModel("Gmail", 574590,60)
        timeList.add(tlist)
        tlist = TimeModel("Contacts", 424590,20)
        timeList.add(tlist)
        tlist = TimeModel("Messages", 354590,10)
        timeList.add(tlist)
        tlist = TimeModel("Internet", 360000,2)
        timeList.add(tlist)
        timeAdapter.notifyDataSetChanged()
        timea_used_progress?.visibility  = View.GONE
    }*/
    private fun getActivityList() {
        val sharedPreference = getSharedPreferences("login", Context.MODE_PRIVATE)
        val uniqueCode = sharedPreference?.getString("uniquecode",null)
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = sdf.format(Date())
        val usageInfo = UsageDetails("kidsAppDailyUsage", uniqueCode, currentDate.toString())
        val retrofit = ServiceBuilder.buildService(RestAPI::class.java)
        retrofit.usageDetails(uniqueCode.toString(),usageInfo).enqueue(
            object : Callback<ResponseBody?> {
                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    if (call.isCanceled) {
                        return
                    }
                }
                override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                    if(response.isSuccessful) {
                        val uDetails = response.body()?.string()

                        if (uDetails != null) {
                            var json = JSONObject(uDetails)
                            var tu = json.getString("total_usage")
                            var tur = json.getString("total_usage_reaming")
                            var appusage = json.getJSONArray("app_usage")
                            todayusage?.text = tu.toString()
                            timeremaining?.text = tur.toString()
                            if(tur.equals("Daily Limit Reached",ignoreCase = false))
                            {
                                val sharedPreferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE)
                                val language = sharedPreferences?.getString("My_Lang", "")
                                if (language != null) {
                                    if(language.equals("en"))
                                    {
                                        timeremaining?.text = "$tur"
                                    }
                                    else if(language.equals("ko"))
                                    {
                                        timeremaining?.text = "일일 한도 도달"
                                    }
                                    else if(language.equals("es"))
                                    {
                                        timeremaining?.text = "Límite diario alcanzado"
                                    }
                                }
                            }
                            else {
                                if(tur.equals("0",ignoreCase = false))
                                {
                                    timeremaining?.text = "- "+getString(R.string.remaining)
                                }
                                else {
                                    timeremaining?.text = "$tur "+getString(R.string.remaining)
                                }
                            }
                            //val jsonArray = JSONTokener(cDetails).nextValue() as JSONArray
                            for (i in 0 until appusage.length()) {
                                val objects: JSONObject = appusage.getJSONObject(i)
                                val appname = objects["app_name"].toString()
                                if (objects.has("app_time")){
                                    val apptime = objects["app_time"]
                                    if(objects.has("app_percentage")) {
                                        val apppercentage = objects["app_percentage"].toString().toDouble()
                                        //val num: Double = apppercentage.toString().t
                                        var tlist = TimeModel(appname, apptime as Int?, apppercentage as Double?)
                                        timeList.add(tlist)
                                    }
                                    else
                                    {
                                        var tlist = TimeModel(appname, apptime as Int?, 0.0 as Double?)
                                        timeList.add(tlist)
                                    }
                                }
                            }
                            timeAdapter.notifyDataSetChanged()
                            timea_used_progress?.visibility  = View.GONE
                            /*var gu = json.getString("graph_usage")
                            var json1 = JSONObject(gu)
                            var sun = json1.getString("Sun")
                            var mon = json1.getString("Mon")
                            var tue = json1.getString("Thu")
                            var wed = json1.getString("Wed")
                            var thu = json1.getString("Thu")
                            var fri = json1.getString("Fri")
                            var sat = json1.getString("Sat")*/
                        }
                    }
                }
            }
        )
    }
}