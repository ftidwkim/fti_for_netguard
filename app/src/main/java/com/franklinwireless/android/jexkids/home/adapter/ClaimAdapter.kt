package com.franklinwireless.android.jexkids.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.franklinwireless.android.jexkids.R
import com.franklinwireless.android.jexkids.home.model.claimmodel

internal class ClaimAdapter(private var claimList: List<claimmodel>) :
    RecyclerView.Adapter<ClaimAdapter.MyViewHolder>() {
    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.claimName)
        var reward: TextView = view.findViewById(R.id.claimreward)
    }
    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_claimlist, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val claim = claimList[position]
        holder.title.text = claim.getTitle()
        holder.reward.text = claim.getReward()
    }
    override fun getItemCount(): Int {
        return claimList.size
    }
}