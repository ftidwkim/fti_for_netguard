package com.franklinwireless.android.jexkids.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.franklinwireless.android.jexkids.R
import com.franklinwireless.android.jexkids.databinding.FragmentHomeBinding
import com.franklinwireless.android.jexkids.home.model.ChildDetails
import com.franklinwireless.android.jexkids.home.model.UsageDetails
import com.franklinwireless.android.jexkids.restservice.RestAPI
import com.franklinwireless.android.jexkids.restservice.ServiceBuilder
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class home : Fragment() {
    private var kidsname: TextView? = null
    private var dailyusage: TextView? = null
    private var earnn: CardView? = null
    private var rewards: CardView? = null
    private var rewardgoal: CardView? = null
    private var card_dailyusage: CardView? = null
    private var pianolessons: CardView? = null
    private lateinit var binding: FragmentHomeBinding
    var handler: Handler = Handler()
    var apiDelayed = 10 * 1000 //1 second=1000 milisecond, 5*1000=5seconds

    var runnable: Runnable? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        val view = binding.root
        //val view: View = inflater.inflate(R.layout.fragment_home, container, false)
        kidsname = view.findViewById<TextView>(R.id.kidsname)
        dailyusage = view.findViewById<TextView>(R.id.dailyusage)
        earnn = view.findViewById<CardView>(R.id.earn)
        rewards = view.findViewById<CardView>(R.id.rewards)
        rewardgoal = view.findViewById<CardView>(R.id.rewardgoal)
        card_dailyusage = view.findViewById<CardView>(R.id.card_dailyusage)
        pianolessons = view.findViewById<CardView>(R.id.pianolessons)
        binding.itemHome.homeRefresh.setOnClickListener{
            /*val context = requireActivity()
            val transaction = context.supportFragmentManager.beginTransaction()
            transaction.replace(R.id.flFragment,home())
            transaction.commit()*/
            binding.itemHome.homeRefresh.isEnabled = false
            getChildDetails()
        }
        earnn!!.setOnClickListener{
            //val intent = Intent(activity,earn::class.java)
            //startActivity(intent)
            Toast.makeText(activity,"Coming Soon",Toast.LENGTH_LONG).show()
            /*AwesomeSuccessDialog(requireActivity())
                .setTitle(getString(R.string.app_name))
                .setDialogBodyBackgroundColor(R.color.pending_btn_bg)
                .setMessage(getString(R.string.dialog_message))
                .setColoredCircle(R.color.earn_bg)
                .setCancelable(true)
                .setPositiveButtonText("Yes")
                .setPositiveButtonbackgroundColor(R.color.earn_bg)
                .setPositiveButtonTextColor(R.color.white)
                .setNegativeButtonText("No")
                .setNegativeButtonbackgroundColor(R.color.pending_header)
                .setNegativeButtonTextColor(R.color.white)
                .setPositiveButtonClick { null }
                .setNegativeButtonClick { null }
                .show()*/
        }
        rewards!!.setOnClickListener{
            //val intent = Intent(activity,reward::class.java)
            //startActivity(intent)
            Toast.makeText(activity,"Coming Soon", Toast.LENGTH_LONG).show()
        }
        rewardgoal!!.setOnClickListener{
            //val intent = Intent(activity,goal::class.java)
            //startActivity(intent)
            Toast.makeText(activity,"Coming Soon",Toast.LENGTH_LONG).show()
        }
        card_dailyusage!!.setOnClickListener{
            //val intent = Intent(activity,DailyUsage::class.java)
            //startActivity(intent)
            val intent = Intent(activity,TimeUsed::class.java)
            startActivity(intent)
        }
        pianolessons!!.setOnClickListener{
            //val intent = Intent(activity,todayevents::class.java)
            //startActivity(intent)
            Toast.makeText(activity,"Coming Soon",Toast.LENGTH_LONG).show()
        }
//        context?.startService(Intent(requireContext(),APIService::class.java))
        getChildDetails()
        return view
    }
    private fun getChildDetails() {
        val sharedPreference = activity?.getSharedPreferences("login", Context.MODE_PRIVATE)
        val uniqueCode = sharedPreference?.getString("uniquecode",null)
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = sdf.format(Date())
        val childInfo = ChildDetails(uniqueCode,currentDate.toString(),"getChildDetails")
        val retrofit = ServiceBuilder.buildService(RestAPI::class.java)
        retrofit.childDetails(uniqueCode.toString(),childInfo).enqueue(
            object : Callback<ResponseBody?> {
                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    if (call.isCanceled) {
                        return
                    }
                }
                override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                    if(response.isSuccessful) {
                        val cDetails = response.body()?.string()

                        if (cDetails != null) {
                            val jsonArray = JSONTokener(cDetails).nextValue() as JSONArray
                            for (i in 0 until jsonArray.length()) {
                                val parentalControlId = jsonArray.getJSONObject(i).getString("parental_control_id")
                                val username = jsonArray.getJSONObject(i).getString("user_name")
                                val details = jsonArray.getJSONObject(i).getString("details")
                                var json = JSONObject(details)
                                var totalusage = json.getString("total_usage_reaming").toString()
                                kidsname?.text = username.toString()
                                /*if(totalusage.equals("Daily Limit Reached",ignoreCase = false))
                                {
                                    val sharedPreferences =
                                        activity?.getSharedPreferences("Settings", Activity.MODE_PRIVATE)
                                    val language = sharedPreferences?.getString("My_Lang", "")
                                    if (language != null) {
                                        if(language.equals("en"))
                                        {
                                            dailyusage?.text = "$totalusage"
                                        }
                                        else if(language.equals("ko"))
                                        {
                                            dailyusage?.text = "일일 한도 도달"
                                        }
                                        else if(language.equals("es"))
                                        {
                                            dailyusage?.text = "Límite diario alcanzado"
                                        }
                                    }
                                }
                                else {
                                    if(totalusage.equals("0",ignoreCase = false))
                                    {
                                        dailyusage?.text = "- "+getString(R.string.remaining)
                                    }
                                    else {
                                        dailyusage?.text = "$totalusage "+getString(R.string.remaining)
                                    }
                                }*/
                                val sharedPreference =  activity?.getSharedPreferences("login",Context.MODE_PRIVATE)
                                var editor = sharedPreference?.edit()
                                if (editor != null) {
                                    editor.putString("parental_controlID",parentalControlId.toString())
                                    editor.putString("kidsname", username.toString())
                                    editor.commit()
                                }
                                getActivityList()
                            }

                        }
                    }
                }
            }
        )
    }
    private fun getActivityList() {
        val sharedPreference = activity?.getSharedPreferences("login", Context.MODE_PRIVATE)
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
                            if(tur.equals("Daily Limit Reached",ignoreCase = false))
                            {
                                val sharedPreferences =
                                    activity?.getSharedPreferences("Settings", Activity.MODE_PRIVATE)
                                val language = sharedPreferences?.getString("My_Lang", "")
                                if (language != null) {
                                    if(language.equals("en"))
                                    {
                                        dailyusage?.text = "$tur"
                                    }
                                    else if(language.equals("ko"))
                                    {
                                        dailyusage?.text = "일일 한도 도달"
                                    }
                                    else if(language.equals("es"))
                                    {
                                        dailyusage?.text = "Límite diario alcanzado"
                                    }
                                }
                            }
                            else {
                                if(tur.equals("0",ignoreCase = false))
                                {
                                    //dailyusage?.text = "- "+R.string.remaining
                                    dailyusage?.text = "- ${getString(R.string.remaining)}"
                                }
                                else {
                                    //dailyusage?.text = "$tur "+R.string.remaining
                                    //dailyusage?.text = "$tur "+getString(R.string.remaining)
                                    dailyusage?.text = "$tur ${getString(R.string.remaining)}"
                                }
                            }
                            binding.itemHome.homeRefresh.isEnabled = true
                        }
                    }
                }
            }
        )
    }
    override fun onPause() {
        super.onPause()
        runnable?.let { handler.removeCallbacks(it) } //stop handler when activity not visible
    }
    override fun onResume() {
        super.onResume()
        getChildDetails()
    }
}