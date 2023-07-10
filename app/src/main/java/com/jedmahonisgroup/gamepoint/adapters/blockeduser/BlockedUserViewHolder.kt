package com.jedmahonisgroup.gamepoint.adapters.blockeduser

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.RequestModel
import com.jedmahonisgroup.gamepoint.model.RequestModelUi
import com.jedmahonisgroup.gamepoint.model.feeds.User
import com.jedmahonisgroup.gamepoint.ui.managfriends.MFlistner


class BlockedUserViewHolder(inflater: LayoutInflater, private val parent: ViewGroup) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(inflater.inflate(R.layout.item_blocked_user, parent, false)) {

    private var TAG: String = BlockedUserViewHolder::class.java.simpleName

    private var mFullName: TextView? = null
    private var mNickname: TextView? = null
    private var mAvatar: ImageView? = null




    init {

        mFullName = itemView.findViewById(R.id.realName)

        mNickname = itemView.findViewById(R.id.userName)

        mAvatar = itemView.findViewById(R.id.friend_request_profile_image)
    }

    fun bind(data: RequestModelUi, position: Int) {

        mFullName?.text = data.friend!!.first_name + " " + data.user!!.last_name

        mNickname?.text = data.friend!!.nickname

        Glide.with(itemView).load(data.friend!!.avatar).circleCrop().error(R.drawable.ic_user_account).into(mAvatar!!)









    }
}
