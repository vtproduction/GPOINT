package com.jedmahonisgroup.gamepoint.adapters.notification

import android.R.attr.animation
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.FacebookSdk.getApplicationContext
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.FeedsUi
import com.jedmahonisgroup.gamepoint.model.notificationResponseModel
import com.jedmahonisgroup.gamepoint.ui.feeds.FFListner
import com.jedmahonisgroup.gamepoint.utils.TimeAgo


class notificationViewHolder(inflater: LayoutInflater, private val parent: ViewGroup) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(inflater.inflate(R.layout.item_notification, parent, false)) {

    private var TAG: String = notificationViewHolder::class.java.simpleName

    private var mbodyText : TextView? = null
    private var mtimeText : TextView? = null



    init {

        mbodyText = itemView.findViewById(R.id.notificationbody)
        mtimeText = itemView.findViewById(R.id.notificationtime)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun bind(data: notificationResponseModel) {

        mbodyText?.text = data.body
        mtimeText?.text = TimeAgo.covertTimeToText(data.sent_at)


    }
}
