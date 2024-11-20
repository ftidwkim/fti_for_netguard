package com.franklinwireless.android.jexkids.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.franklinwireless.android.jexkids.R
import com.franklinwireless.android.jexkids.home.model.PendingModel

internal class PendingAdapter(private var pendingList: List<PendingModel>) :
    RecyclerView.Adapter<PendingAdapter.MyViewHolder>() {
    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.pendingName)
        var token: TextView = view.findViewById(R.id.pendingcount)
    }
    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pendinglist, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val pending = pendingList[position]
        holder.title.text = pending.getTitle()
        holder.token.text = pending.getToken()
    }
    override fun getItemCount(): Int {
        return pendingList.size
    }
}