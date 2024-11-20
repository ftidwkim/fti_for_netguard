package com.franklinwireless.android.jexkids.auth

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.franklinwireless.android.jexkids.R
import com.franklinwireless.android.jexkids.databinding.ActivityOnboardingBinding
import com.franklinwireless.android.jexkids.adapters.OnboardingViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.franklinwireless.android.jexkids.utils.Animatoo

class Onboarding : AppCompatActivity() {
    private lateinit var mViewPager: ViewPager2
    private lateinit var textSkip: TextView
    private val RESULT_ACTION_USAGE_ACCESS_SETTINGS = 1

    private lateinit var binding: ActivityOnboardingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        val view = binding.root
        //setContentView(R.layout.activity_onboarding)
        setContentView(view)
        mViewPager = binding.viewPager
        mViewPager.adapter = OnboardingViewPagerAdapter(this, this)
        TabLayoutMediator(binding.pageIndicator, mViewPager) { _, _ -> }.attach()
        textSkip = findViewById(R.id.text_skip)
        textSkip.setOnClickListener {

            //val intent = Intent(applicationContext, termsandconditions::class.java)
            val intent = Intent(applicationContext, login::class.java)
            startActivity(intent)
            finish()
            Animatoo.animateSlideLeft(this)
        }

        val btnNextStep: Button = findViewById(R.id.btn_next_step)

        btnNextStep.setOnClickListener {
            if (getItem() > mViewPager.childCount) {
                //val intent = Intent(applicationContext, termsandconditions::class.java)
                val intent = Intent(applicationContext, login::class.java)
                startActivity(intent)
                finish()
                Animatoo.animateSlideLeft(this)

            } else {
                mViewPager.setCurrentItem(getItem() + 1, true)
            }
        }
    }
    private fun getItem(): Int {
        return mViewPager.currentItem
    }
    override fun onBackPressed() {
        //super.onBackPressed()
    }
}