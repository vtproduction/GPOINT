package com.jedmahonisgroup.gamepoint.ui.blockeduser

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.adapters.blockeduser.BlockedUserAdapter
import com.jedmahonisgroup.gamepoint.model.*
import com.jedmahonisgroup.gamepoint.model.feeds.User
import com.jedmahonisgroup.gamepoint.ui.managfriends.ManageFriendsViewModel
import kotlinx.android.synthetic.main.fragment_blockeduser.*


class blockuserfragment : Fragment()  {





    private var searchInput: SearchView? = null
    private var searchrequestListUI: ArrayList<User>? = null
    private var TOKEN: String? = null
    private var mUser: UserResponse? = null
    private var mback: Button? = null
    private var user:String? = null



    private var binding: ViewDataBinding? = null
    private lateinit var manageFriendsViewModel: ManageFriendsViewModel

    lateinit var friendrequestListUI : ArrayList<RequestModelUi>
    private var mSwipeToRefresh: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? = null




    private var viewVisibilityRFT : Int? = null
    private var viewVisibilityRFRV : Int? = null

    private var viewVisibilityFT : Int? = null
    private var viewVisibilityFRV : Int? = null
    private var viewVisibilityFNoData : Int? = null



    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_blockeduser, container, false)
        val rootView = binding?.root

        manageFriendsViewModel = ViewModelProviders.of(this).get(ManageFriendsViewModel::class.java)

        getToken()
        responseFromServer()


        setUpUi(rootView)

        return rootView



    }

    private fun setUpUi(rootView: View?) {

        mSwipeToRefresh = rootView?.findViewById(R.id.swiperefreshBU)
        mSwipeToRefresh?.isRefreshing = true


        mSwipeToRefresh?.setOnRefreshListener {
            manageFriendsViewModel.getFriends(TOKEN!!)
        }

        manageFriendsViewModel.getFriends(TOKEN!!)

    }


    private fun getToken() {
        var token = requireArguments().getString("token")!!

        Log.e(ContentValues.TAG + "Token", token)

        TOKEN = token

        val gson = Gson()
        mUser = GamePointApplication.shared!!.getCurrentUser(requireContext())



        Log.i("${ContentValues.TAG}, TOKEN: ", token)
        Log.i(ContentValues.TAG, "user  ==========> ${mUser.toString()}")
    }

    private fun responseFromServer() {

        // lets observe server data response


        manageFriendsViewModel.friendsFromServer.observe(viewLifecycleOwner, Observer { feeds ->
            mSwipeToRefresh?.isRefreshing = false

            if (feeds != null) {
                Log.i("TAG", "here are the picks data is: $feeds")
                displayData(feeds)


            } else {
                //picks are null


            }


        })


        manageFriendsViewModel.serverDataFailed.observe(viewLifecycleOwner, Observer { response ->
            Log.e("TAG", "problem fetching friends from server: $response")
            mSwipeToRefresh?.isRefreshing = false

        })

//        first, try to get data from the database



    }

    private fun displayData(friends: FriendsModel) {
        try {
            if (friends.blocked_users.isEmpty() ) {
                //display the empty screen

                viewVisibilityRFT = View.GONE
                viewVisibilityRFRV = View.GONE



                Log.e("TAG", "requests were empty")

            } else if (!friends.blocked_users.isNullOrEmpty()) {
                Log.e("TAG", "feeds were neither null nor empty")

                setupRecyclerview(friends.blocked_users)

                viewVisibilityRFT = View.VISIBLE
                viewVisibilityRFRV = View.VISIBLE


            }




        } catch (e: NullPointerException) {
            Log.e("TAG", "feeds were null $e")

            //display null
        }
    }


    private fun setupRecyclerview(response: List<RequestModel>) {

        friendrequestListUI = makeFeedsUiList(response)

        blockeduserRV.apply {
            //set a linear layout manager
            layoutManager = LinearLayoutManager(context)
            adapter = BlockedUserAdapter(friendrequestListUI)
        }


    }


    private fun makeFeedsUiList(RequestResponse: List<RequestModel>): ArrayList<RequestModelUi> {
        var requestUi: RequestModelUi


        val requestModelUiList: ArrayList<RequestModelUi> = ArrayList()

        val active: ArrayList<DealModel> = ArrayList()
        for (i in 0 until RequestResponse.size) {
            val d = RequestResponse[i]

            requestUi = RequestModelUi(
                    id = d.id,
                    user_id = d.user_id,
                    friend_id = d.friend_id,
                    approval = d.approval,
                    created_at = d.created_at,
                    updated_at = d.updated_at,
                    user = d.user,
                    friend = d.friend,
                    url = d.url


            )
            requestModelUiList.add(requestUi)

        }

        return requestModelUiList
    }




}




