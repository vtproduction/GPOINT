package com.jedmahonisgroup.gamepoint.adapters.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.RequestModelUi
import com.jedmahonisgroup.gamepoint.ui.managfriends.MFlistner


class FriendsRequesAdapter(private val response: ArrayList<RequestModelUi>, managefragment: MFlistner?) : androidx.recyclerview.widget.RecyclerView.Adapter<FriendsRequesViewHolder>() {


    private var mListener: MFlistner = managefragment!!


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsRequesViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return FriendsRequesViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
        return response.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun onBindViewHolder(holder: FriendsRequesViewHolder, position: Int) {

        val data: RequestModelUi = response[position]
        holder.bind(data , mListener , position)

    }


}
