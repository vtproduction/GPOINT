package com.jedmahonisgroup.gamepoint.adapters.feeds

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.jedmahonisgroup.gamepoint.model.FeedsUi
import com.jedmahonisgroup.gamepoint.ui.feeds.FFListner


class FeedsAdapter(private val response: List<FeedsUi> , fragFeeds: FFListner) : androidx.recyclerview.widget.RecyclerView.Adapter<FeedsViewHolder>() {

    private var fragmentFeeds = fragFeeds


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return FeedsViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
        return response.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: FeedsViewHolder, position: Int) {

        val data: FeedsUi = response[position]
        holder.bind(data , fragmentFeeds)


    }


}
