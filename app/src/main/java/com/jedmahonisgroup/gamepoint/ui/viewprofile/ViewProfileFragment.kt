package com.jedmahonisgroup.gamepoint.ui.viewprofile

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.adapters.feeds.FeedsAdapter
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.*

import com.jedmahonisgroup.gamepoint.model.feeds.PostsModel
import com.jedmahonisgroup.gamepoint.model.feeds.User
import com.jedmahonisgroup.gamepoint.ui.comments.CommentsFragment
import com.jedmahonisgroup.gamepoint.ui.feeds.FFListner
import com.jedmahonisgroup.gamepoint.ui.feeds.FeedsViewModel
import kotlinx.android.synthetic.main.fragment_feeds.*
import kotlinx.android.synthetic.main.fragment_feeds_view_profile.*
import kotlinx.android.synthetic.main.report_pop_up_alert.view.*
import java.util.ArrayList
import kotlinx.android.synthetic.main.fragment_feeds_view_profile.feedsNoDataView as feedsNoDataView1


class ViewProfileFragment : Fragment(), FFListner {

    private var token: String? = null


    private var TOKEN: String? = null
    private var mUser: UserResponse? = null

    private lateinit var feedsviewprofileViewModel: FeedsViewModel


    private var UserProfile: User? = null
    private var typeOfFeeds: String = "P"
    private var user: String? = null
    private var approval: String? = null


    private var mFullname: TextView? = null
    private var mAvatar: ImageView? = null
    private var mNickname: TextView? = null
    private var mFriends: TextView? = null
    private var mPosts: TextView? = null
    private var mBack: Button? = null
    private var mAddfriend: Button? = null
    private var mViewProfileReport: ImageView? = null
    private var mConfirmFriend: Button? = null

    private var mProgressBar: ProgressBar? = null
    private var mProgressBarAF: ProgressBar? = null



    private var binding: ViewDataBinding? = null


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        feedsviewprofileViewModel = ViewModelProviders.of(this, ViewModelFactory(this.activity as AppCompatActivity)).get(FeedsViewModel::class.java)


        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_feeds_view_profile, container, false)
        val rootView = binding?.root

        getToken()

        setUpUi(rootView)

        responseFromServer()

        return rootView

    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun setUpUi(rootView: View?) {

        mFullname = rootView?.findViewById(R.id.view_profile_fullname)
        mAvatar = rootView?.findViewById(R.id.view_profile_picture)
        mNickname = rootView?.findViewById(R.id.view_profile_nickname)
        mFriends = rootView?.findViewById(R.id.view_profile_friends)
        mPosts = rootView?.findViewById(R.id.view_profile_post)
        mBack = rootView?.findViewById(R.id.view_profile_back)
        mProgressBar = rootView?.findViewById(R.id.progressBarReady)
        mProgressBarAF = rootView?.findViewById(R.id.progressBarAFReady)
        mAddfriend = rootView?.findViewById(R.id.view_profile_addfriend)
        mViewProfileReport = rootView?.findViewById(R.id.view_profile_report)
        mConfirmFriend = rootView?.findViewById(R.id.view_profile_confirmfriend)



        Glide.with(rootView!!).load(UserProfile?.avatar).error(R.drawable.no_access_to_photos).into(mAvatar!!)


        mFullname?.text = UserProfile?.first_name + " " + UserProfile?.last_name
        mNickname?.text = "@" + UserProfile?.nickname
        mFriends?.text = if (UserProfile?.friend_count!! > 1) UserProfile?.friend_count.toString() + " Friends" else UserProfile?.friend_count.toString() + " Friend"


        feedsviewprofileViewModel?.changefeedstype(TOKEN!!, typeOfFeeds, UserProfile?.id.toString()!!)

        mBack?.setOnClickListener {

            fragmentManager?.popBackStack()

        }
        if (UserProfile?.friend!!) {
            mAddfriend?.text = "Unfriend"

            mAddfriend?.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.colorDefaultGrey)));

            mAddfriend?.setOnClickListener {

                feedsviewprofileViewModel?.deletefriend(TOKEN!!,typeOfFeeds, UserProfile)

                mAddfriend?.setBackgroundTintList(ColorStateList.valueOf(resources.getColor(R.color.colorPrimary)));

                mAddfriend?.text = "Add Friend"


            }



        } else {
            Log.e("user profile is ", UserProfile.toString())
            mAddfriend?.isClickable = false
            if (UserProfile?.id != mUser?.id) {

                if (UserProfile?.friend_request?.toString() != null){
                    if (UserProfile?.friend_request?.approval == "pending") {
                        mConfirmFriend?.visibility = View.VISIBLE
                        mAddfriend?.visibility = View.INVISIBLE
                        mConfirmFriend?.text = "Pending"
                    } else {
                        mAddfriend?.setOnClickListener {

                            var sendId: FriendSentRequestModel = FriendSentRequestModel(friend_request = null)
                            sendId.friend_request = FriendId(
                                    friend_id = UserProfile!!.id
                            )

                            feedsviewprofileViewModel?.sendRequest(TOKEN!!, sendId)

                            mProgressBarAF?.visibility = View.VISIBLE
                            mAddfriend?.text = ""


                        }
                    }
                }else{
                if (approval == "pending") {
                    mConfirmFriend?.visibility = View.VISIBLE
                    mAddfriend?.visibility = View.INVISIBLE

                } else {
                    mAddfriend?.setOnClickListener {

                        var sendId: FriendSentRequestModel = FriendSentRequestModel(friend_request = null)
                        sendId.friend_request = FriendId(
                                friend_id = UserProfile!!.id
                        )

                        feedsviewprofileViewModel?.sendRequest(TOKEN!!, sendId)

                        mProgressBarAF?.visibility = View.VISIBLE
                        mAddfriend?.text = ""
                        mAddfriend?.visibility = View.VISIBLE
                        mConfirmFriend?.visibility = View.INVISIBLE


                    }
                }

                }



            } else {

                mAddfriend?.text = "Your Profile"

            }


        }

        mViewProfileReport?.setOnClickListener {

            var messageBoxView = LayoutInflater.from(activity).inflate(R.layout.report_pop_up_alert, null)

            //AlertDialogBuilder
            var messageBoxBuilder = AlertDialog.Builder(requireContext()).setView(messageBoxView)


//                    messageBoxView.buttonPostDeleteUser.visibility = View.GONE
//                    messageBoxView.buttonPostDeleteUserLine.visibility = View.GONE
//                    messageBoxView.buttonReportPost.visibility = View.GONE
//                    messageBoxView.buttonReportPostLine.visibility = View.GONE
//                    messageBoxView.buttonCommentDeleteUser.visibility = View.GONE
//                    messageBoxView.buttonCommentDeleteUserLine.visibility = View.GONE
//                    messageBoxView.buttonCommentReportUser.visibility = View.GONE
//                    messageBoxView.buttonCommentReportUserLine.visibility = View.GONE

//                    messageBoxView.buttonReportUser.visibility = View.GONE
//                    messageBoxView.buttonReportUserLine.visibility = View.GONE


            messageBoxView.buttonPostDeleteUser.visibility = View.GONE
            messageBoxView.buttonPostDeleteUserLine.visibility = View.GONE
            messageBoxView.buttonReportPost.visibility = View.GONE
            messageBoxView.buttonReportPostLine.visibility = View.GONE
            messageBoxView.buttonCommentDeleteUser.visibility = View.GONE
            messageBoxView.buttonCommentDeleteUserLine.visibility = View.GONE
            messageBoxView.buttonCommentReportUser.visibility = View.GONE
            messageBoxView.buttonCommentReportUserLine.visibility = View.GONE




            val messageBoxInstance = messageBoxBuilder.show()


            if (UserProfile!!.friend) {




            }


            //setting text values

            messageBoxView.buttonReportUser.setOnClickListener {

                feedsviewprofileViewModel?.reportUser(TOKEN!!, UserProfile?.id.toString() , reportModel(reason = "inappropriate"))

                messageBoxInstance.dismiss()


            }

            messageBoxView.buttonBlockUser.setOnClickListener {

                feedsviewprofileViewModel?.blockUser(TOKEN!!, UserBlockModel(friend_request = Blockmodel(friend_id = UserProfile!!.id,approval = "blocked")))
                messageBoxInstance.dismiss()
                fragmentManager?.popBackStack()

            }

            //show dialog


            val lp: WindowManager.LayoutParams = messageBoxInstance.getWindow()!!.getAttributes()
            lp.width = 600
            lp.height = 900

            messageBoxInstance.getWindow()!!.setAttributes(lp)


            //set Listener
            messageBoxView.setOnClickListener() {
                //close dialog
                messageBoxInstance.dismiss()
            }


        }


    }


    private fun getToken() {
        val gson = Gson()
        token = requireArguments().getString("token")
        approval = requireArguments().getString("approval")

        UserProfile = gson.fromJson(requireArguments().getString("iD"), User::class.java)





        TOKEN = token


        mUser = GamePointApplication.shared!!.getCurrentUser(requireContext())
    }


    private fun responseFromServer() {

        // lets observe server data response
//        mSwipeToRefresh?.isRefreshing = true


        feedsviewprofileViewModel.feedsFromServer.observe(viewLifecycleOwner, Observer { feeds ->

            if (feeds != null) {
//                mSwipeToRefresh!!.isRefreshing = false
                Log.i(TAG, "here are the picks data is: $feeds")
                mPosts?.text = if (feeds.size > 1) feeds!!.size!!.toString() + " Posts" else feeds!!.size!!.toString() + " Post"
                mProgressBar?.visibility = View.GONE

                displayData(feeds)


            } else {
                //picks are null
                VPrecycle?.visibility = View.INVISIBLE
                feedsNoDataView?.visibility = View.VISIBLE

            }


        })

        feedsviewprofileViewModel.serverDataFailed.observe(viewLifecycleOwner, Observer { response ->
            Log.e(TAG, "problem fetching picks from server: $response")
            //mSwipeToRefresh!!.isRefreshing = false
//            myFeedsRv?.visibility = View.INVISIBLE
//            feedsNoDataView?.visibility = View.VISIBLE
        })

//        first, try to get data from the database


        feedsviewprofileViewModel.failedFeedsFromDb.observe(viewLifecycleOwner, Observer {
            //            myPicksRLNoData?.visibility = View.INVISIBLE
//            myDataEmptyView?.visibility = View.INVISIBLE

//            myPicksRv?.visibility = View.VISIBLE

            Log.e(TAG, "failed getting feeds: $it")
            try {
                feedsviewprofileViewModel.getMyFeedsFromServer(TOKEN!!)
            } catch (e: NullPointerException) {
                Log.e(TAG, "could not fetch user feeds. Token is null")
                //here we should log user out or try refreshing token
            }
        })


        //could not save streak to db
        feedsviewprofileViewModel.errorSavingFeeds.observe(viewLifecycleOwner, Observer {
            Log.e(TAG, "problem saving feeds to db: $it")



        })

        /**
         * Post deal response
         */

        //posted deal successfully
        feedsviewprofileViewModel.postSuccess.observe(viewLifecycleOwner, Observer {

            Log.i(TAG, "Request Sent with success: $it")

            if (it.contains("destroyed",true)){
                mProgressBarAF?.visibility = View.GONE
                mAddfriend?.text = "Add Friend"

                mAddfriend?.setOnClickListener(null)

                mAddfriend?.setOnClickListener {

                    var sendId: FriendSentRequestModel = FriendSentRequestModel(friend_request = null)
                    sendId.friend_request = FriendId(
                            friend_id = UserProfile!!.id
                    )

                    feedsviewprofileViewModel?.sendRequest(TOKEN!!, sendId)

                    mProgressBarAF?.visibility = View.VISIBLE
                    mAddfriend?.text = ""

                }

                mConfirmFriend?.setOnClickListener {

                    var sendId: FriendSentRequestModel = FriendSentRequestModel(friend_request = null)
                    sendId.friend_request = FriendId(
                            friend_id = UserProfile!!.id
                    )

                    feedsviewprofileViewModel?.sendRequest(TOKEN!!, sendId)

                    mAddfriend?.visibility = View.VISIBLE
                    mProgressBarAF?.visibility = View.VISIBLE
                    mAddfriend?.text = ""
                    mConfirmFriend?.visibility = View.INVISIBLE

                }


            }else{
                mAddfriend?.setOnClickListener(null)

                mProgressBarAF?.visibility = View.GONE
                mAddfriend?.text = "Pending"
            }




        })

        //failed posting deal
        feedsviewprofileViewModel.postFail.observe(viewLifecycleOwner, Observer {
            Log.e(TAG, "problem posting deal: $it")
            //display alert dialog

        })

    }


    private fun displayData(feeds: List<PostsModel>) {
        try {
            if (feeds.isEmpty()) {
                //display the empty screen
                setupRecyclerview(feeds)


                VPrecycle?.visibility = View.INVISIBLE
                feedsNoDataView?.visibility = View.VISIBLE
                Log.e(TAG, "Feeds were empty")

            } else if (!feeds.isNullOrEmpty()) {
                Log.e(TAG, "feeds were neither null nor empty")

                VPrecycle?.visibility = View.VISIBLE
                feedsNoDataView?.visibility = View.INVISIBLE
                setupRecyclerview(feeds)
            }
        } catch (e: NullPointerException) {
            Log.e(TAG, "feeds were null $e")
            VPrecycle?.visibility = View.INVISIBLE
            feedsNoDataView?.visibility = View.VISIBLE
            //display null
        }
    }

    private fun makeFeedsUiList(feeds: List<PostsModel>): ArrayList<FeedsUi> {
        var feedsUi: FeedsUi


        val FeedsUiList: ArrayList<FeedsUi> = ArrayList()

        for (i in 0 until feeds.size) {
            val d = feeds[i]

            feedsUi = FeedsUi(

                    id = d.id,
                    body = d.body,
                    user = d.user,
                    event = d.event,
                    image = d.image,
                    created_at = d.created_at,
                    comment_count = d.comment_count,
                    like_count = d.like_count,
                    liked = d.liked


            )
            FeedsUiList.add(feedsUi)

        }

        return FeedsUiList
    }


    private fun setupRecyclerview(response: List<PostsModel>) {

        val test = makeFeedsUiList(response)

        VPrecycle.apply {
            //set a linear layout manager
            layoutManager = LinearLayoutManager(context)
            adapter = FeedsAdapter(test, this@ViewProfileFragment)
        }


    }


    @SuppressLint("WrongConstant")
    override fun likePost(iD: Int) {
        VPrecycle?.adapter!!.notifyDataSetChanged()
        feedsviewprofileViewModel?.postLike(TOKEN!!, iD.toString())


    }

    @SuppressLint("WrongConstant")
    override fun dislikePost(iD: Int) {
        VPrecycle?.adapter!!.notifyDataSetChanged()
        feedsviewprofileViewModel?.postDislike(TOKEN!!, iD.toString())

    }



    override fun reportPopUp(iD: Int, userid: String, postId: String) {
        var messageBoxView = LayoutInflater.from(activity).inflate(R.layout.report_pop_up_alert, null)

        //AlertDialogBuilder
        var messageBoxBuilder = AlertDialog.Builder(requireContext()).setView(messageBoxView)



        messageBoxView.buttonCommentReportUser.visibility = View.GONE
        messageBoxView.buttonCommentReportUserLine.visibility = View.GONE
        messageBoxView.buttonCommentDeleteUser.visibility = View.GONE
        messageBoxView.buttonCommentDeleteUserLine.visibility = View.GONE

        messageBoxView.buttonReportUser.visibility = View.GONE
        messageBoxView.buttonReportUserLine.visibility = View.GONE
        messageBoxView.buttonBlockUser.visibility = View.GONE
        messageBoxView.buttonBlockUserLine.visibility = View.GONE
        val  messageBoxInstance = messageBoxBuilder.show()


        if (userid == mUser?.id.toString()){


            messageBoxView.buttonReportPost.visibility = View.GONE
            messageBoxView.buttonReportPostLine.visibility = View.GONE


            messageBoxView.buttonPostDeleteUser.setOnClickListener {

                feedsviewprofileViewModel?.deletePost(TOKEN!!,iD.toString() , typeOfFeeds , mUser?.id.toString()!!)
                messageBoxInstance.dismiss()


            }
        }else{

            messageBoxView.buttonPostDeleteUser.visibility = View.GONE
            messageBoxView.buttonPostDeleteUserLine.visibility = View.GONE

        }

        messageBoxView.buttonReportPost.setOnClickListener {

            feedsviewprofileViewModel?.reportPost(TOKEN!!, postId)

            messageBoxInstance.dismiss()



        }





        val lp: WindowManager.LayoutParams = messageBoxInstance.getWindow()!!.getAttributes()
        lp.width = 600
        lp.height = 900

        messageBoxInstance.getWindow()!!.setAttributes(lp)



        //set Listener
        messageBoxView.setOnClickListener(){
            //close dialog
            messageBoxInstance.dismiss()
        }



    }


    override fun commentfrag(iDPost: Int) {

        val bundle = Bundle()
        bundle.putString("token", TOKEN.toString())
        bundle.putString("iDPost", iDPost.toString())


        // set Fragmentclass Arguments
        val fragobj = CommentsFragment()
        fragobj.arguments = bundle



        requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.feedsDrawerLayout, fragobj, "2").addToBackStack("2").commitAllowingStateLoss()

    }

    override fun visitprofile(userID: User) {
    }


}

