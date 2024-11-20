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
import com.franklinwireless.android.jexkids.home.adapter.PendingAdapter
import com.franklinwireless.android.jexkids.home.model.PendingModel
import com.sayantan.advancedspinner.MultiSpinner
import java.util.*


class Pending : Fragment() {
    private var recyclerView: RecyclerView? = null
    private var multipleselection: MultiSpinner? = null
    private val pendingList = ArrayList<PendingModel>()
    private lateinit var pendingAdapter: PendingAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_pending, container, false)
        recyclerView = view.findViewById<RecyclerView>(R.id.pendingrecycler)
        multipleselection = view.findViewById<MultiSpinner>(R.id.multipleItemSelectionSpinner)
        //val Lines = listOf(resources.getStringArray(R.array.spinner_items))
        /*val Lines = arrayListOf<String>(*resources.getStringArray(R.array.spinner_items))

        multipleselection?.isSearchEnabled = true
        multipleselection?.setSearchHint("All")
        multipleselection?.isShowSelectAllButton = true
        multipleselection?.setClearText("Done")

        val listArray1: MutableList<KeyPairBoolData> = ArrayList()
        for (i in Lines.indices) {
            val h = KeyPairBoolData()
            h.id = (i + 1).toLong()
            h.name = Lines[i].toString()
            //h.isSelected = i < 5
            listArray1.add(h)
        }

        multipleselection?.setItems(listArray1,
            MultiSpinnerListener { items ->
                for (i in items.indices) {
                    if (items[i].isSelected) {
                        Log.i(
                            "Selected",
                            i.toString() + " : " + items[i].name + " : " + items[i].isSelected
                        )
                    }
                }
            })
        multipleselection?.setLimit(2, LimitExceedListener {
            Toast.makeText(
                activity,
                "Limit exceed", Toast.LENGTH_LONG
            ).show()
        })*/

        pendingAdapter = PendingAdapter(pendingList)
        val layoutManager = LinearLayoutManager(activity)
        recyclerView?.layoutManager = layoutManager
        recyclerView?.itemAnimator = DefaultItemAnimator()
        recyclerView?.adapter = pendingAdapter
        preparePendingData()
        return view
    }
    private fun preparePendingData() {
        var alist = PendingModel("Read a Chapter of a book!", "2")
        pendingList.add(alist)
        alist = PendingModel("Wash the Dog", "2")
        pendingList.add(alist)
        pendingAdapter.notifyDataSetChanged()
    }
}