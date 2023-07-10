package com.jedmahonisgroup.gamepoint.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jedmahonisgroup.gamepoint.GamePointResultListener
import com.jedmahonisgroup.gamepoint.model.events.EventsModel


class EventAdapter(private val schoolColor: String?) : androidx.recyclerview.widget.RecyclerView.Adapter<EventViewHolder>() {
    private val response: ArrayList<EventsModel> = ArrayList()
    private var mListener: GamePointResultListener? = null

    private val TAG: String = EventAdapter::class.java.simpleName
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return EventViewHolder(inflater, parent, schoolColor)
    }

    override fun getItemCount(): Int {
        return response.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {

        val data: EventsModel = response[position]
        /*last edit text in rv change click action to done*/
        /*if (position == itemCount - 1) {
            holder.separator?.visibility = View.INVISIBLE
        }*/


        holder.bind(data, mListener)
    }

    fun refreshList(newList: ArrayList<EventsModel>, listner: GamePointResultListener?) {
        this.response.clear()
        mListener = listner

        this.response.clear()
        response.addAll(newList)

        notifyDataSetChanged()

    }


}