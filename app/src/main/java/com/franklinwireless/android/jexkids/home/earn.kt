package com.franklinwireless.android.jexkids.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.franklinwireless.android.jexkids.R
import com.franklinwireless.android.jexkids.home.adapter.EarnAdapter
import com.franklinwireless.android.jexkids.home.model.EarnModel

class earn : AppCompatActivity() {
    private var earnrecycler: RecyclerView? = null
    private val earnList = ArrayList<EarnModel>()
    private lateinit var earnAdapter: EarnAdapter
    private  var backbtn: AppCompatImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_earn)
        earnrecycler = findViewById<RecyclerView>(R.id.earnrecycler)
        backbtn = findViewById<AppCompatImageView>(R.id.backbtn)
        backbtn!!.setOnClickListener{
            val intent = Intent(applicationContext,Homepage::class.java)
            startActivity(intent)
        }
        earnAdapter = EarnAdapter(earnList)
        val layoutManager = LinearLayoutManager(this)
        earnrecycler?.layoutManager = layoutManager
        earnrecycler?.itemAnimator = DefaultItemAnimator()
        earnrecycler?.adapter = earnAdapter
        prepareActiveData()
    }
    private fun prepareActiveData() {
        var elist = EarnModel("Read a Chapter of a book!", "2")
        earnList.add(elist)
        elist = EarnModel("Wash the Dog", "2")
        earnList.add(elist)
        elist = EarnModel("Take out trash", "5")
        earnList.add(elist)
        elist = EarnModel("Wash clothes", "2")
        earnList.add(elist)
        elist = EarnModel("Read Geography text", "10")
        earnList.add(elist)
        elist = EarnModel("Clean your room", "2")
        earnList.add(elist)
        elist = EarnModel("Clean your room", "2")
        earnList.add(elist)
        earnAdapter.notifyDataSetChanged()
    }
}