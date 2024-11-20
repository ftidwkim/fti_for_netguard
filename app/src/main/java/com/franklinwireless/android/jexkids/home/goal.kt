package com.franklinwireless.android.jexkids.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatImageView
import com.franklinwireless.android.jexkids.R

class goal : AppCompatActivity() {
    private  var backbtn: AppCompatImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal)
        backbtn = findViewById<AppCompatImageView>(R.id.backbtn)
        backbtn!!.setOnClickListener{
            val intent = Intent(applicationContext,Homepage::class.java)
            startActivity(intent)
        }
    }
}