package com.franklinwireless.android.jexkids.home.adapter

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeInfoDialog
import com.franklinwireless.android.jexkids.R
import com.franklinwireless.android.jexkids.home.model.RewardsModel

internal class RewardsAdapter(private var rewardsList: List<RewardsModel>) :
    RecyclerView.Adapter<RewardsAdapter.MyViewHolder>() {
    private var context: Context? = null
    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.rewardsName)
        var token: TextView = view.findViewById(R.id.rewardscount)
        var rewardsStatus: TextView = view.findViewById(R.id.rewardsStatus)
    }
    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rewardslist, parent, false)
        context = parent.context;
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val rew = rewardsList[position]
        holder.title.text = rew.getTitle()
        holder.token.text = rew.getToken()
        holder.rewardsStatus.setOnClickListener{
            AwesomeInfoDialog(context)
                .setTitle(Html.fromHtml("<font color='#000000'>jexkids</font>"))
                .setDialogBodyBackgroundColor(R.color.white)
                .setMessage(Html.fromHtml("<font color='#000000'><big>Request a reward!<br/>Send in your request<br/>approval?</big></font>"))
                .setColoredCircle(R.color.rewards_header)
                .setCancelable(true)
                .setPositiveButtonText("Request")
                .setPositiveButtonbackgroundColor(R.color.rewards_header)
                .setPositiveButtonTextColor(R.color.white)
                .setNegativeButtonText("No")
                .setNegativeButtonbackgroundColor(R.color.pending_header)
                .setNegativeButtonTextColor(R.color.white)
                .setPositiveButtonClick { null }
                .setNegativeButtonClick { null }
                .show()
        }
    }
    override fun getItemCount(): Int {
        return rewardsList.size
    }
}