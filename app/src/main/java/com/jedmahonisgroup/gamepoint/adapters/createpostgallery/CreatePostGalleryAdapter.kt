package com.jedmahonisgroup.gamepoint.adapters.createpostgallery


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.CreatPostUi


 class CreatePostGalleryAdapter(var itemCreatePostUI: ArrayList<CreatPostUi>, var context: Context?) : RecyclerView.Adapter<CreatePostGalleryViewHolder>() {



     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreatePostGalleryViewHolder {
        val itemHolder = LayoutInflater.from(parent.context).inflate(R.layout.row_items_create_post,parent,false)
         return CreatePostGalleryViewHolder(itemHolder)
     }

     @SuppressLint("WrongConstant")
     override fun onBindViewHolder(holder: CreatePostGalleryViewHolder, position: Int) {
         var a:CreatPostUi = itemCreatePostUI.get(position)
         holder.bind(a)

     }

     override fun getItemCount(): Int {
         return itemCreatePostUI.size
     }


 }