package com.jedmahonisgroup.gamepoint.adapters.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import com.jedmahonisgroup.gamepoint.model.feeds.User
import com.jedmahonisgroup.gamepoint.ui.managfriends.MFlistner


class FriendsAdapter(private val response: ArrayList<User> , manageFriends : MFlistner) : androidx.recyclerview.widget.RecyclerView.Adapter<FriendsViewHolder>() {


    private var mListener: MFlistner? = manageFriends


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return FriendsViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
        return response.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {

        val data: User = response[position]
        holder.bind(data , mListener!! , position)

    }


}
