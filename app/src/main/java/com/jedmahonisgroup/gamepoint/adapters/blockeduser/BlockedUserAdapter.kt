package com.jedmahonisgroup.gamepoint.adapters.blockeduser

import android.view.LayoutInflater
import android.view.ViewGroup
import com.jedmahonisgroup.gamepoint.model.RequestModelUi
import com.jedmahonisgroup.gamepoint.model.feeds.User


class BlockedUserAdapter(private val response: ArrayList<RequestModelUi>) : androidx.recyclerview.widget.RecyclerView.Adapter<BlockedUserViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedUserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return BlockedUserViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
        return response.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun onBindViewHolder(holder: BlockedUserViewHolder, position: Int) {

        val data: RequestModelUi = response[position]
        holder.bind(data  , position)

    }


}
