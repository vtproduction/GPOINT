package com.jedmahonisgroup.gamepoint.ui.notification

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Build

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
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
import com.jedmahonisgroup.gamepoint.adapters.notification.notificationAdapter
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.*

import com.jedmahonisgroup.gamepoint.model.feeds.PostsModel
import com.jedmahonisgroup.gamepoint.model.feeds.User
import com.jedmahonisgroup.gamepoint.ui.comments.CommentsFragment
import com.jedmahonisgroup.gamepoint.ui.feeds.FFListner
import com.jedmahonisgroup.gamepoint.ui.feeds.FeedsViewModel
import com.jedmahonisgroup.gamepoint.ui.managfriends.ManageFriendsViewModel
import kotlinx.android.synthetic.main.fragment_feeds.*
import kotlinx.android.synthetic.main.fragment_feeds_view_profile.*
import kotlinx.android.synthetic.main.report_pop_up_alert.view.*
import java.util.ArrayList
import kotlinx.android.synthetic.main.fragment_feeds_view_profile.feedsNoDataView as feedsNoDataView1


class NotificationFragment : Fragment() {

    private var token: String? = null


    private var TOKEN: String? = null
    private var mUser: UserResponse? = null


    private var mBack: Button? = null

    private lateinit var notificationviewmodel: NotificationViewModel
    private var mNoNotifiationsLayout: RelativeLayout? = null





    private var binding: ViewDataBinding? = null


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        notificationviewmodel = ViewModelProviders.of(this).get(NotificationViewModel::class.java)


        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notification, container, false)
        val rootView = binding?.root

        getToken()

        setUpUi(rootView)

        responseFromServer()

        return rootView

    }


    fun setUpUi(rootView: View?) {

        notificationviewmodel.getNotificationFromServer(TOKEN!!)
        mBack = rootView?.findViewById(R.id.view_profile_back)

        mNoNotifiationsLayout = rootView?.findViewById(R.id.feedsNoDataView)

        mBack?.setOnClickListener {
            fragmentManager?.popBackStack()
        }

    }


    private fun getToken() {
        val gson = Gson()
        token = requireArguments().getString("token")



        TOKEN = token


        mUser = GamePointApplication.shared!!.getCurrentUser(requireContext())



    }


    private fun responseFromServer() {



        notificationviewmodel.notificationFromServer.observe(viewLifecycleOwner, Observer { feeds ->

            if (feeds != null) {

                displayData(feeds)


            } else {

                mNoNotifiationsLayout?.visibility = View.VISIBLE


            }


        })

        notificationviewmodel.serverDataFailed.observe(viewLifecycleOwner, Observer { response ->
            Log.e(TAG, "problem fetching picks from server: $response")
            //mSwipeToRefresh!!.isRefreshing = false
//            myFeedsRv?.visibility = View.INVISIBLE
//            feedsNoDataView?.visibility = View.VISIBLE
        })



    }


    private fun displayData(feeds: List<notificationResponseModel>) {
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

    private fun makeFeedsUiList(feeds: List<notificationResponseModel>): ArrayList<notificationResponseModel> {
        var feedsUi: notificationResponseModel


        val FeedsUiList: ArrayList<notificationResponseModel> = ArrayList()

        for (i in 0 until feeds.size) {
            val d = feeds[i]

            feedsUi = notificationResponseModel(

                     id = d.id,
             title= d.title,
             body = d.body,
             sent = d.sent,
             sent_at = d.sent_at,
             sent_success = d.sent_success,
             push_type = d.push_type,
             url = d.url



            )
            FeedsUiList.add(feedsUi)

        }

        return FeedsUiList
    }


    private fun setupRecyclerview(response: List<notificationResponseModel>) {

        val test = makeFeedsUiList(response)

        VPrecycle.apply {
            //set a linear layout manager
            layoutManager = LinearLayoutManager(context)
            adapter = notificationAdapter(test)
        }


    }




}

