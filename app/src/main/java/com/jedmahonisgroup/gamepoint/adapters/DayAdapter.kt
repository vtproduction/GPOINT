package com.jedmahonisgroup.gamepoint.adapters

import android.graphics.Color
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.GamePointResultListener
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.events.EventsModel
import com.jedmahonisgroup.gamepoint.ui.events.EventsFragment
import com.jedmahonisgroup.gamepoint.utils.StringFormatter
import com.jedmahonisgroup.gamepoint.utils.StringFormatter.dayOfYearToDate


class DayAdapter( eventsFragment: GamePointResultListener?, private val schoolColor: String?, private val schoolSecondaryColor: String?) : androidx.recyclerview.widget.RecyclerView.Adapter<DayAdapter.ViewHolder>() {
    private val listner: GamePointResultListener? = eventsFragment
    private val eventList: ArrayList<EventsModel> = ArrayList()
    private val mDayList: ArrayList<Int> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.day_card, null)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDayList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data: Int = mDayList[position]
        //val weeklyDateOfYear = StringFormatter.convertTimestampToDate(data)
        val dayOfWeek: String



        dayOfWeek = when (data) {
            StringFormatter.getDayOfYear(StringFormatter.getTodayDate().toString()) -> "Today's Events"
            StringFormatter.getDayOfYear(StringFormatter.getTomorrowDate().toString()) -> "Tomorrow's Events"

            //display mDate in Aug, 8, 2019 format
            else -> StringFormatter.dayOfyearToNameDate(data)
        }

        holder.itemView
        holder.bind(dayOfWeek)

       // display event adapter
        val adapter = EventAdapter(schoolColor)
        holder.mEventRv?.adapter = adapter
        holder.mEventRv?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(holder.mEventRv?.context)
//


//        holder.mDayParent = holder.itemView.findViewById(R.id.day_parent)
//        holder.mDayParent.setBackgroundColor(Color.parseColor(schoolSecondaryColor))


//        //create a list with events grouped by day.
        val newList = ArrayList<EventsModel>()
        for (event in eventList) {
            val dateOfEvent = StringFormatter.convertTimestampToDate(event.startTime)
            if (dayOfYearToDate(data) == dateOfEvent ) {
                newList.add(event)
            }
        }
        val gSon = Gson()
        val  userString = gSon.toJson(newList)

//
        adapter.refreshList(newList, listner)


    }

    fun refreshList(dayList: Array<Int>, events: List<EventsModel>) {
        if (itemCount != 0){
            this.mDayList.clear()
            this.eventList.clear()
        }
        mDayList.addAll(dayList)
        eventList.addAll(events)

        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        private var mDay: TextView? = itemView.findViewById(R.id.day)

        var mEventRv: androidx.recyclerview.widget.RecyclerView? = itemView.findViewById(R.id.eventRv)

       //var mDayBackgroundView: View = itemView.findViewById(R.id.day_background_view)


        fun bind(dayOfWeek: String) {
            mDay?.text = dayOfWeek
            //mDay?.setTextColor(Color.parseColor(schoolColor))
            //mDayBackgroundView.setBackgroundColor(Color.parseColor(schoolSecondaryColor))
        }
    }


}