package com.jedmahonisgroup.gamepoint.ui.feeds

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
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
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.RecyclerviewClickListener
import com.jedmahonisgroup.gamepoint.adapters.feeds.FeedsAdapter
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.FeedsUi
import com.jedmahonisgroup.gamepoint.model.SponsorModel
import com.jedmahonisgroup.gamepoint.model.UserResponse
import com.jedmahonisgroup.gamepoint.model.UserResponseModel
import com.jedmahonisgroup.gamepoint.model.feeds.PostsModel
import com.jedmahonisgroup.gamepoint.model.feeds.User
import com.jedmahonisgroup.gamepoint.ui.BaseFragment
import com.jedmahonisgroup.gamepoint.ui.auth.AuthActivity
import com.jedmahonisgroup.gamepoint.ui.comments.CommentsFragment
import com.jedmahonisgroup.gamepoint.ui.createpost.FeedsCreatPostFragment
import com.jedmahonisgroup.gamepoint.ui.managfriends.ManageFriendsFragment
import com.jedmahonisgroup.gamepoint.ui.notification.NotificationFragment
import com.jedmahonisgroup.gamepoint.ui.settings.EditProfile
import com.jedmahonisgroup.gamepoint.ui.settings.SettingsViewModel
import com.jedmahonisgroup.gamepoint.ui.viewprofile.ViewProfileFragment
import com.jedmahonisgroup.gamepoint.utils.Constants
import com.jedmahonisgroup.gamepoint.utils.Constants.PREFS_FILENAME
import com.jedmahonisgroup.gamepoint.utils.SharedPreferencesGamePointSharedPrefsRepo
import com.jedmahonisgroup.gamepoint.utils.gamePointSharedPrefsRepo
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.event_expired_error.view.*
import kotlinx.android.synthetic.main.fragment_feeds.*
import kotlinx.android.synthetic.main.fragment_redeem.*
import kotlinx.android.synthetic.main.report_pop_up_alert.view.*
import java.util.*
import kotlin.collections.ArrayList

class FeedsFragment : BaseFragment(), AdapterView.OnItemSelectedListener, RecyclerviewClickListener , FFListner  {

    private lateinit var settingsViewModel: SettingsViewModel
    private var mFeedsNavUseravatar: ImageView? = null
    private var TAG: String = FeedsFragment::class.java.simpleName
    private var listner: FragmentChangeListener? = null

    private var binding: ViewDataBinding? = null
    private var TOKEN: String? = null
    private var mUser: UserResponse? = null
    private var user:String? = null

    private val socialViewModel: SocialViewModel by viewModels { ViewModelFactory(this.activity as AppCompatActivity) }
    //misc
    private var token: String? = null
    private var repository: gamePointSharedPrefsRepo? = null
    private val disposables = CompositeDisposable()


    private var mSwipeToRefresh: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? = null

    private var typeOfFeeds: String = ""
    private var mFeedsGlobalIV: ImageButton? = null
    private var mFeedsaccountGroupIV: ImageButton? = null
    private var mFeedsPrivateIV: ImageButton? = null
    private var mFeedsDrawerLayout: DrawerLayout? = null
    private var mFeedsNavLayout: NavigationView? = null
    private var mMenuButton: ImageButton? = null
    private var mFeedsNavUserNickNAme: TextView? = null
    private var mFeedsNavUserUserName: TextView? = null


    private var sponsorList: ArrayList<SponsorModel>? = null


    private var mHeadView: View? = null


    @SuppressLint("WrongConstant")
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_feeds, container, false)
        val rootView = binding?.root
        super.onCreateView(inflater, container, savedInstanceState)
        getToken()

        repository = SharedPreferencesGamePointSharedPrefsRepo.create(requireActivity().baseContext)
        mSwipeToRefresh = rootView?.findViewById(R.id.feeds_swipe_refresh_layout)

        requireFragmentManager().removeOnBackStackChangedListener {
            Toast.makeText(context,"hello",3).show()
        }







        setUpUi(rootView)
        typeOfFeeds = "G"

        setOnClickListeners()
        responseFromServer()


        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.e("JMG", "this.activity: " + this.activity)

    }

    private fun changeFragments(fragment: androidx.fragment.app.Fragment) {
        Log.e(TAG + "changeFragments", TOKEN.toString())

        val bundle = Bundle()
        bundle.putString("token", TOKEN.toString())

        // set Fragmentclass Arguments
        val fragobj = fragment
        fragobj.arguments = bundle



        requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.feedsDrawerLayout, fragobj, "1").addToBackStack("1").commitAllowingStateLoss()

        mFeedsDrawerLayout?.closeDrawers()

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
//        for (fragment in requireFragmentManager().getFragments()) {
//            requireFragmentManager().beginTransaction().remove(fragment).commit()
//        }
    }

    fun setOnClickListeners () {
        setAccountGroupBackground()
        setPrivateBackground()
        mFeedsGlobalIV?.setOnClickListener {
            mSwipeToRefresh?.isRefreshing = true

            typeOfFeeds = "G"
            socialViewModel?.changefeedstype(TOKEN!! , typeOfFeeds , mUser?.id.toString()!!)
            mFeedsGlobalIV?.setImageResource(R.drawable.ic_globe)
            mFeedsGlobalIV?.setBackgroundResource(R.drawable.bg_pick_unselected)
            setAccountGroupBackground()
            setPrivateBackground()
        }

        mFeedsaccountGroupIV?.setOnClickListener {
            mSwipeToRefresh?.isRefreshing = true

            typeOfFeeds = "F"
            socialViewModel?.changefeedstype(TOKEN!! , typeOfFeeds , mUser?.id.toString()!!)
            mFeedsaccountGroupIV?.setBackgroundResource(R.drawable.bg_pick_unselected)
            mFeedsaccountGroupIV?.setImageResource(R.drawable.ic_account_group)
            setGlobeBackground()
            setPrivateBackground()
        }

        mFeedsPrivateIV?.setOnClickListener {
            mSwipeToRefresh?.isRefreshing = true

            typeOfFeeds = "P"
            socialViewModel?.changefeedstype(TOKEN!! , typeOfFeeds , mUser?.id.toString()!!)
            mFeedsPrivateIV?.setImageResource(R.drawable.ic_account)
            mFeedsPrivateIV?.setBackgroundResource(R.drawable.bg_pick_unselected)
            setGlobeBackground()
            setAccountGroupBackground()
        }


        mSwipeToRefresh?.setOnRefreshListener {
            socialViewModel?.changefeedstype(TOKEN!! , typeOfFeeds , mUser?.id.toString()!!)
        }

        mMenuButton?.setOnClickListener {
            Log.e("JMG", "that hamburger shit was touched")
            mFeedsDrawerLayout?.openDrawer(GravityCompat.START)
        }
    }
    private fun setGlobeBackground(){
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        getSchoolColor()?.let { shape.setColor(it) }
        getSchoolColor()?.let { shape.setStroke(2, it) }
        mFeedsGlobalIV!!.background = shape
        mFeedsGlobalIV?.setImageResource(R.drawable.ic_globe_white)

    }
    private fun setAccountGroupBackground(){
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        getSchoolColor()?.let { shape.setColor(it) }
        getSchoolColor()?.let { shape.setStroke(2, it) }
        mFeedsaccountGroupIV!!.background = shape
        mFeedsaccountGroupIV?.setImageResource(R.drawable.ic_account_tri_white)

    }
    private fun setPrivateBackground(){
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        getSchoolColor()?.let { shape.setColor(it) }
        getSchoolColor()?.let { shape.setStroke(2, it) }
        mFeedsPrivateIV!!.background = shape
        mFeedsPrivateIV?.setImageResource(R.drawable.ic_account_white)

    }

     override fun setUpUi(rootView: View?) {

         mMenuButton = rootView?.findViewById(R.id.feedsMenuSpinner)
         mFeedsDrawerLayout = rootView?.findViewById(R.id.feedsDrawerLayout)
         mFeedsNavLayout = rootView?.findViewById(R.id.nav_view)

        mFeedsGlobalIV = rootView?.findViewById(R.id.globe)
        mFeedsaccountGroupIV = rootView?.findViewById(R.id.accountGroup)
        mFeedsPrivateIV = rootView?.findViewById(R.id.privateFeeds)

        mFeedsGlobalIV?.setImageResource(R.drawable.ic_globe);
        mFeedsGlobalIV?.setBackgroundResource(R.drawable.bg_pick_unselected)

        mFeedsNavLayout?.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.feedsNavItemCreatePost -> {
                    changeFragments(FeedsCreatPostFragment())
                    true
                }
                R.id.feedsNavItemManageFriends -> {
                    changeFragments(ManageFriendsFragment())
                    true
                }
                R.id.feedsNavItemNotification -> {
                    changeFragments(NotificationFragment())
                    true
                }
                R.id.feedsNavItemAccountEdit -> {
                    mFeedsDrawerLayout?.closeDrawers()

                    val intent = Intent(context, EditProfile::class.java)
                    startActivity(intent)

                    true
                }
                R.id.nav_footer_1 -> {
                    closeAllActivities()
                    true
                }

                else -> ConsoleLog("msg")
            }
        }
         mHeadView = mFeedsNavLayout?.getHeaderView(0)
         mFeedsNavUserNickNAme = mHeadView?.findViewById(R.id.feedsNavHeaderUserNickName)
         mFeedsNavUserUserName = mHeadView?.findViewById(R.id.feedsNavHeaderUserName)
         mFeedsNavUseravatar = mHeadView?.findViewById(R.id.feedsNavHeaderUserAvatar)

         mFeedsNavUserNickNAme?.text = mUser?.first_name + " " + mUser?.last_name
         mFeedsNavUserUserName?.text = "@"+mUser?.nickname
         Log.e("JMG", "mUser!!.user.avatar: " + mFeedsNavUseravatar)
         Glide.with(this).load(mUser!!.avatar).circleCrop().error(R.drawable.ic_user_account).into(mFeedsNavUseravatar!!)
    }

    private fun ConsoleLog(s: String): Boolean {
        Log.e(TAG, s)
        return true

    }






    private fun responseFromServer() {

        // lets observe server data response
        mSwipeToRefresh?.isRefreshing = true

        socialViewModel.getSponsorFromServer(TOKEN!!)

        socialViewModel.SponsorsFromServer.observe(viewLifecycleOwner, Observer { feeds ->
            Log.e("JMG", "returned!")
            socialViewModel.getMyFeedsFromServer(token!!)
            if (feeds != null) {

                sponsorList =  feeds
                infoSponsor?.visibility = View.VISIBLE
                Glide.with(requireContext()).load(sponsorList!![0].image).into(sponsor_image)

            }else{
                infoSponsor?.visibility = View.GONE

            }


        })
        Log.e("JMG", "should observe feedsFromServer: " + socialViewModel)
        socialViewModel.feedsFromServer.observe(viewLifecycleOwner, {
            Log.e("JMG", "success feeds2: " + it.size)
            if (it != null) {
                mSwipeToRefresh!!.isRefreshing = false
                Log.e(TAG, "here are the picks data is: $it")
                displayData(it)


            } else {
                //picks are null
                myFeedsRv?.visibility = View.INVISIBLE
                feedsNoDataView?.visibility = View.VISIBLE

            }
        })


        socialViewModel.serverDataFailed.observe(viewLifecycleOwner, Observer { response ->
            Log.e(TAG, "problem fetching feeds from server: $response")
            mSwipeToRefresh!!.isRefreshing = false
            myFeedsRv?.visibility = View.INVISIBLE
            feedsNoDataView?.visibility = View.VISIBLE
        })

//        first, try to get data from the database




        //could not save streak to db
        socialViewModel.errorSavingFeeds.observe(viewLifecycleOwner, Observer {
            Log.e(TAG, "problem saving feeds to db: $it")

        })

        /**
         * Post deal response
         */

        //posted deal successfully
        socialViewModel.postSuccess.observe(viewLifecycleOwner, Observer {
            Log.i(TAG, "post resultsFromServer: $it")

        })

        //failed posting deal
        socialViewModel.postFail.observe(viewLifecycleOwner, Observer {
            Log.e(TAG, "problem posting deal: $it")
            //display alert dialog
//            feedClosedAlert("" + it)

        })

    }


    // lets observe server data response


    private fun feedClosedAlert(it: String) {
        Log.i(TAG, "Feed was closed")
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.pick_closed_alert, null)
        //AlertDialogBuilder
        val errorTextView = mDialogView.findViewById<TextView>(R.id.redeemNowInfo)
        errorTextView.text = it
        val mBuilder = this.let { AlertDialog.Builder(requireContext()).setView(mDialogView) }
        //show dialog
        val mAlertDialog = mBuilder?.show()
        mAlertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        //login button cick of custom layout
        mDialogView.close.setOnClickListener {
            mAlertDialog?.dismiss()


        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //TODO("Not yet implemented")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent!!.getItemAtPosition(position).toString() == "Results") {
            try {
                listner?.onResultsClicked()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onTeam1Clicked(team1: String, id: Int) {
        //TODO("Not yet implemented")
    }

    override fun onTeam2Clicked(team2: String, id: Int) {
        //TODO("Not yet implemented")
    }

    override fun onResume() {
        super.onResume()
        getToken()
        socialViewModel.getMyFeedsFromServer(token!!)
        setToolbarColors(toolbar_feeds)
    }

    private fun getToken() {
        token = requireArguments().getString("token")
        Log.e(TAG + "Token", token.toString())

        TOKEN = token

        mUser = GamePointApplication.shared!!.getCurrentUser(requireContext())


    }

    private fun displayData(feeds: List<PostsModel>) {
        try {
            if (feeds.isEmpty()) {
                //display the empty screen
                setupRecyclerview(feeds)


                myFeedsRv?.visibility = View.INVISIBLE
                feedsNoDataView?.visibility = View.VISIBLE
                Log.e(TAG, "Feeds were empty")

            } else if (!feeds.isNullOrEmpty()) {
                Log.e(TAG, "feeds were neither null nor empty")

                myFeedsRv?.visibility = View.VISIBLE
                feedsNoDataView?.visibility = View.INVISIBLE
                setupRecyclerview(feeds)
            }
        } catch (e: NullPointerException) {
            Log.e(TAG, "feeds were null $e")
            myFeedsRv?.visibility = View.INVISIBLE
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


    @SuppressLint("NewApi")
    private fun setupRecyclerview(response: List<PostsModel>) {

        val test = makeFeedsUiList(response)

        myFeedsRv.apply {
            //set a linear layout manager
            layoutManager = LinearLayoutManager(context)
            adapter = FeedsAdapter(test, this@FeedsFragment)
        }

        myFeedsRv?.setOnScrollChangeListener { view, i, i2, i3, i4 ->
           var index: Int =  myFeedsRv?.getCurrentPosition()?.div(10)!!



            try {

                Glide.with(requireContext()).load(sponsorList!![myFeedsRv?.getCurrentPosition()?.rem(sponsorList!!.size)!!].image).into(sponsor_image)
            }catch (e: java.lang.Exception){

            }





        }

    }

    private fun closeAllActivities() {
        val intent = Intent(context, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        intent.putExtra("EXIT", true)
        startActivity(intent)
    }

    fun unregisterPushToken() {
        val token = requireActivity().intent.getStringExtra("token")!!
        settingsViewModel.unregisterPushTokenSuccess.observe(this, Observer {
            sharedPreferences.edit().putString("pushTokenString", "").apply()
            sharedPreferences.edit().putBoolean("pushTokenSent", false).apply()
            val editor = sharedPreferences.edit()
            editor.putBoolean(Constants.IsLogedIn, false)
            editor.apply()

            //make the api call
            settingsViewModel!!.logout(token)
            closeAllActivities()
        })
        settingsViewModel!!.unregisterPushTokenFail.observe(this, Observer {
            Log.e(TAG, "something went wrong while trying to unregister push token $it we will log user out anyways")
            val editor = sharedPreferences.edit()
            editor.putBoolean(Constants.IsLogedIn, false)
            editor.apply()

            //make the api call
            settingsViewModel.logout(token)
            closeAllActivities()
        })

        Log.e("JMG", "sharedPreferences.getString(\"pushTokenString\", \"\")!!: " + sharedPreferences.getString("pushTokenString", "")!!)
        settingsViewModel.unregisterPushToken(token, sharedPreferences.getString("pushTokenString", "")!!)
    }


    interface FragmentChangeListener {
        fun onResultsClicked()
    }




    @SuppressLint("WrongConstant")
    override fun likePost(iD: Int) {
       myFeedsRv?.adapter!!.notifyDataSetChanged()
        socialViewModel?.postLike(TOKEN!!, iD.toString())



    }
    @SuppressLint("WrongConstant")
    override fun dislikePost(iD: Int) {
        myFeedsRv?.adapter!!.notifyDataSetChanged()
        socialViewModel?.postDislike(TOKEN!!, iD.toString())

    }

    override fun reportPopUp(iD: Int , userid : String , postID:String) {
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

                socialViewModel?.deletePost(TOKEN!!,iD.toString() , typeOfFeeds , mUser?.id.toString()!!)
                messageBoxInstance.dismiss()


            }
        }else{

            messageBoxView.buttonPostDeleteUser.visibility = View.GONE
            messageBoxView.buttonPostDeleteUserLine.visibility = View.GONE

        }

        messageBoxView.buttonReportPost.setOnClickListener {

            socialViewModel?.reportPost(TOKEN!!, postID)

            messageBoxInstance.dismiss()



        }





        val lp: WindowManager.LayoutParams = messageBoxInstance.window!!.attributes
        lp.width = 600
        lp.height = 900

        messageBoxInstance.window!!.attributes = lp



        //set Listener
        messageBoxView.setOnClickListener(){
            //close dialog
            messageBoxInstance.dismiss()
        }



    }



    override fun commentfrag(iDPost: Int ) {

        val bundle = Bundle()
        bundle.putString("token", TOKEN.toString())
        bundle.putString("iDPost", iDPost.toString())


        // set Fragmentclass Arguments
        val fragobj = CommentsFragment()
        fragobj.arguments = bundle



        requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.feedsDrawerLayout, fragobj, "1").addToBackStack("1").commitAllowingStateLoss()

    }

    override fun visitprofile(userID : User) {

        val gson = Gson()

        Log.e(TAG + "changeFragments", TOKEN.toString())

        val bundle = Bundle()
        bundle.putString("token", TOKEN.toString())
        bundle.putString("iD", gson.toJson(userID))


        // set Fragmentclass Arguments
        val fragobj = ViewProfileFragment()
        fragobj.arguments = bundle



        requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.feedsDrawerLayout, fragobj, "1").addToBackStack("1").commitAllowingStateLoss()


    }

    private fun RecyclerView.getCurrentPosition() : Int {
        return (this.layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
    }


}



interface FFListner {
    fun likePost(iD: Int)
    fun dislikePost(iD: Int)
    fun reportPopUp(iD: Int , userid: String , postId: String)
    fun commentfrag(iDPost: Int)
    fun visitprofile (userID : User)
    fun setUpUi(rootView: View?)

}