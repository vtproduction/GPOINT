package com.jedmahonisgroup.gamepoint.adapters.createpostgallery


import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.jedmahonisgroup.gamepoint.model.CommentsRModel
import com.jedmahonisgroup.gamepoint.model.events.EventsModel
import com.jedmahonisgroup.gamepoint.model.events.EventsModelUi
import com.jedmahonisgroup.gamepoint.ui.comments.CFListner


class CreatePostEventAdapter(var itemEvent: ArrayList<EventsModelUi>) : androidx.recyclerview.widget.RecyclerView.Adapter<CreatePostEventViewHolder>() {



     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreatePostEventViewHolder {
         val inflater = LayoutInflater.from(parent.context)
         return CreatePostEventViewHolder(inflater, parent)
     }

     @RequiresApi(Build.VERSION_CODES.N)
     override fun onBindViewHolder(holder: CreatePostEventViewHolder, position: Int) {
         holder.bind(itemEvent[position])

     }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

     override fun getItemCount(): Int {
         return itemEvent.size
     }


 }