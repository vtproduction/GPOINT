package com.jedmahonisgroup.gamepoint.adapters.friends

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.RequestModelUi
import com.jedmahonisgroup.gamepoint.model.feeds.FriendRequest
import com.jedmahonisgroup.gamepoint.ui.managfriends.MFlistner


class FriendsRequesViewHolder(inflater: LayoutInflater, private val parent: ViewGroup) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(inflater.inflate(R.layout.item_friend_request, parent, false)) {

    private var TAG: String = FriendsRequesViewHolder::class.java.simpleName

    private var mText: TextView? = null
    private var mdeleteBtn: Button? = null
    private var mconfirmBtn: Button? = null
    private var mFullName: TextView? = null
    private var mNickname: TextView? = null
    private var mAvatar: ImageView? = null






    init {

        mText = itemView.findViewById(R.id.textView5)

        mdeleteBtn = itemView.findViewById(R.id.delete_button)
        mconfirmBtn = itemView.findViewById(R.id.confirmfrind)
        mFullName = itemView.findViewById(R.id.textView5)
        mNickname = itemView.findViewById(R.id.textView6)
        mAvatar = itemView.findViewById(R.id.friend_request_profile_image)


    }

    fun bind(data: RequestModelUi, mListener: MFlistner, position: Int) {

        mText?.text = data.user!!.first_name


        mdeleteBtn!!.setOnClickListener {

            mListener.Request("denied",data.id!! , position)
        }

        mconfirmBtn!!.setOnClickListener {

            mListener.Request("accepted",data.id!! , position)
        }

        Log.e("user is", data.toString())

        mAvatar?.setOnClickListener {
            mListener?.visitprofilewithapproval(data.user, data.approval.toString())
        }


        mNickname?.setOnClickListener {
            mListener?.visitprofilewithapproval(data.user, data.approval.toString())
        }


        mFullName?.setOnClickListener {
            mListener?.visitprofilewithapproval(data.user, data.approval.toString())
        }






    }
}
