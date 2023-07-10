package com.jedmahonisgroup.gamepoint.adapters.comments

import android.annotation.SuppressLint
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.CommentsRModel
import com.jedmahonisgroup.gamepoint.ui.comments.CFListner


class CommentsViewHolder(inflater: LayoutInflater, private val parent: ViewGroup) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(inflater.inflate(R.layout.item_comment, parent, false)) {
    var mImageView: ImageView? = null
    var mButtonReport: ImageButton? = null

    var mBodyText: TextView? = null

    init {
        mImageView = itemView.findViewById(R.id.myUserAvatarComment)
        mBodyText = itemView.findViewById(R.id.myUserCommentBody)
        mButtonReport = itemView.findViewById(R.id.buttonreportcomment)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("WrongConstant")
    fun bind(data: CommentsRModel , commentFragment : CFListner){
        Glide.with(itemView.getContext())
                .load(data.user.avatar).circleCrop().error(R.drawable.ic_user_account).into(mImageView!!)

        val text = "<font color=#7a0019><b>"+ data.user.nickname + "</b></font> <font color=#000000>" + data.body + "</font>"

        mBodyText?.text = Html.fromHtml(text,1)

        mButtonReport?.setOnClickListener {
            commentFragment?.reportpopup(data.post_id.toString() , data.id.toString() ,data.user.id.toString())
        }






    }
}


