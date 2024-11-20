package com.franklinwireless.android.jexkids.home.adapter

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeSuccessDialog
import com.franklinwireless.android.jexkids.R
import com.franklinwireless.android.jexkids.home.model.EarnModel

internal class EarnAdapter(private var earnList: List<EarnModel>) :
    RecyclerView.Adapter<EarnAdapter.MyViewHolder>() {
    private var context: Context? = null
    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.earnName)
        var token: TextView = view.findViewById(R.id.earncount)
        var earnStatus: TextView = view.findViewById(R.id.earnStatus)
    }
    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_earnlist, parent, false)
        context = parent.context;
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val earn = earnList[position]
        holder.title.text = earn.getTitle()
        holder.token.text = earn.getToken()
        holder.earnStatus.setOnClickListener{
            AwesomeSuccessDialog(context)
                .setTitle(Html.fromHtml("<font color='#000000'>jexkids</font>"))
                .setDialogBodyBackgroundColor(R.color.white)
                .setMessage(Html.fromHtml("<font color='#000000'><big>Lets Go!<br/>Add item to your task list?</big></font>"))
                .setColoredCircle(R.color.earn_bg)
                .setCancelable(true)
                .setPositiveButtonText("Yes")
                .setPositiveButtonbackgroundColor(R.color.earn_bg)
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
        return earnList.size
    }
}