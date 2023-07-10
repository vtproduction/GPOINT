package com.jedmahonisgroup.gamepoint.adapters.results

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.jedmahonisgroup.gamepoint.R

class ResultsDayViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(inflater.inflate(R.layout.day_card, parent, false)){

    private var day: TextView? = null
    var eventRv: androidx.recyclerview.widget.RecyclerView? = null

    private var date: TextView? = null
    private var team1: TextView? = null
    private var team2: TextView? = null

    //private var numberOfPoints: TextView? = null

    init {
        day = itemView.findViewById(R.id.day)
        eventRv = itemView.findViewById(R.id.eventRv)
    }

    fun bind(dayOfWeek: String) {
        day?.text = dayOfWeek

    }

}