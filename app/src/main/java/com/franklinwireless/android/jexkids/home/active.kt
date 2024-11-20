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
import com.franklinwireless.android.jexkids.home.adapter.ActiveAdapter
import com.franklinwireless.android.jexkids.home.model.activemodel


class active : Fragment() {
    private var recyclerView: RecyclerView? = null
    private val activeList = ArrayList<activemodel>()
    private lateinit var activeAdapter: ActiveAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_active, container, false)
        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        activeAdapter = ActiveAdapter(activeList)
        val layoutManager = LinearLayoutManager(activity)
        recyclerView?.layoutManager = layoutManager
        recyclerView?.itemAnimator = DefaultItemAnimator()
        recyclerView?.adapter = activeAdapter
        prepareActiveData()
        return view
    }
    private fun prepareActiveData() {
        var alist = activemodel("Read a Chapter of a book!", "2")
        activeList.add(alist)
        alist = activemodel("Wash the Dog", "2")
        activeList.add(alist)
        activeAdapter.notifyDataSetChanged()
    }
}