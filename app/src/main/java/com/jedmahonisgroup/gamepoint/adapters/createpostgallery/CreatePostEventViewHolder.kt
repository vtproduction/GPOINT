package com.jedmahonisgroup.gamepoint.adapters.createpostgallery

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.ColorFilter
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.CreatPostUi
import com.jedmahonisgroup.gamepoint.model.events.EventsModel
import com.jedmahonisgroup.gamepoint.model.events.EventsModelUi

class CreatePostEventViewHolder(inflater: LayoutInflater, private val parent: ViewGroup) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(inflater.inflate(R.layout.item_events_feeds_create_post_2, parent, false)) {
    var mImageView: ImageView? = null
    var mImageSelected: TextView? = null


    init {
        mImageView = itemView.findViewById(R.id.Imagemapmarkerrr)
        mImageSelected = itemView.findViewById(R.id.textView)
    }

    @SuppressLint("WrongConstant")
    fun bind(data : EventsModelUi){


            if (!data.selected){
                mImageView?.setColorFilter(Color.GRAY)
            }else{
                mImageView?.setColorFilter(Color.parseColor("#7a0019"))
            }

            mImageSelected?.text = data.name




    }
}


