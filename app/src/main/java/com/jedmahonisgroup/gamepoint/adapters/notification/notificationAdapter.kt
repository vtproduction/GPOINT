package com.jedmahonisgroup.gamepoint.adapters.notification

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.jedmahonisgroup.gamepoint.model.FeedsUi
import com.jedmahonisgroup.gamepoint.model.notificationResponseModel
import com.jedmahonisgroup.gamepoint.ui.feeds.FFListner


class notificationAdapter(private val response: List<notificationResponseModel>) : androidx.recyclerview.widget.RecyclerView.Adapter<notificationViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): notificationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return notificationViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
        return response.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: notificationViewHolder, position: Int) {

        val data: notificationResponseModel = response[position]
        holder.bind(data)


    }


}
