package com.jedmahonisgroup.gamepoint.adapters.results

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jedmahonisgroup.gamepoint.model.picks.MyPicksModel

class ResultsAdapterEvent : androidx.recyclerview.widget.RecyclerView.Adapter<ResultsViewHolder>() {

    private val TAG: String = ResultsDayAdapter::class.java.simpleName
    private val response: ArrayList<MyPicksModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ResultsViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
        return response.size
    }

    override fun onBindViewHolder(holder: ResultsViewHolder, position: Int) {
        val data: MyPicksModel = response[position]
        if (position == itemCount - 1) {
            holder.separator?.visibility = View.INVISIBLE
        }

        holder.bind(data)
    }

    fun refreshList(newList: ArrayList<MyPicksModel>) {
        if (itemCount != 0) this.response.clear()
        this.response.clear()
        response.addAll(newList)
        notifyDataSetChanged()
    }
}