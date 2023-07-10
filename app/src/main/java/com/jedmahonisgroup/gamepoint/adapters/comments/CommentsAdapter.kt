package com.jedmahonisgroup.gamepoint.adapters.comments


import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.jedmahonisgroup.gamepoint.model.CommentsRModel
import com.jedmahonisgroup.gamepoint.ui.comments.CFListner


class CommentsAdapter(var itemCommentsList: ArrayList<CommentsRModel> , commentFragment : CFListner) : androidx.recyclerview.widget.RecyclerView.Adapter<CommentsViewHolder>() {


    var commentFragment : CFListner = commentFragment

     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder {
         val inflater = LayoutInflater.from(parent.context)
         return CommentsViewHolder(inflater, parent)
     }

     @RequiresApi(Build.VERSION_CODES.N)
     override fun onBindViewHolder(holder: CommentsViewHolder, position: Int) {
         var data = itemCommentsList[position]
         holder.bind(data , commentFragment)

     }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

     override fun getItemCount(): Int {
         return itemCommentsList.size
     }


 }