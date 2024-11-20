package com.franklinwireless.android.jexkids.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.franklinwireless.android.jexkids.R
import com.sayantan.advancedspinner.MultiSpinner
import java.text.SimpleDateFormat
import java.util.*


class History : Fragment() {
    private var multipleselection: MultiSpinner? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_history, container, false)
        val txt_calendar = view.findViewById<AppCompatTextView>(R.id.history_calendar)
        multipleselection = view.findViewById<MultiSpinner>(R.id.historySpinner)
        val sdf = SimpleDateFormat("MM/dd/yyyy")
        val currentDate = sdf.format(Date())
        txt_calendar.text = currentDate.toString()
        return view
    }
}