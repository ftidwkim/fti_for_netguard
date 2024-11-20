package com.franklinwireless.android.jexkids.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatButton
import com.franklinwireless.android.jexkids.R
import com.franklinwireless.android.jexkids.utils.Animatoo

class termsandconditions : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_termsandconditions)

        val acceptcontinue = findViewById<AppCompatButton>(R.id.accept_continue)
        acceptcontinue.setOnClickListener{
            val intent = Intent(applicationContext, login::class.java)
            startActivity(intent)
            finish()
            Animatoo.animateSlideLeft(this)
        }
    }
}