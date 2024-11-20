package com.franklinwireless.android.jexkids.auth

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.*
import com.franklinwireless.android.jexkids.R
import com.franklinwireless.android.jexkids.utils.Animatoo

class login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val enterbtn = findViewById<Button>(R.id.btn_login)
        val username = findViewById<EditText>(R.id.et_email)
        val terms_conditions = findViewById<RadioButton>(R.id.terms_conditions)
        val privacy_policy = findViewById<RadioButton>(R.id.privacy_policy)
        val termstv = findViewById<TextView>(R.id.terms_tv)
        val privacytv = findViewById<TextView>(R.id.privacy_tv)
        termstv.movementMethod = LinkMovementMethod.getInstance()
        privacytv.movementMethod = LinkMovementMethod.getInstance()
        enterbtn.setOnClickListener{
            if(!terms_conditions.isChecked)
            {
                Toast.makeText(this, "Kindly accept JEXtream's Terms and Conditions", Toast.LENGTH_SHORT).show()
            }
            else if(!privacy_policy.isChecked)
            {
                Toast.makeText(this, "Kindly accept JEXtream's Privacy Policy", Toast.LENGTH_SHORT).show()
            }
            else {
                if (username.text.toString().isNotEmpty()) {
                    val sharedPreference = getSharedPreferences("login", Context.MODE_PRIVATE)
                    var editor = sharedPreference.edit()
                    editor.putString("kidsname", username.text.toString())
                    editor.commit()
                    val intent = Intent(applicationContext, otppage::class.java)
                    startActivity(intent)
                    finish()
                    Animatoo.animateSlideLeft(this)
                } else {
                    Toast.makeText(applicationContext, "Enter your Kids Name", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }
}