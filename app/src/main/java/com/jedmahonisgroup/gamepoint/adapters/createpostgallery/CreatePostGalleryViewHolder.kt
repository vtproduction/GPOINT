package com.jedmahonisgroup.gamepoint.adapters.createpostgallery

import android.annotation.SuppressLint
import android.opengl.Visibility
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.CreatPostUi

class CreatePostGalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    var mImageView: ImageView? = null
    var mImageSelected: ImageView? = null


    init {
        mImageView = itemView.findViewById(R.id.create_post_image_view)
        mImageSelected = itemView.findViewById(R.id.create_post_image_view_Selected)
    }

    @SuppressLint("WrongConstant")
    fun bind(data : CreatPostUi){
        mImageSelected?.visibility = View.INVISIBLE
        Glide.with(itemView.getContext())
                .load(data.path).placeholder(R.drawable.ic_photo_black_48dp).error(R.drawable.no_access_to_photos).into(mImageView!!)

        if(data.selected){
            mImageSelected?.visibility = View.VISIBLE
        }



    }
}


