package com.jedmahonisgroup.gamepoint.adapters.feeds

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
import com.ddd.androidutils.DoubleClick
import com.ddd.androidutils.DoubleClickListener
import com.facebook.FacebookSdk.getApplicationContext
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.FeedsUi
import com.jedmahonisgroup.gamepoint.ui.feeds.FFListner
import com.jedmahonisgroup.gamepoint.utils.TimeAgo


class FeedsViewHolder(inflater: LayoutInflater, private val parent: ViewGroup) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(inflater.inflate(R.layout.item_feeds, parent, false)) {

    private var TAG: String = FeedsViewHolder::class.java.simpleName
    private var mFeedsPostEventName: TextView? = null
    private var mBodyText: TextView? = null
    private var mFeedsUserIcon: ImageView? = null
    private var mFeedsPostImage: ImageView? = null
    private var mFeedsPostPostedTime: TextView? = null
    private var mFeedsPostUserName: TextView? = null
    private var mFeedsPostCommentCount: TextView? = null
    private var mFeedsPostLikeCount: TextView? = null
    private var mFeedsPostCommentIcon: ImageView? = null
    private var mFeedsPostLikeIcon: ImageView? = null
    private var mFeedsReportPopUp: ImageView? = null
    private var mFeedsBigHEart: ImageView? = null



    init {

        mFeedsPostEventName = itemView.findViewById(R.id.itemFeedsName)
        mBodyText = itemView.findViewById(R.id.itemFeedsBody)
        mFeedsUserIcon = itemView.findViewById(R.id.itemFeedsIconUser)
        mFeedsPostImage = itemView.findViewById(R.id.itemFeedsImage)
        mFeedsPostPostedTime = itemView.findViewById(R.id.itemFeedsTime)
        mFeedsPostPostedTime = itemView.findViewById(R.id.itemFeedsTime)
        mFeedsPostCommentCount = itemView.findViewById(R.id.itemFeedsCommentCount)
        mFeedsPostLikeCount = itemView.findViewById(R.id.itemFeedsLikeCount)
        mFeedsPostCommentIcon = itemView.findViewById(R.id.itemFeedsPostCommentIcon)
        mFeedsPostLikeIcon = itemView.findViewById(R.id.itemFeedsPostLikeIcon)
        mFeedsReportPopUp =  itemView.findViewById(R.id.userImageViewAction)
        mFeedsBigHEart = itemView.findViewById(R.id.itemFeedsBigHeartImage)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun bind(data: FeedsUi, fragmentFeeds: FFListner) {
        mFeedsPostEventName?.text = data.event.name;
        val text = "<font color=#7a0019><b>"+ data.user.nickname + "</b></font> <font color=#000000>" + data.body + "</font>"

        mBodyText?.text = Html.fromHtml(text,3)

        mFeedsPostLikeCount?.text = data.like_count.toString()
        mFeedsPostPostedTime?.text = TimeAgo.covertTimeToText(data.created_at)
        mFeedsPostCommentCount?.text = data.comment_count.toString()


        mFeedsReportPopUp?.setOnClickListener {
            fragmentFeeds.reportPopUp(data.id,data.user.id.toString(),data.id.toString())
        }

        val doubleClick = DoubleClick(object : DoubleClickListener {
            override fun onSingleClickEvent(view: View?) {
            }

            override fun onDoubleClickEvent(view: View?) {
                val aniSlide: Animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomout)
                val anifade: Animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade)

                if (!data.liked){
                    mFeedsBigHEart?.visibility = View.VISIBLE

                    anifade.setAnimationListener(object: AnimationListener{
                        override fun onAnimationStart(animation: Animation) {
                            // TODO Auto-generated method stub
                        }

                        override fun onAnimationRepeat(animation: Animation) {
                            // TODO Auto-generated method stub
                        }
                        override fun onAnimationEnd(animation: Animation) {
                            fragmentFeeds.likePost(data.id)
                            data.liked = true
                            data.like_count++
                        }

                    })

                    aniSlide.setAnimationListener(object : AnimationListener {
                        override fun onAnimationStart(animation: Animation) {
                            // TODO Auto-generated method stub
                        }

                        override fun onAnimationRepeat(animation: Animation) {
                            // TODO Auto-generated method stub
                        }

                        override fun onAnimationEnd(animation: Animation) {
                            mFeedsBigHEart?.startAnimation(anifade)
                        }
                    })
                    mFeedsBigHEart?.startAnimation(aniSlide)


                    mFeedsBigHEart?.visibility = View.INVISIBLE

                }

            }
        })


        mFeedsPostImage?.setOnClickListener(doubleClick)
        mFeedsPostLikeIcon?.setOnClickListener {

            if (data.liked){
                fragmentFeeds.dislikePost(data.id)
                data.liked = false
                data.like_count--
            }else{

                fragmentFeeds.likePost(data.id)
                data.liked = true
                data.like_count++
            }

        }


        if (data.liked){

            Glide.with(this.itemView)
                    .load(R.drawable.ic_heart)
                    .apply(RequestOptions().override(90, 90))
                    .into(mFeedsPostLikeIcon!!)

        }else{

            Glide.with(this.itemView)
                    .load(R.drawable.ic_heart_outline)
                    .apply(RequestOptions().override(90, 90))
                    .into(mFeedsPostLikeIcon!!)

        }


        Glide.with(this.itemView)
                .load(R.drawable.ic_comment_outline)
                .apply(RequestOptions().override(90, 90))
                .into(mFeedsPostCommentIcon!!)




        if (!data.user.avatar.isNullOrEmpty()) {


            Glide.with(this.itemView)
                    .load(data.user.avatar)
                    .circleCrop()
                    .error(R.drawable.ic_user_account)
                    .into(mFeedsUserIcon!!)

        }

        if (!data.image.isNullOrEmpty()) {

            Glide.with(this.itemView)
                    .load(data.image)
                    .error(R.drawable.no_access_to_photos)
                    .into(mFeedsPostImage!!)


        }

        mFeedsPostCommentIcon?.setOnClickListener {
            fragmentFeeds.commentfrag(data.id)
        }

        mFeedsUserIcon?.setOnClickListener {
            fragmentFeeds.visitprofile(data.user)
        }


    }
}
