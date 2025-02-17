package com.franklinwireless.android.jexkids.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.franklinwireless.android.jexkids.fragments.OnboardingFragment
import com.franklinwireless.android.jexkids.R


class OnboardingViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val context: Context
) :
    FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingFragment.newInstance(
                context.resources.getString(R.string.title_onboarding_1),
                context.resources.getString(R.string.description_onboarding_1),
                R.raw.logo_splash,
                context.resources.getString(R.string.description_onboarding_link1),
                context.resources.getString(R.string.link_address1),
            )
            else -> OnboardingFragment.newInstance(
                context.resources.getString(R.string.title_onboarding_2),
                context.resources.getString(R.string.description_onboarding_2),
                R.raw.logo_splash,
                context.resources.getString(R.string.description_onboarding_link2),
                context.resources.getString(R.string.link_address2),
            )
            /*else -> OnboardingFragment.newInstance(
                context.resources.getString(R.string.title_onboarding_3),
                context.resources.getString(R.string.description_onboarding_3),
                R.raw.logo_splash
            )*/
        }
    }

    override fun getItemCount(): Int {
        return 2
    }
}