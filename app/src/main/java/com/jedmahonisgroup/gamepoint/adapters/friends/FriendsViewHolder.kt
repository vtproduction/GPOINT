package com.jedmahonisgroup.gamepoint.adapters.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.RequestModel
import com.jedmahonisgroup.gamepoint.model.feeds.User
import com.jedmahonisgroup.gamepoint.ui.managfriends.MFlistner


class FriendsViewHolder(inflater: LayoutInflater, private val parent: ViewGroup) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(inflater.inflate(R.layout.item_my_friends, parent, false)) {

    private var TAG: String = FriendsViewHolder::class.java.simpleName

    private var mFullName: TextView? = null
    private var mNickname: TextView? = null
    private var mAvatar: ImageView? = null




    init {

        mFullName = itemView.findViewById(R.id.realName)

        mNickname = itemView.findViewById(R.id.userName)

        mAvatar = itemView.findViewById(R.id.friend_request_profile_image)
    }

    fun bind(data: User, mListener: MFlistner, position: Int) {

        mFullName?.text = data.first_name + " " + data.last_name

        mNickname?.text = data.nickname

        Glide.with(itemView).load(data.avatar).circleCrop().error(R.drawable.ic_user_account).into(mAvatar!!)

        mAvatar?.setOnClickListener {
            mListener?.visitprofile(data)
        }
        mNickname?.setOnClickListener {
            mListener?.visitprofile(data)
        }
        mFullName?.setOnClickListener {
            mListener?.visitprofile(data)
        }





    }
}
