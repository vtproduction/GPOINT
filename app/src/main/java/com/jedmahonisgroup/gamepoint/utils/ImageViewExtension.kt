package com.jedmahonisgroup.gamepoint.utils

import android.widget.ImageView
import com.squareup.picasso.Picasso
import java.io.File



fun ImageView.setRemoteImage(url: String, applyCircle: Boolean = false) {
    //val glide = Glide.with(this).load(url)
    val picasso = Picasso.get().load(url)
    if (applyCircle) {
        picasso.transform(CircleTransform()).into(this)
    } else {
        picasso.into(this)
    }
}

fun ImageView.setLocalImage(file: File, applyCircle: Boolean = false) {
    val picasso = Picasso.get().load(file)

    if (applyCircle) {
        picasso.transform(CircleTransform()).into(this)
    } else {
        picasso.into(this)
    }
}
