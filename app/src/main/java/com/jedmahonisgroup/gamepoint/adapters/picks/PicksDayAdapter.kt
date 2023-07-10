package com.jedmahonisgroup.gamepoint.adapters.picks

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.RecyclerviewClickListener
import com.jedmahonisgroup.gamepoint.model.picks.OpenPicksModel
import com.jedmahonisgroup.gamepoint.ui.picks.PicksFragment
import com.jedmahonisgroup.gamepoint.utils.StringFormatter

class PicksDayAdapter(picksFragment: PicksFragment) : RecyclerView.Adapter<PicksDayViewHolder>() {

    private val TAG: String = PicksDayAdapter::class.java.simpleName
   // private val response: List<MyPicksModel> = mResponse
    private val listener: RecyclerviewClickListener = picksFragment

    private val mDayList: ArrayList<Int> = ArrayList()
    private val openPickList: ArrayList<OpenPicksModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PicksDayViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PicksDayViewHolder(inflater, parent)
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

    override fun onBindViewHolder(holder: PicksDayViewHolder, position: Int) {
        val data: Int = mDayList[position]
        val dayOfWeek: String

        Log.i(TAG, "converted === > ${StringFormatter.dayOfYearToDate(data)}")

        dayOfWeek = when (data) {
            StringFormatter.getDayOfYear(StringFormatter.getTodayDate().toString()) -> "Today"
            StringFormatter.getDayOfYear(StringFormatter.getTomorrowDate().toString()) -> "Tomorrow"

            //display mDate in Aug, 8, 2019 format
            else -> StringFormatter.dayOfyearToNameDate(data)
        }
        holder.itemView
        holder.bind(dayOfWeek)


        //display event adapter
        val adapter = PicksEventAdapter()
        holder.eventRv?.adapter = adapter
        holder.eventRv?.layoutManager = LinearLayoutManager(holder.eventRv?.context)

        //create a list with events grouped by day.
        val newList = ArrayList<OpenPicksModel>()
        for (event in openPickList) {
            val dateOfEvent = StringFormatter.convertTimestampToDate(event.start_time)
            if (StringFormatter.dayOfYearToDate(data) == dateOfEvent) {
                newList.add(event)
            }
        }

        val gSon = Gson()
        val  userString = gSon.toJson(newList)
        Log.i(TAG, "new list - > :$userString")

        adapter.refreshList(newList, listener, holder.eventRv!!.context)

    }

    fun refreshList(dayList: Array<Int>, events: List<OpenPicksModel>) {
        if (itemCount != 0){
            this.mDayList.clear()
            this.openPickList.clear()
        }
        mDayList.addAll(dayList)
        openPickList.addAll(events)

        notifyDataSetChanged()
    }

}
