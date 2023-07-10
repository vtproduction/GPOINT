package com.jedmahonisgroup.gamepoint.ui.managfriends

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.adapters.friends.FriendsAdapter
import com.jedmahonisgroup.gamepoint.adapters.friends.FriendsRequesAdapter
import com.jedmahonisgroup.gamepoint.model.*
import com.jedmahonisgroup.gamepoint.model.feeds.User
import com.jedmahonisgroup.gamepoint.ui.viewprofile.ViewProfileFragment
import kotlinx.android.synthetic.main.fragment_manage_friends.*


class ManageFriendsFragment : Fragment(), MFlistner {
    // TODO: Rename and change types of parameters


    private var searchInput: SearchView? = null
    private var searchrequestListUI: ArrayList<User>? = null
    private var TOKEN: String? = null
    private var mUser: UserResponse? = null
    private var mback: Button? = null
    private var user:String? = null



    private var binding: ViewDataBinding? = null
    private lateinit var manageFriendsViewModel: ManageFriendsViewModel

     lateinit var friendrequestListUI : ArrayList<RequestModelUi>
    lateinit var friendListUI : ArrayList<User>




    private var viewVisibilityRFT : Int? = null
    private var viewVisibilityRFRV : Int? = null

    private var viewVisibilityFT : Int? = null
    private var viewVisibilityFRV : Int? = null
    private var viewVisibilityFNoData : Int? = null


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_manage_friends, container, false)
        val rootView = binding?.root

        manageFriendsViewModel = ViewModelProviders.of(this).get(ManageFriendsViewModel::class.java)


        getToken()
        responseFromServer()

        setUpUi(rootView)


        // Inflate the layout for this fragment
        return rootView



    }

    private fun setUpUi(rootView: View?) {



        mback = rootView?.findViewById(R.id.feedsCreatPostBackBtn)

        mback?.setOnClickListener {

            fragmentManager?.popBackStack("1",1)

        }

        searchInput = rootView?.findViewById(R.id.search)



        val searchCloseButtonId: Int? = searchInput?.getContext()?.getResources()?.getIdentifier("android:id/search_close_btn", null, null)
        val closeButton: ImageView? = rootView?.findViewById(searchCloseButtonId!!)

     closeButton?.setOnClickListener {
        hideKeyboard()
         searchInput?.setQuery("",false)
         searchInput?.clearFocus()

         visibilityMF(View.GONE,View.GONE,View.GONE,viewVisibilityRFT!!,viewVisibilityRFRV!!,viewVisibilityFT!!,viewVisibilityFRV!!,viewVisibilityFNoData!!)


     }

        searchInput?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                val word = newText?.split(" ")!!.toTypedArray()

                if (newText == " " || newText.isEmpty()){

                }else{
                    manageFriendsViewModel?.getsearchresult(TOKEN!! , word)
                }

                visibilityMF(View.VISIBLE,View.VISIBLE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE)


                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {

                val word = query?.split(" ")!!.toTypedArray()

                manageFriendsViewModel?.getsearchresult(TOKEN!! , word)

                visibilityMF(View.VISIBLE,View.VISIBLE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE)




                // Do your task here
                return false
            }

        })







        manageFriendsViewModel.getFriends(TOKEN!!)

    }

    private fun visibilityMF(Searchtitle : Int , SearchRV : Int , SearchNodata : Int , RequestFriendtitle : Int , RequestFriendRV: Int , Friendtitle: Int , FriendRV: Int , FriendNoData: Int) {
        textviewSearch?.visibility = Searchtitle
        reseultsearchRV?.visibility = SearchRV
        searchNoDataView?.visibility = SearchNodata
        textView3?.visibility = RequestFriendtitle
        friendsRequestRv?.visibility = RequestFriendRV
        textView4?.visibility = Friendtitle
        myFriendsRv?.visibility = FriendRV
        friendsNoDataView?.visibility = FriendNoData


    }


    private fun getToken() {
        var token = requireArguments().getString("token")

        TOKEN = token

        mUser = GamePointApplication.shared!!.getCurrentUser(requireContext())
    }

    private fun responseFromServer() {

        // lets observe server data response


        manageFriendsViewModel.friendsFromServer.observe(viewLifecycleOwner, Observer { feeds ->

            if (feeds != null) {
                Log.i("TAG", "here are the friends data is: $feeds")
                displayData(feeds)


            } else {
                //picks are null


            }


        })

        manageFriendsViewModel.searchfriendsFromServer.observe(viewLifecycleOwner, Observer { feeds ->

            if (feeds != null) {
                Log.i("TAG", "here are the friends data is: $feeds")
                displaysearchData(feeds)


            } else {

                visibilityMF(View.VISIBLE,View.VISIBLE,View.VISIBLE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE)

            }


        })

        manageFriendsViewModel.serverDataFailed.observe(viewLifecycleOwner, Observer { response ->
            Log.e("TAG", "problem fetching friends from server: $response")
            //mSwipeToRefresh!!.isRefreshing = false

        })

//        first, try to get data from the database



    }


    private fun displaysearchData(friends: List<User>) {
        try {
            if (friends.isEmpty() ) {
                //display the empty screen
                setupRecyclersearchview(friends)

                visibilityMF(View.VISIBLE,View.VISIBLE,View.VISIBLE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE)




            } else if (!friends.isNullOrEmpty()) {

                visibilityMF(View.VISIBLE,View.VISIBLE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE)
                setupRecyclersearchview(friends)


            }



        } catch (e: NullPointerException) {
            Log.e("TAG", "feeds were null $e")

            //display null
        }
    }

    private fun displayData(friends: FriendsModel) {
        try {
            if (friends.requests.isEmpty() ) {
                //display the empty screen

                viewVisibilityRFT = View.GONE
                viewVisibilityRFRV = View.GONE



                Log.e("TAG", "requests were empty")

            } else if (!friends.requests.isNullOrEmpty()) {
                Log.e("TAG", "feeds were neither null nor empty")

                setupRecyclerview(friends.requests)

                viewVisibilityRFT = View.VISIBLE
                viewVisibilityRFRV = View.VISIBLE


            }

            if (friends.friends.isEmpty()){

                viewVisibilityFT = View.VISIBLE
                viewVisibilityFRV = View.VISIBLE
                viewVisibilityFNoData = View.VISIBLE

                Log.e("TAG", "friends were empty")

            }else if (!friends.friends.isNullOrEmpty()){
                viewVisibilityFT = View.VISIBLE
                viewVisibilityFRV = View.VISIBLE
                viewVisibilityFNoData = View.GONE
                setupRecyclerviewfriend(friends.friends)
            }

            visibilityMF(View.GONE,View.GONE,View.GONE,viewVisibilityRFT!!,viewVisibilityRFRV!!,viewVisibilityFT!!,viewVisibilityFRV!!,viewVisibilityFNoData!!)

        } catch (e: NullPointerException) {
            Log.e("TAG", "feeds were null $e")

            //display null
        }
    }


    private fun setupRecyclersearchview(response: List<User>) {

        friendListUI = makeFeedsUiListFriends(response)

        reseultsearchRV.apply {
            //set a linear layout manager
            layoutManager = LinearLayoutManager(context)
            adapter = FriendsAdapter(friendListUI, this@ManageFriendsFragment)
        }


    }

    private fun setupRecyclerview(response: List<RequestModel>) {

        friendrequestListUI = makeFeedsUiList(response)

        friendsRequestRv.apply {
            //set a linear layout manager
            layoutManager = LinearLayoutManager(context)
            adapter = FriendsRequesAdapter(friendrequestListUI, this@ManageFriendsFragment)
        }


    }

    private fun setupRecyclerviewfriend(response: List<User>) {

        friendListUI = makeFeedsUiListFriends(response)

        myFriendsRv.apply {
            //set a linear layout manager
            layoutManager = LinearLayoutManager(context)
            adapter = FriendsAdapter(friendListUI, this@ManageFriendsFragment)
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

    private fun makesearchUiList(RequestResponse: List<User>): ArrayList<RequestModelUi> {
        var requestUi: RequestModelUi


        val requestModelUiList: ArrayList<RequestModelUi> = ArrayList()

        val active: ArrayList<DealModel> = ArrayList()
        for (i in 0 until RequestResponse.size) {
            val d = RequestResponse[i]

            requestUi = RequestModelUi(

                    friend = d


            )
            requestModelUiList.add(requestUi)

        }

        return requestModelUiList
    }

    private fun makeFeedsUiListFriends(RequestResponse: List<User>): ArrayList<User> {
        var requestUi: User


        val requestModelUiList: ArrayList<User> = ArrayList()

        val active: ArrayList<DealModel> = ArrayList()
        for (i in 0 until RequestResponse.size) {
            val d = RequestResponse[i]

            requestUi = User(
                     id = d.id,
             first_name = d.first_name,
             last_name = d.last_name,
             email = d.email,
             nickname = d.nickname,
             sign_up_code = d.sign_up_code,
             sign_with_code = d.sign_with_code,
             private_posts = d.private_posts,
             friend = d.friend,
             friend_count = d.friend_count,
             avatar = d.avatar,
             friend_request = d.friend_request

            )
            requestModelUiList.add(requestUi)

        }

        return requestModelUiList
    }

    @SuppressLint("WrongConstant")
    override fun Request(request: String,pos: Int , poslist: Int){

        val iDreq = FriendADRequestModel(friend_request = Approvalmodel(
                approval = request
        ))

        manageFriendsViewModel?.Request(TOKEN!!,pos.toString(),iDreq)

        friendrequestListUI.removeAt(poslist)

        friendsRequestRv.adapter?.notifyItemRemoved(poslist)

    }


    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    @SuppressLint("LongLogTag")
    override fun visitprofile(userID : User) {

        val gson = Gson()

        Log.e(TAG + "changeFragments", TOKEN.toString())

        val bundle = Bundle()
        bundle.putString("token", TOKEN.toString())
        bundle.putString("iD", gson.toJson(userID))


        // set Fragmentclass Arguments
        val fragobj = ViewProfileFragment()
        fragobj.arguments = bundle



        hideKeyboard()

        searchInput?.clearFocus()
        requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.feedsFriendsLayout, fragobj, "3").addToBackStack("3").commitAllowingStateLoss()


    }

    @SuppressLint("LongLogTag")
    override fun visitprofilewithapproval(userID : User, approval: String) {

        val gson = Gson()

        Log.e(TAG + "changeFragments", TOKEN.toString())

        val bundle = Bundle()
        bundle.putString("token", TOKEN.toString())
        bundle.putString("iD", gson.toJson(userID))
        bundle.putString("approval", approval)


        // set Fragmentclass Arguments
        val fragobj = ViewProfileFragment()
        fragobj.arguments = bundle



        hideKeyboard()

        searchInput?.clearFocus()
        requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.feedsFriendsLayout, fragobj, "3").addToBackStack("3").commitAllowingStateLoss()


    }



}


interface MFlistner {
    fun Request(request:String , pos: Int , poslist: Int)
    fun visitprofile (userID : User)
    fun visitprofilewithapproval (userID : User, approval: String)


}