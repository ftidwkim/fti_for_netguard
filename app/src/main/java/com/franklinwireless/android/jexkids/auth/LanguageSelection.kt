package com.franklinwireless.android.jexkids.auth

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.franklinwireless.android.jexkids.R
import com.franklinwireless.android.jexkids.databinding.ActivityLanguageSelectionBinding
import com.orhanobut.logger.Logger
import java.util.Locale

class LanguageSelection : AppCompatActivity() {
    private val KOREAN:Int = 1
    private val SPANISH:Int = 2

    private var spSelect:Int = 0
    private var descText: MutableLiveData<String> = MutableLiveData()
    private var btnText: MutableLiveData<String> = MutableLiveData()

    private lateinit var binding: ActivityLanguageSelectionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var lang = ""
        lang = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            resources.configuration.locales.get(0).toString()
        else
            resources.configuration.locale.toString()

        descText.observe(this) {
            binding.languageProceed.text = it
        }
        btnText.observe(this) {
            binding.proceed.text = it
        }
        if(lang.length > 2)
        {
            lang = lang.substring(0,2)
            when(lang){
                "ko" ->{
                    spSelect = 1
                    binding.spLangDl.setSelection(1)
                }
                "es" ->{
                    spSelect = 2
                    binding.spLangDl.setSelection(2)
                }
                else ->{
                    spSelect - 0
                    binding.spLangDl.setSelection(0)
                }
            }
        }
        binding.spLangDl.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position){
                    SPANISH -> setLocale("es")
                    KOREAN -> setLocale("ko")
                    else -> setLocale("en")
                }
            }
        }
        binding.proceed.setOnClickListener{
            val ii = Intent(this, Onboarding::class.java)
            startActivity(ii)
            finish()
        }
    }

    private fun setLocale(languageCode: String?) {
        val editor = getSharedPreferences("Settings", Context.MODE_PRIVATE).edit()
        editor.putString("My_Lang", languageCode)
        editor.apply()

        val locale = Locale(languageCode?:"")
        Locale.setDefault(locale)
        val resources = this.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
//        applicationContext.createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)

        descText.value = getString(R.string.language_proceed)
        btnText.value = getString(R.string.proceed)
    }
}