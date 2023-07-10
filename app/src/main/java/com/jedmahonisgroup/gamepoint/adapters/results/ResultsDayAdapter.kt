package com.jedmahonisgroup.gamepoint.adapters.results

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.model.picks.MyPicksModel
import com.jedmahonisgroup.gamepoint.utils.StringFormatter
import com.jedmahonisgroup.gamepoint.utils.StringFormatter.dayOfYearToDate

class ResultsDayAdapter() : androidx.recyclerview.widget.RecyclerView.Adapter<ResultsDayViewHolder>() {

    private val TAG: String = ResultsDayAdapter::class.java.simpleName

    private val mDayList: ArrayList<Int> = ArrayList()
    private val picksList: ArrayList<MyPicksModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsDayViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ResultsDayViewHolder(inflater, parent)
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

    override fun onBindViewHolder(holder: ResultsDayViewHolder, position: Int) {
        val data: Int = mDayList[position]
        val dayOfWeek: String

        dayOfWeek = when (data) {
            StringFormatter.getDayOfYear(StringFormatter.getTodayDate().toString()) -> "Today"
            StringFormatter.getDayOfYear(StringFormatter.getTomorrowDate().toString()) -> "Tomorrow"

            //display mDate in Aug, 8, 2019 format
            else -> StringFormatter.dayOfyearToNameDate(data)
        }

        holder.itemView
        holder.bind(dayOfWeek)


        //display event adapter
        val adapter = ResultsAdapterEvent()
        holder.eventRv?.adapter = adapter
        holder.eventRv?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(holder.eventRv?.context)

        //create a list with events grouped by day.
        val newList = ArrayList<MyPicksModel>()
        for (event in picksList) {
            val dateOfEvent = StringFormatter.convertTimestampToDate(event.start_time)
            if (dayOfYearToDate(data) == dateOfEvent) {
                newList.add(event)
            }
        }
        val gSon = Gson()
        val  userString = gSon.toJson(newList)
        Log.i(TAG, "new list - > :$userString")


        adapter.refreshList(newList)
    }

    fun refreshList(dayList: Array<Int>, events: List<MyPicksModel>) {
        if (itemCount != 0){
            this.mDayList.clear()
            this.picksList.clear()
        }
        mDayList.addAll(dayList)
        picksList.addAll(events)

        notifyDataSetChanged()
    }

}