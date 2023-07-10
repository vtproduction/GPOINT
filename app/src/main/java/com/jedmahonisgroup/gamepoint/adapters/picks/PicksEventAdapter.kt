package com.jedmahonisgroup.gamepoint.adapters.picks

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jedmahonisgroup.gamepoint.RecyclerviewClickListener
import com.jedmahonisgroup.gamepoint.model.picks.OpenPicksModel

class PicksEventAdapter : RecyclerView.Adapter<PicksEventViewHolder>() {

    private val response: ArrayList<OpenPicksModel> = ArrayList()
    private var mListener: RecyclerviewClickListener? = null
    private var mContext: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PicksEventViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PicksEventViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
        return response.size
    }

    override fun onBindViewHolder(holder: PicksEventViewHolder, position: Int) {
        val data: OpenPicksModel = response[position]
        if (position == itemCount - 1) {
            holder.separator?.visibility = View.INVISIBLE
        }

        holder.bind(data, mListener, mContext)
    }



    fun refreshList(newList: ArrayList<OpenPicksModel>, listner: RecyclerviewClickListener, mCntxt: Context?) {
        if (itemCount != 0) this.response.clear()
        mListener = listner
        mContext = mCntxt
        this.response.clear()
        response.addAll(newList)
        notifyDataSetChanged()

    }
}