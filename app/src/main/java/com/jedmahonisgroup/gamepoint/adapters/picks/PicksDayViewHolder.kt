package com.jedmahonisgroup.gamepoint.adapters.picks

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.ui.sharedPreferences
import com.jedmahonisgroup.gamepoint.utils.Constants

class PicksDayViewHolder(inflater: LayoutInflater, private val parent: ViewGroup) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(inflater.inflate(R.layout.day_card, parent, false)) {

    private var day: TextView? = null
    private var day_bar: RelativeLayout? = null
    var eventRv: androidx.recyclerview.widget.RecyclerView? = null

    private var date: TextView? = null
    private var team1: TextView? = null
    private var team2: TextView? = null
    private var color: Int? = null
    private var color2: Int? = null

    //private var numberOfPoints: TextView? = null

    init {
        day = itemView.findViewById(R.id.day)
        eventRv = itemView.findViewById(R.id.eventRv)
        day_bar = itemView.findViewById(R.id.dayBar)
    }

    fun bind(dayOfWeek: String) {
        day?.text = dayOfWeek
        Color.parseColor(
            sharedPreferences.getString(
                Constants.PRIMARY_COLOR,
                R.color.colorPrimary.toString()
            )
        ).also { color = it }
        Color.parseColor(
            sharedPreferences.getString(
                Constants.SECONDARY_COLOR,
                R.color.colorYellow.toString()
            )
        ).also { color2 = it }
        day?.setTextColor(color!!)
        day_bar?.background = ColorDrawable(color2!!)
    }
}