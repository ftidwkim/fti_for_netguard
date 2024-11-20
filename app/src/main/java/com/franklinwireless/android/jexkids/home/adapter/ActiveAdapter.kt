package com.franklinwireless.android.jexkids.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.franklinwireless.android.jexkids.R
import com.franklinwireless.android.jexkids.home.model.activemodel

internal class ActiveAdapter(private var activeList: List<activemodel>) :
    RecyclerView.Adapter<ActiveAdapter.MyViewHolder>() {
    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.activeName)
        var token: TextView = view.findViewById(R.id.activecount)
    }
    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activelist, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val active = activeList[position]
        holder.title.text = active.getTitle()
        holder.token.text = active.getToken()
    }
    override fun getItemCount(): Int {
        return activeList.size
    }
}