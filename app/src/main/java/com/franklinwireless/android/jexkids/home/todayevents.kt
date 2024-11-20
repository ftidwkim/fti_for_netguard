package com.franklinwireless.android.jexkids.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.franklinwireless.android.jexkids.R
import com.franklinwireless.android.jexkids.home.model.Event
import com.framgia.library.calendardayview.CalendarDayView
import com.framgia.library.calendardayview.EventView
import com.framgia.library.calendardayview.EventView.OnEventClickListener
import com.framgia.library.calendardayview.data.IEvent
import com.framgia.library.calendardayview.decoration.CdvDecorationDefault
import java.util.*


class todayevents : AppCompatActivity() {
    private var dayView: CalendarDayView? = null
    private  var backbtn: AppCompatImageView? = null
    private val events = java.util.ArrayList<IEvent>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todayevents)
        dayView = findViewById<CalendarDayView>(R.id.calendar)
        backbtn = findViewById<AppCompatImageView>(R.id.backbtn)

        (dayView?.getDecoration() as CdvDecorationDefault).setOnEventClickListener(
            object : OnEventClickListener {
                override fun onEventClick(view: EventView, data: IEvent) {
                    Log.e("TAG", "onEventClick:" + data.name)
                }

                override fun onEventViewClick(view: View?, eventView: EventView?, data: IEvent) {
                    Log.e("TAG", "onEventViewClick:" + data.name)
                    if (data is Event) {
                        // change event (ex: set event color)
                        dayView?.setEvents(events)
                    }
                }
            })
        backbtn!!.setOnClickListener{
            val intent = Intent(applicationContext,Homepage::class.java)
            startActivity(intent)
        }
        prepareEventData()
    }
    private fun prepareEventData() {
        val eventColor = ContextCompat.getColor(this, R.color.app_color)
        val timeStart: Calendar = Calendar.getInstance()
        timeStart.set(Calendar.HOUR_OF_DAY, 15)
        timeStart.set(Calendar.MINUTE, 0)
        val timeEnd: Calendar = timeStart.clone() as Calendar
        timeEnd.set(Calendar.HOUR_OF_DAY, 17)
        timeEnd.set(Calendar.MINUTE, 0)
        /*val timeStart1: Calendar = Calendar.getInstance()
        timeStart.set(Calendar.HOUR_OF_DAY, 18)
        timeStart.set(Calendar.MINUTE, 0)
        val timeEnd1: Calendar = timeStart.clone() as Calendar
        timeEnd.set(Calendar.HOUR_OF_DAY, 20)
        timeEnd.set(Calendar.MINUTE, 30)*/
        var elist = Event(1, timeStart, timeEnd, "Pianno Lessons", "", eventColor)
        events.add(elist)
        //elist = Event(1, timeStart1, timeEnd1, "Another Event", "Erode", eventColor)
        //events.add(elist)
        dayView?.setEvents(events)
    }
}