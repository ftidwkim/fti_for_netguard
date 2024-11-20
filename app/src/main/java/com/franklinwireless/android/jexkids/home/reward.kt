package com.franklinwireless.android.jexkids.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.franklinwireless.android.jexkids.R
import com.franklinwireless.android.jexkids.home.adapter.RewardsAdapter
import com.franklinwireless.android.jexkids.home.model.RewardsModel

class reward : AppCompatActivity() {
    private var rewardsrecycler: RecyclerView? = null
    private val rewardsList = ArrayList<RewardsModel>()
    private lateinit var rewardsAdapter: RewardsAdapter
    private  var backbtn: AppCompatImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reward)
        rewardsrecycler = findViewById<RecyclerView>(R.id.rewardsrecycler)
        backbtn = findViewById<AppCompatImageView>(R.id.backbtn)
        backbtn!!.setOnClickListener{
            val intent = Intent(applicationContext,Homepage::class.java)
            startActivity(intent)
        }
        rewardsAdapter = RewardsAdapter(rewardsList)
        val layoutManager = LinearLayoutManager(this)
        rewardsrecycler?.layoutManager = layoutManager
        rewardsrecycler?.itemAnimator = DefaultItemAnimator()
        rewardsrecycler?.adapter = rewardsAdapter
        prepareRewardData()
    }
    private fun prepareRewardData() {
        var rlist = RewardsModel("1 extra hour of internet", "2")
        rewardsList.add(rlist)
        rlist = RewardsModel("Ice Cream Cone", "2")
        rewardsList.add(rlist)
        rlist = RewardsModel("Day of Bike Riding", "5")
        rewardsList.add(rlist)
        rlist = RewardsModel("1 hour Netflix", "2")
        rewardsList.add(rlist)
        rlist = RewardsModel("McDonalds for Dinner", "10")
        rewardsList.add(rlist)
        rlist = RewardsModel("1 hour social media", "2")
        rewardsList.add(rlist)
        rlist = RewardsModel("1 Chocolate Chip Cookie", "5")
        rewardsList.add(rlist)
        rewardsAdapter.notifyDataSetChanged()
    }
}