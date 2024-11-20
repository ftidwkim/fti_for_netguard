package com.franklinwireless.android.jexkids.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.franklinwireless.android.jexkids.R
import com.franklinwireless.android.jexkids.home.model.UsageDetails
import com.franklinwireless.android.jexkids.restservice.RestAPI
import com.franklinwireless.android.jexkids.restservice.ServiceBuilder
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DailyUsage : AppCompatActivity() {
    lateinit var barChart: BarChart
    lateinit var barData: BarData
    lateinit var barDataSet: BarDataSet
    lateinit var barEntriesList: ArrayList<BarEntry>
    private var timeremaining: TextView? = null
    private var todayusage: TextView? = null
    private  var backbtn: AppCompatImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dailyusage)

        barChart = findViewById<BarChart>(R.id.idBarChart)
        timeremaining = findViewById<TextView>(R.id.txt_timeremaining)
        todayusage = findViewById<TextView>(R.id.txt_todayusage)
        backbtn = findViewById<AppCompatImageView>(R.id.backbtn)
        backbtn!!.setOnClickListener{
            val intent = Intent(applicationContext,Homepage::class.java)
            startActivity(intent)
        }

        getBarChartData()

        barDataSet = BarDataSet(barEntriesList, "")

        barData = BarData(barDataSet)

        barChart.data = barData
        val xAxisBottom = barChart.xAxis
        xAxisBottom.position = XAxis.XAxisPosition.BOTTOM

        val xAxisLabel: ArrayList<String> = ArrayList()
        xAxisLabel.add("Sun")
        xAxisLabel.add("Sun")
        xAxisLabel.add("Mon")
        xAxisLabel.add("Tue")
        xAxisLabel.add("Wed")
        xAxisLabel.add("Thu")
        xAxisLabel.add("Fri")
        xAxisLabel.add("Sat")

        val xAxis: XAxis = barChart.getXAxis()
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float, axis: AxisBase): String {
                return xAxisLabel[value.toInt()]
            }
        }
        xAxis.setAxisMaximum(8f);
        barChart.getXAxis().setValueFormatter(IndexAxisValueFormatter(xAxisLabel));

        barChart.getAxisLeft().setLabelCount(5, true);
        barChart.getAxisLeft().setAxisMinValue(1f);
        barChart.getAxisLeft().setAxisMaxValue(5F);

        barChart.getAxisRight().setLabelCount(5 , true);
        barChart.getAxisRight().setAxisMinValue(1f);
        barChart.getAxisRight().setAxisMaxValue(5F);

        /*val y: YAxis = barChart.getAxisLeft()
        y.setLabelCount(5);
        y.setAxisMaxValue(10f);
        y.setAxisMinValue(2f);

        val y1: YAxis = barChart.axisRight
        y1.setLabelCount(5);
        y1.setAxisMaxValue(10f);
        y1.setAxisMinValue(2f);*/
        barChart.axisRight.setDrawLabels(false)


        barDataSet.valueTextColor = R.color.black

        barDataSet.setColor(resources.getColor(R.color.app_color))

        barDataSet.valueTextSize = 14f

        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        getActivityList()
    }

    private fun getBarChartData() {
        barEntriesList = ArrayList()

        barEntriesList.add(BarEntry(1f, 0f))
        barEntriesList.add(BarEntry(2f, 0f))
        barEntriesList.add(BarEntry(3f, 0f))
        barEntriesList.add(BarEntry(4f, 0f))
        barEntriesList.add(BarEntry(5f, 0f))
        barEntriesList.add(BarEntry(6f, 0f))
        barEntriesList.add(BarEntry(7f, 0f))

    }

    private fun getActivityList() {
        val sharedPreference = getSharedPreferences("login", Context.MODE_PRIVATE)
        val uniqueCode = sharedPreference?.getString("uniquecode",null)
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = sdf.format(Date())
        val usageInfo = UsageDetails("kidsAppDailyUsage",uniqueCode,currentDate.toString(),)
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
                            todayusage?.text = tu.toString()
                            timeremaining?.text = tur.toString()
                            if(tur.equals("Daily Limit Reached",ignoreCase = false))
                            {
                                val sharedPreferences =
                                    getSharedPreferences("Settings", Activity.MODE_PRIVATE)
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
                            var gu = json.getString("graph_usage")
                            var json1 = JSONObject(gu)
                            var sun = json1.getString("Sun")
                            var mon = json1.getString("Mon")
                            var tue = json1.getString("Thu")
                            var wed = json1.getString("Wed")
                            var thu = json1.getString("Thu")
                            var fri = json1.getString("Fri")
                            var sat = json1.getString("Sat")
                        }
                    }
                }
            }
        )
    }
}