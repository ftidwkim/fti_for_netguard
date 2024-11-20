package com.franklinwireless.android.jexkids.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.franklinwireless.android.jexkids.R
import com.franklinwireless.android.jexkids.home.adapter.ClaimAdapter
import com.franklinwireless.android.jexkids.home.model.claimmodel


class claim : Fragment() {
    private var recyclerView: RecyclerView? = null
    private val claimList = ArrayList<claimmodel>()
    private lateinit var claimAdapter: ClaimAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_claim, container, false)
        recyclerView = view.findViewById<RecyclerView>(R.id.claimrecycler)
        claimAdapter = ClaimAdapter(claimList)
        val layoutManager = LinearLayoutManager(activity)
        recyclerView?.layoutManager = layoutManager
        recyclerView?.itemAnimator = DefaultItemAnimator()
        recyclerView?.adapter = claimAdapter
        prepareActiveData()
        return view
    }
    private fun prepareActiveData() {
        var alist = claimmodel("1 extra hour of internet", "You earned a reward")
        claimList.add(alist)
        alist = claimmodel("Ice Cream Cone", "You earned a reward")
        claimList.add(alist)
        alist = claimmodel("Day of Bike Riding", "You earned a reward")
        claimList.add(alist)
        alist = claimmodel("1 hour Netflix", "You earned a reward")
        claimList.add(alist)
        alist = claimmodel("McDonalds for Dinner", "You earned a reward")
        claimList.add(alist)
        alist = claimmodel("1 hour social media", "You earned a reward")
        claimList.add(alist)
        alist = claimmodel("1 Chocolate Chip Cookie", "You earned a reward")
        claimList.add(alist)
        claimAdapter.notifyDataSetChanged()
    }
}