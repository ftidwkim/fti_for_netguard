package com.franklinwireless.android.jexkids.home.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.franklinwireless.android.jexkids.R
import com.franklinwireless.android.jexkids.home.model.TimeModel

internal class TimeAdapter(private var timeList: List<TimeModel>) :
    RecyclerView.Adapter<TimeAdapter.MyViewHolder>() {
    private var context: Context? = null
    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var appname: TextView = view.findViewById(R.id.app_name_tv)
        var timeusage: TextView = view.findViewById(R.id.time_usage_tv)
        var usageprogress: ProgressBar = view.findViewById(R.id.usage_progress)
    }
    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_used, parent, false)
        context = parent.context;
        return MyViewHolder(itemView)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val tused = timeList[position]
        holder.appname.text = tused.getAppName()
        /*val duration =  tused.getUsedtime()
        val seconds = duration?.div(1000)
        val hh = seconds?.div(3600)
        val secondsLeft: Int = seconds?.minus(hh?.times(3600) ?: 0) ?:0
        val mm = secondsLeft/60
        val ss = secondsLeft - mm * 60
        if (hh != null) {
            holder.timeusage.text = ""
            if (hh > 1) {
                holder.timeusage.text = "$hh Hour(s) "
            } else if (hh == 1) {
                holder.timeusage.text = "$hh Hour "
            }
            if (mm > 1) {
                holder.timeusage.text = holder.timeusage.text.toString() + "$mm Minute(s) "
            } else if (mm == 1) {
                holder.timeusage.text = holder.timeusage.text.toString() + "$mm Minute "
            }
            if (ss > 1) {
                holder.timeusage.text = holder.timeusage.text.toString() + "$ss Second(s) "
            } else {
                holder.timeusage.text = holder.timeusage.text.toString() + "$ss Second"
            }
        }*/
        /*val duration: Duration? = tused.getUsedtime()?.let { Duration.ofMillis(it.toLong()) }
        val seconds = duration?.seconds;
        val HH = seconds?.div(3600);
        val MM = seconds!! % 3600 / 60
        if (HH != null) {
            if(HH>0) {
                if(HH>1) {
                    if(MM>1) {
                        holder.timeusage.text = "$HH Hour(s) $MM Minute(s)"
                    }
                    else
                    {
                        holder.timeusage.text = "$HH Hour(s) $MM Minute"
                    }
                }
                else
                {
                    if(MM>1) {
                        holder.timeusage.text = "$HH Hour $MM Minute(s)"
                    }
                    else
                    {
                        holder.timeusage.text = "$HH Hour $MM Minute"
                    }
                }
            }
            else
            {
                if(MM>1) {
                    holder.timeusage.text = "$MM Minute(s)"
                }
                else
                {
                    holder.timeusage.text = "$MM Minute"
                }
            }
        }
        else
        {
            if(MM>1) {
                holder.timeusage.text = "$MM Minute(s)"
            }
            else
            {
                holder.timeusage.text = "$MM Minute"
            }
        }*/
        val milliseconds= tused.getUsedtime()?.toLong()
        if (milliseconds!! >= 1000) {
            val seconds = (milliseconds / 1000).toInt() % 60
            val minutes = (milliseconds / (1000 * 60) % 60).toInt()
            val hours = (milliseconds / (1000 * 60 * 60) % 24).toInt()
            val days = (milliseconds / (1000 * 60 * 60 * 24)).toInt()
            if (days == 0 && hours > 0) {
                if(hours>1) {
                    if(minutes>1) {
                        holder.timeusage.text = "$hours Hour(s) $minutes Minute(s) $seconds Second(s)"
                    }
                    else
                    {
                        holder.timeusage.text = "$hours Hour $minutes Minute $seconds Second(s)"
                    }
                }
                else
                {
                    if(minutes>1) {
                        holder.timeusage.text = "$hours Hour $minutes Minute(s) $seconds Second(s)"
                    }
                    else
                    {
                        holder.timeusage.text = "$hours Hour $minutes Minute $seconds Second(s)"
                    }
                }
            } else if (hours == 0 && minutes != 0) {
                if(minutes>1) {
                    holder.timeusage.text = "$minutes Minute(s) $seconds Second(s)"
                }
                else
                {
                    holder.timeusage.text = "$minutes Minute $seconds Second(s)"
                }
            } else if (days == 0 && hours == 0 && minutes == 0) {
                holder.timeusage.text = "$seconds Second(s)"
            } else {
                holder.timeusage.text = "$days Day(s) $hours Hour(s) $minutes Minute(s) $seconds Second(s)"
            }
        } else {
            holder.timeusage.text = "0 Second"
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.usageprogress.max = 100
            holder.usageprogress.setProgress(tused.getAppPercentage()?.toInt()?:0, false)
        }

    }
    override fun getItemCount(): Int {
        return timeList.size
    }
}
