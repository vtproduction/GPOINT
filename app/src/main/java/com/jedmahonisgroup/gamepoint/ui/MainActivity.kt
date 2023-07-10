package com.jedmahonisgroup.gamepoint.ui

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color.*
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.*
import com.jedmahonisgroup.gamepoint.model.events.EventsModel
import com.jedmahonisgroup.gamepoint.model.gameshow.GameShow
import com.jedmahonisgroup.gamepoint.ui.auth.AuthActivity
import com.jedmahonisgroup.gamepoint.ui.deals.DealDetailFragment
import com.jedmahonisgroup.gamepoint.ui.deals.DealFragment
import com.jedmahonisgroup.gamepoint.ui.events.EventDetailActivity
import com.jedmahonisgroup.gamepoint.ui.events.EventsFragment
import com.jedmahonisgroup.gamepoint.ui.home.HomeFragment
import com.jedmahonisgroup.gamepoint.ui.leaderboard.GameShowLeaderBoardFragment
import com.jedmahonisgroup.gamepoint.ui.leaderboard.GameShowLeaderBoardViewModel
import com.jedmahonisgroup.gamepoint.ui.leaderboard.PointsFragment
import com.jedmahonisgroup.gamepoint.ui.leaderboard.StreakFragment
import com.jedmahonisgroup.gamepoint.ui.pickEm.PickEmFragment
import com.jedmahonisgroup.gamepoint.ui.picks.PicksFragment
import com.jedmahonisgroup.gamepoint.ui.picks.ResultFragment
import com.jedmahonisgroup.gamepoint.ui.points.PointDetailActivity
import com.jedmahonisgroup.gamepoint.ui.settings.SettingsFragment
import com.jedmahonisgroup.gamepoint.utils.*
import com.jedmahonisgroup.gamepoint.utils.Constants.ELAPSED_TIME
import com.jedmahonisgroup.gamepoint.utils.Constants.PERCENT_KEY
import com.jedmahonisgroup.gamepoint.utils.Constants.POINTS_KEY
import com.jedmahonisgroup.gamepoint.utils.Constants.POINTS_THIS_CHECK_IN
import com.jedmahonisgroup.gamepoint.utils.Constants.REQUEST_CODE_UPDATE
import com.jedmahonisgroup.gamepoint.utils.Constants.TODAY_GAME_KEY
import com.jedmahonisgroup.gamepoint.utils.Constants.TOTAL_POINTS_KEY
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_header_toolbar.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*
import kotlin.concurrent.schedule


class MainActivity : BaseActivity(), PicksFragment.FragmentChangeListener,
        ResultFragment.SwitchFragmentListener, PointsFragment.LeaderBoardTabsListener,
        StreakFragment.PointsTouchedListener, DealFragment.RedeemListener,
        EventDetailActivity.RedeemPressed, EventsFragment.EventsFragmentListener, DealDetailFragment.RedeemDetailsListener {


    private var bottomNavigationView: BottomNavigationView? = null
    private val TAG = MainActivity::class.java.simpleName

    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: com.jedmahonisgroup.gamepoint.databinding.ActivityMainBinding

    private var mToken: String? = null
    private var mUser: UserResponse? = null
    private var mProfilePicUrl: String? = null

    private val PREFS_FILENAME = "com.jedmahonisgroup.gamepoint"

    private val handler = Handler()
    private var runnableCode: Runnable? = null



    private lateinit var repository: gamePointSharedPrefsRepo
    private val disposables = CompositeDisposable()

    private var mEventStartTime: String? = null
    private var mEventLength: Int? = null

    private var mLoginModel: Login? = null

    private var isLoggingOut = false

    private var afterDealRedeem = false
    
    private lateinit var appUpdateManager: AppUpdateManager
    private val updateAvailable = MutableLiveData<Boolean>().apply { value = false }
    private var isOnDealRedeemScreen = false



    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)


        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainViewModel = ViewModelProviders.of(this, ViewModelFactory(this)).get(MainViewModel::class.java)
        mainViewModel.context = baseContext

        findViewById<View>(R.id.containerPoint).setOnClickListener {
            startActivity(Intent(this, PointDetailActivity::class.java))
        }

        findViewById<View>(R.id.header_right_btn).setOnClickListener {
            toggleNavigationTab()
        }

        findViewById<View>(R.id.imgHome).setOnClickListener {
            toggleHideNavigationTab()
            changeFragments(HomeFragment.newInstance(HomeFragmentCallback(this)))
        }

        var isCheckedIn = sharedPreferences.getBoolean("CHECKED_IN", false)
        val sos = sharedPreferences.getBoolean("SOS", false)

        if (!isCheckedIn && !sos) {
            Log.e(TAG, "deleting is the problem")
            val editor = sharedPreferences.edit()
            editor.putString(PERCENT_KEY, "0")
            editor.putString(POINTS_KEY, "0")
            editor.putString(POINTS_THIS_CHECK_IN, "0")
            editor.apply()
        }
        repository = SharedPreferencesGamePointSharedPrefsRepo.create(this)


        val intent = intent
        val profilePic = intent.getStringExtra("profileUrl")
        if (profilePic != null) {
            mProfilePicUrl = profilePic
        }

        //resultFromAuth()

        bottomNavigationView = findViewById(R.id.bottom_navigation)



//        Log.e(TAG, "=======> onCreate")


        resultFromPoints()

        onTokenRefreshed()
        binding.mainViewModel = mainViewModel


        appUpdateManager = AppUpdateManagerFactory.create(this)
        //checkAvailability()
        checkForUpdate()
//        mainViewModel.getUserFromDbSuccess.observe(this, Observer {
//            Log.e("JMG", "it: " + it)
//        if (it != null) {
//            refreshToken(it)
//        }
//    })
        mainViewModel.getUserFromDB()

        //addFragment(HomeFragment())

    }


    class HomeFragmentCallback(private val activity: MainActivity) :
        HomeFragment.HomeFragmentItemClickCallback {
        override fun onPickEmItemClicked(data: Pair<Int, GameShow>) {
            LogUtil.d("HomeFragmentCallback > onPickEmItemClicked > 187: ${data.first}")
            LogUtil.d("HomeFragmentCallback > onPickEmItemClicked > 187: ${data.second}")
            activity.changeFragments(PickEmFragment.newInstance(data.second))
        }

        override fun onEventClicked(data: EventsModel) {
            try {
                val gson = Gson()
                val eventDataStr = gson.toJson(data)

                val myIntent = Intent(activity, EventDetailActivity::class.java)
                myIntent.putExtra("event", eventDataStr) //Optional parameters
                activity.startActivityForResult(myIntent, 100)
            } catch (t: Throwable) {
                LogUtil.e("HomeFragmentCallback > onEventClicked > 191: ${t.localizedMessage}")
            }
        }

        override fun onRewardClicked(data: DealsUi) {
            activity.onSwitchRedeemDetail(data)
        }

        override fun onViewMoreEventBtnClicked() {
            activity.changeFragments(EventsFragment())
        }

        override fun onViewMoreRewardBtnClicked() {
            activity.changeFragments(DealFragment())
        }

    }

    private fun toggleNavigationTab(){
        if (bottomNavigationView?.visibility == View.VISIBLE){
            toggleHideNavigationTab()
        }else{
            toggleShowNavigationTab()
        }
    }

    private fun toggleHideNavigationTab(){
        bottomNavigationView!!.visibility = View.GONE
        header_right_btn.setImageResource(R.drawable.icon_header3)
    }

    private fun toggleShowNavigationTab(){
        bottomNavigationView!!.visibility = View.VISIBLE
        header_right_btn.setImageResource(R.drawable.ic_action_cancel)
    }


    fun setColors() {


        //Rob this is where I left off:  :( I slayed the dragon :)
        try {

            val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())

            val colors = intArrayOf(
                color!!,
                parseColor("#bbbbbb")
            )
            val myColorList = ColorStateList(states, colors)
            bottomNavigationView!!.itemIconTintList = myColorList
            bottomNavigationView!!.itemTextColor = myColorList


        }catch (e: Exception){
            Log.e("JMG", "catch set colors: " + e.toString())
        }
    }

    private fun checkForUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

//        Toast.makeText(this, "Check Version", Toast.LENGTH_SHORT).show()
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(IMMEDIATE)
            ) {
                // Request the update.
                appUpdateManager.startUpdateFlowForResult(
                        // Pass the intent that is returned by 'getAppUpdateInfo()'.
                        appUpdateInfo,
                        // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                        IMMEDIATE,
                        // The current activity making the update request.
                        this,
                        // Include a request code to later monitor this update request.
                        REQUEST_CODE_UPDATE)
            }
        }
        appUpdateInfoTask.addOnFailureListener {
//            Toast.makeText(this, "onFailure: " + it.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {


        super.onResume()
        resultFromDb()
//        var user = Gson().fromJson()
        setUpBottomNavigation()
        setColors()
        /**
         * if the user leaves app and opens it again
         * before the update has completed we resume the process
         */
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                        it,
                        IMMEDIATE,
                        this,
                        REQUEST_CODE_UPDATE
                )
            }
        }
        //resumeAppUpdateProcess()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {

                if (data != null) {
                    val MBuddle = data.extras
                    val MMessage = MBuddle?.getString("data")
                    val Message = MBuddle?.getString("event")
                    // The user picked a contact.
                    // The Intent's data Uri identifies which contact was selected.

                    // Do something with the contact here (bigger example below)
                    //val mString = data?.getStringExtra("event_data")

                    Log.e(TAG, "=======result $MMessage, $Message")
                }
                Log.e(TAG, "======= result was null")

            }
        }

        if (requestCode == REQUEST_CODE_UPDATE) {
            if (requestCode != Activity.RESULT_OK) {
                Log.e(TAG, "Update flow failed! Result code: $resultCode")
                // If the update is cancelled or fails,
                // you can request to start the update again.

            }
        }
    }


    fun backToHome(){

        val f = supportFragmentManager.findFragmentById(R.id.placeholder)
        if (f is HomeFragment){
            changeFragments(f)
        }else{
            changeFragments(HomeFragment.newInstance(callback = HomeFragmentCallback(this)))
        }


    }

    private fun updateUserPoint(user: UserResponse){
        findViewById<TextView>(R.id.txtUserPoint).text = user.redeemable
    }

    private fun resultFromDb() {
        mainViewModel.getUserFromDbSuccess.observe(this, Observer {
//            return@Observer

            if (it != null) {
                if (GamePointApplication.shared!!.shouldRefreshToken) {
                    GamePointApplication.shared!!.shouldRefreshToken = false
                    Log.e("JMG", "resultFromDb, gonna refresh the token")
                    refreshToken(it)
                } else {
                    letEveryoneKnow(it)
                }
            }
        })

        mainViewModel.writeUserToDbSucess.observe(this, Observer {
            //get user from db now because the model has changed
//            mainViewModel.getUserFromDB()
            updateUserPoint(it)
            letEveryoneKnow(it)
        })

        mainViewModel.getUserFromDbError.observe(this, Observer {
            startLogout()
        })
    }

    private fun onTokenRefreshed() {
        mainViewModel.tokenRefreshsuccess.observe(this, Observer { response ->
            letEveryoneKnow(response!!)
        })


    }


    /*
    * 1. This method checks weather or not the token is expired.
    * 2. It sets the data as a global variable so that the fragments can get data too.
    * 3. Did not have a better points for it
    */

    private fun refreshToken(user: UserResponse) {
        Log.e("JMG", "refreshTokenr: " + user.login)
        mainViewModel.getTokenRefresh(user.login.refresh_token, user.id.toString())

    }

    private fun letEveryoneKnow(user: UserResponse) {

        //save this new user to the database because they have everything we need.
        //  mainViewModel.updateDatabaseWithUser(response)
        Log.e("JMG", "letEveryoneKnow")



        Log.e(TAG, "Token is not expired, carry on.")

        mUser = user
        mToken = user.login.token
        mLoginModel = user.login
        saveLoginModelSp(user.login)


        Log.e("JMG", "User.points: " + user.redeemable)
        val editor = sharedPreferences.edit()
        editor.putString("refresh_token", user.login.refresh_token)
        editor.putString("logged_in_user_id", user.id.toString())
        editor.putString("access_token", user.login.token)
        editor.apply()
        disposables.add(repository.savePoints(TOTAL_POINTS_KEY, user.redeemable).subscribe())


        /*if (!sharedPreferences.getBoolean("refreshUserAfterRedeemingADeal", false)) {
            Log.e("JMG", "gonna change to events fragment")

            if (bottomNavigationView?.menu?.getItem(0)?.isChecked!!){
                changeFragments(EventsFragment())
            }


        } else {
            sharedPreferences.edit().putBoolean("refreshUserAfterRedeemingADeal", false).apply()
            Log.e("JMG", "should change to deals fragment")
            changeFragments(DealFragment())
        }*/
//        if (dt.isafter .isAfterNow) {
//            Log.e(TAG, "Token is expired. please refresh it")
//            mainViewModel.getTokenRefresh(response.user.login.refresh_token, response.user.id.toString())
//        } else {
//
//
//        }

        Log.e("JMG", "pushTokenSent: " + sharedPreferences.getBoolean("pushTokenSent", false))
        if (!sharedPreferences.getBoolean("pushTokenSent", false)) {
            getPushToken()

        }

        addFragment(HomeFragment.newInstance(HomeFragmentCallback(this)))

    }

    private fun getPushToken () {
//        Log.e("JMG", "getPushToken")
        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    Log.e("JMG", "task: " + task + " isSuccessful: " + task.isSuccessful)
                    if (!task.isSuccessful) {
                        Log.e("JMG", "getInstanceId failed", task.exception)
                        return@OnCompleteListener
                    }

                    // Get new Instance ID token
                    val token = task.result?.token

                    // Log and toast
                    val msg = "Got Token: " + token
                    Log.e("JMG", msg)
//                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    if (token != null) {
                        sendTokenToGmePointServer(token)
                    }
                })
    }

    private fun sendTokenToGmePointServer(token: String) {
        Log.e("JMG", "sendTokenToGmePointServer")
        mainViewModel.sendPushTokenSuccess.observe(this, Observer {
            sharedPreferences.edit().putBoolean("pushTokenSent", true).apply()
            sharedPreferences.edit().putString("pushTokenString", token).apply()
            Log.e("JMG", "sharedPreferences.getString(\"pushTokenString\", \"\")!!: " + sharedPreferences.getString("pushTokenString", "")!!)

        })
        mainViewModel.sendPushTokenError.observe(this, Observer {
            Log.e("JMG", "error send push token: " + it)
        })
        mainViewModel.sendPushTokenToServer(mToken!!, token)
    }

    private fun saveLoginModelSp(login: Login) {
        var gson = Gson()
        val loginStr = gson.toJson(login)

        val editor = sharedPreferences.edit()
        editor.putString("loginStr", loginStr)
        editor.apply()

    }

    private fun setUpBottomNavigation() {
        //bottomNavigationView!!.getMenu().removeItem(R.id.action_social);
        val isDealBeingRedeemed = sharedPreferences.getBoolean(Constants.TIMER_ACTIVE, false)
        val dealStr = sharedPreferences.getString(Constants.REDEEMED_DEAL, "")

        bottom_navigation!!.setOnNavigationItemSelectedListener { item ->
            toggleNavigationTab()
            if (isDealBeingRedeemed) {
                when (item.itemId) {
                    R.id.action_recents -> changeFragments(EventsFragment())
                    R.id.action_conference -> changeFragments(PickEmFragment.newInstance())
                    R.id.action_leaderboard -> changeFragments(GameShowLeaderBoardFragment())
                    R.id.action_reward -> changeFragments(DealFragment())
                    R.id.action_settings -> changeFragments(SettingsFragment.newInstance())
                }
                addBadge()

            } else {
                when (item.itemId) {
                    R.id.action_recents -> changeFragments(EventsFragment())
                    R.id.action_conference -> changeFragments(PickEmFragment.newInstance())
                    R.id.action_leaderboard -> changeFragments(GameShowLeaderBoardFragment())
                    R.id.action_reward -> changeFragments(DealFragment())
                    R.id.action_settings -> changeFragments(SettingsFragment.newInstance())
                }
            }
            true
        }


    }

    private fun addBadge() {

        val bottomNavigationMenuView = bottomNavigationView!!.getChildAt(0) as BottomNavigationMenuView

        val v = bottomNavigationMenuView.getChildAt(bottomNavigationMenuView.childCount - 1)
        val itemView = v as BottomNavigationItemView

        val badge = LayoutInflater.from(this@MainActivity).inflate(R.layout.notification_badge, itemView, false)
        itemView.addView(badge)
    }

    private fun removeBadge() {
        val bottomNavigationMenuView = bottom_navigation.getChildAt(0) as BottomNavigationMenuView
        val v = bottomNavigationMenuView.getChildAt(bottomNavigationMenuView.childCount - 1)
        val itemView = v as BottomNavigationItemView

        //val badge = LayoutInflater.from(this@MainActivity).inflate(R.layout.notification_badge, itemView, false)
        // itemView.removeView(badge)

        val badge = itemView.findViewById<RelativeLayout>(R.id.notification_badge);
        //((ViewGroup)badge.parent()).removeView(badge);
        //((ViewGroup)badge.getParent()).removeView(badge);
        //val me = badge as ViewGroup
        itemView.removeView(badge)
    }



    private fun changeFragments(fragment: androidx.fragment.app.Fragment) {
        LogUtil.d("MainActivity > changeFragments > 512: $mToken")

        val bundle = Bundle()
        bundle.putString("token", mToken.toString())

        // set Fragmentclass Arguments
        fragment.arguments = bundle

        val ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.placeholder, fragment)
        ft.commitAllowingStateLoss()
    }

    fun addFragment(fragment: androidx.fragment.app.Fragment) {
        LogUtil.d("MainActivity > addFragment > 512: $mToken")

        val bundle = Bundle()
        bundle.putString("token", mToken.toString())

        // set Fragmentclass Arguments
        val fragobj = fragment
        fragobj.arguments = bundle

        val ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.placeholder, fragobj)
        ft.commit()
    }

    /**
     * Track the number of points earned since the user was checked out
     * */

    private fun resultFromPoints() {

        val allPrefs: Map<String, *> = sharedPreferences.all //your sharedPreference

        val set = allPrefs.keys
        for (s in set) {
            Log.d(
                "MEOMEOMEO", s + "<" + allPrefs[s]!!.javaClass.simpleName + "> =  "
                        + allPrefs[s].toString()
            )
        }




        mainViewModel.points.observe(this, Observer {
            Log.e(TAG, "You got this many points $it")
//            val editor = sharedPreferences.edit()
//            editor.putString("pointsEarned",it.toString())
//            editor.apply()
            if (!isEventOver()) {
                disposables.add(repository.savePoints(POINTS_KEY, it.toString()).subscribe())

            }
        })

        mainViewModel.pointsThisCheckIn.observe(this, Observer {
            if (!isEventOver()) {
                disposables.add(repository.savePointsThisCheckIn(POINTS_THIS_CHECK_IN, it.toString()).subscribe())
            } else {
                disposables.add(repository.savePointsThisCheckIn(POINTS_THIS_CHECK_IN, "0").subscribe())
            }
        })

        mainViewModel.percentage.observe(this, Observer {

            if (!isEventOver()) {
                Log.e(TAG, "You got this many percent $it")
                disposables.add(repository.savePercent(PERCENT_KEY, it.toString()).subscribe())

            }

        })

        mainViewModel.elapsedTime.observe(this, Observer {
            //disposables.add(repository.savePreviousMinuets(PREVIOUS_MINUETS_KEY , it.toString() ).subscribe())
            if (!isEventOver()) {
                disposables.add(repository.savePercent(ELAPSED_TIME, it.toString()).subscribe())
            }
        })
    }

    private fun calculatePointsEarned(eventDuration: Int, checkInTimeStamp: String, totalPoints: Int, eventStartTime: String, eventId: Int, previousMinutesCheckedIn: Int) {
        runnableCode = object : Runnable {
            override fun run() {
                Log.e(TAG, "Calcualting points every minuet")
                mainViewModel.pointsEarned(eventDuration, checkInTimeStamp, getCurrentTime(), totalPoints, eventStartTime, eventId, previousMinutesCheckedIn, sharedPreferences)
                handler.postDelayed(this, 60000)
            }
        }
        handler.post(runnableCode as Runnable)
//        if (runnableCode == null){
//
//            Log.e(TAG, "do nothing here because there's already another instance.")
//        }else{
//            Log.e(TAG, "we should never reach this")
//        }

    }

    private fun getCurrentTime(): DateTime {
        val timeRightNow = StringFormatter.getDate()!!

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        val dt = formatter.parseDateTime(timeRightNow).toLocalDateTime()

        return DateTime(
                dt.year,
                dt.monthOfYear,
                dt.dayOfMonth,
                dt.hourOfDay,
                dt.minuteOfHour,
                dt.secondOfMinute
        )
    }

    /**
     * Callback methods for fragment transitions
     *
     * */

    override fun onPointsSelected() {
        changeFragments(GameShowLeaderBoardFragment())
    }

    override fun onPicksSelected() {
        Log.i(TAG, "onPicksSelected")

        changeFragments(StreakFragment())

    }

    override fun onBigTenPicksClicked() {
        Log.i(TAG, "onBigTenPicksClicked")
        changeFragments(PicksFragment())
    }

    override fun onResultsClicked() {
        Log.i(TAG, "onResultsClicked")

        changeFragments(ResultFragment())
    }

    override fun onRedeemClicked() {
        Log.e(TAG, "onRedeemClicked")

        changeFragments(DealDetailFragment())
    }

    override fun onSwitchRedeemDetail(data: DealsUi) {
        Log.e(TAG, "user wants to see deal details")
        val gson = Gson()
        val event = gson.toJson(data)
        changeToDealDetail(event)
    }

    private fun changeToDealDetail(event: String) {
        val bundle = Bundle()
        bundle.putString("token", mToken.toString())
        bundle.putString("event", event)

        // set Fragmentclass Arguments
        val fragobj = DealDetailFragment()
        fragobj.arguments = bundle
        isOnDealRedeemScreen = true
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.placeholder, fragobj)
        ft.commit()
    }

    override fun onRedeemPressed() {
        Log.i(TAG, "onRedeemPressed")
        changeFragments(DealDetailFragment())
    }

    override fun onBackToRedeem() {
        Log.i(TAG, "take user back to redeem screen from redeem details")
        val isDealBeingRedeemed = sharedPreferences.getBoolean(Constants.TIMER_ACTIVE, false)
       isOnDealRedeemScreen = false
        changeFragments(DealFragment())

//        if (!isDealBeingRedeemed){
//            changeFragments(DealFragment())
//        }else{
//            Snackbar.make(this.binding.root,"You must complete this transaction first",Snackbar.LENGTH_LONG).show()
//           // Toast.makeText(this, "You must redeem this deal first", Toast.LENGTH_LONG).show();
//        }
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        Log.e("JMG", "isOnDealRedeemScreen: " + isOnDealRedeemScreen)
        //isOnDealRedeemScreen = true
        /*if (isOnDealRedeemScreen) {
            onBackToRedeem()
            return
        }else {
            Log.e("JMG", "backstack: ${supportFragmentManager.backStackEntryCount}")
            if (!bottomNavigationView?.menu?.getItem(0)?.isChecked!!){
                if(supportFragmentManager.backStackEntryCount > 0){
                    supportFragmentManager.popBackStack()
                }else{
                    changeFragments(EventsFragment())
                    bottomNavigationView!!.menu.getItem(0).isChecked = true
                }

            }else{
                super.onBackPressed()
            }
        }*/


        Log.e("JMG", "backstack: ${supportFragmentManager.backStackEntryCount}")
        /*if (!bottomNavigationView?.menu?.getItem(0)?.isChecked!!){
            if(supportFragmentManager.backStackEntryCount > 0){
                supportFragmentManager.popBackStack()
            }else{
                changeFragments(HomeFragment.newInstance(HomeFragmentCallback(this)))
                bottomNavigationView!!.menu.getItem(0).isChecked = true

            }

        }else{
            super.onBackPressed()
        }*/
        if(supportFragmentManager.backStackEntryCount > 0){
            supportFragmentManager.popBackStack()
        }else{
            changeFragments(HomeFragment.newInstance(HomeFragmentCallback(this)))
            bottomNavigationView!!.menu.getItem(0).isChecked = true

        }

    }

    override fun onCheckedIn(minutes_to_redeem: Int, created_at: String, point_value: Int, start_time: String, eventId: Int) {
        //Log.i(TAG, "previous Minuets: ${getPreviousMinutesCheckedIn()}")
        mEventStartTime = start_time

        mEventLength = minutes_to_redeem.toInt()

        val editor = sharedPreferences.edit()
        editor.putString("start_time", start_time)
        editor.putString("event_id", eventId.toString())
        if (!sharedPreferences.getString("CONFIRM_CHECK_IN_TIMESTAMP", "").isNullOrEmpty()) {
            editor.putString("StartTime", sharedPreferences.getString("CONFIRM_CHECK_IN_TIMESTAMP", ""))
            mEventStartTime = sharedPreferences.getString("CONFIRM_CHECK_IN_TIMESTAMP", "")
            editor.putString("CONFIRM_CHECK_IN_TIMESTAMP", "")
        }
        editor.apply()

        calculatePointsEarned(minutes_to_redeem, created_at, point_value, start_time, eventId, previousMinutesCheckedIn(eventId))
    }

    override fun onCheckOut() {


        val todayGame = TodayGameModel(
                event_id = if (sharedPreferences.getString("event_id", "").isNullOrBlank()) 0 else sharedPreferences.getString("event_id", "")!!.toInt(),
                event_start_timestamp = if (sharedPreferences.getString("start_time", "").isNullOrBlank()) "" else sharedPreferences.getString("start_time", "")!!,
                elapsedMinuets = if (sharedPreferences.getString("elapsedMinuets", "").isNullOrBlank()) 0 else sharedPreferences.getString("elapsedMinuets", "")!!.toInt()
        )

        savePreviousMinutesCheckedIn(todayGame)


        val editor = sharedPreferences.edit()
        editor.remove("start_time")
        editor.remove("event_id")
        editor.remove("elapsedMinuets")
        editor.apply()

        runnableCode?.let { handler.removeCallbacks(it) }
        handler.removeMessages(0)
        isLoggingOut = false
        //remove geofence
        val geofenceEvent = getRepository().get("1")

        if (geofenceEvent != null) {
            removeGeofence(geofenceEvent)
        } else {
            Log.i(TAG, "geofenceEvetns were null when trying to get from shared preferences. myabe the user cleared the data.")
        }

    }

    override fun onRefreshUserInDatabase(userId: Int) {
        //save user to database
        //read from sp loginStr
        val loginStr = sharedPreferences.getString("loginStr", "")

        val gson = Gson()
        val loginModel = gson.fromJson(loginStr, Login::class.java)

        if (loginStr.isNullOrEmpty()) {
            Log.e(TAG, "login object was null when user checking out")
            mainViewModel.refresUser(mToken.toString(), userId.toString(), mLoginModel)
        } else {
            mainViewModel.refresUser(mToken.toString(), userId.toString(), loginModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runnableCode?.let { handler.removeCallbacks(it) }
        disposables.clear()
//        handler.removeCallbacks(runnableCode)
        handler.removeMessages(0)

    }

    private fun savePreviousMinutesCheckedIn(game: TodayGameModel) {
        val gson = Gson()
        val editor = sharedPreferences.edit()
        val todayGameList: ArrayList<TodayGameModel> = ArrayList()

        //do games exist?
        val gamesList = getPreviousMinutesCheckedIn()

        if (gamesList.isNullOrEmpty()) {
            //it's never existed before, so create it
            todayGameList.add(game)
            val newGames = gson.toJson(todayGameList)
            Log.e(TAG, "never saved games before, created: $newGames")
            editor.putString(TODAY_GAME_KEY, newGames)
            editor.apply()
        } else {
            //it does exist!! let's make some changes instead
            var found = false
            for (i in 0 until gamesList.size) {
                if (gamesList[i].event_id == game.event_id) {
                    //this event exists, so lets change a few things it
                    gamesList[i].event_id = game.event_id
                    gamesList[i].elapsedMinuets = game.elapsedMinuets
                    gamesList[i].event_start_timestamp = game.event_start_timestamp
                    found = true
                    break
                }
            }
            if (!found) {
                //this event does not exists, so add it.
                gamesList.add(game)
            }
            val existingGames = gson.toJson(gamesList)

            Log.e(TAG, "Saved games before, changed: $existingGames")

            editor.putString(TODAY_GAME_KEY, existingGames)
            editor.apply()
        }

    }

    private fun getPreviousMinutesCheckedIn(): ArrayList<TodayGameModel> {
        val gson = Gson()
        val gamesList: String? = sharedPreferences.getString(TODAY_GAME_KEY, "")
        val type = object : TypeToken<ArrayList<TodayGameModel>>() {}.type

        return if (gamesList.equals("") || gamesList == null) {
            //return this empty array. We have not written to sp yet.
            ArrayList()
        } else {
            gson.fromJson<ArrayList<TodayGameModel>>(gamesList, type)
        }
    }

    private fun previousMinutesCheckedIn(eventId: Int): Int {
        val gameList = getPreviousMinutesCheckedIn()
        var previousMinutesCheckedIn: Int? = null

        if (gameList.isNullOrEmpty()) {
            //did not exist before. return zero
            return 0
        } else {
            //they do exist, make sure the ids match
            for (i in 0 until gameList.size) {
                if (gameList[i].event_id == eventId) {
                    //id's match return the previousMinuets for this event
                    previousMinutesCheckedIn = gameList[i].elapsedMinuets
                    Log.e(TAG, "Inside loop, previousMinutesCheckedIn $previousMinutesCheckedIn ")
                } else {
                    previousMinutesCheckedIn = 0
                    Log.e(TAG, "Else loop, previousMinutesCheckedIn $previousMinutesCheckedIn ")

                    //this means that the user has not checked into the event before.
                    //meaning previousMinuets don't exist. so return zero
                }
            }
            Log.e(TAG, "Return previousMinutesCheckedIn $previousMinutesCheckedIn ")
            return previousMinutesCheckedIn!!
        }
    }

    private fun isEventOver(): Boolean {
        val now = StringFormatter.getFormatedTimeStamp(StringFormatter.getDate()!!)
        if (mEventStartTime != null) {
            val eventEndTime = StringFormatter.getFormatedTimeStamp(mEventStartTime!!).plusMinutes(mEventLength!!.toInt())
            if (now.isAfter(eventEndTime)) {
                //event is definatley over
                if (!isLoggingOut) {
                    isLoggingOut = true
                    val exitGeofenceIntent = Intent("EVENT_OVER")
                    Timer("checkOutAfterDelay", false).schedule(2000) {
                        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(baseContext).sendBroadcast(exitGeofenceIntent)
                    }
                }

                return true
            }
            //event is not over
            return false
        }
        //data was null, so user is not even checked in
        return false
    }

    private fun removeGeofence(reminder: EventGeofenceModel) {
        getRepository().remove(
                reminder,
                success = { eventId ->
                    Log.i(TAG, "removed geofence $eventId")

                },
                failure = {
                    Snackbar.make(main, it, Snackbar.LENGTH_LONG).show()
                })
    }

    override fun onAddBadge() {
        addBadge()

    }

    override fun refreshUserAfterRedeemingADeal() {
        Log.e("JMG", "refreshUserAfterRedeemingADeal")
        afterDealRedeem = true
        mainViewModel.refresUser(mToken.toString(), sharedPreferences.getString("logged_in_user_id", "")!!, mLoginModel)

    }

    override fun onRemoveBadge() {
        removeBadge()
    }

    private fun startLogout() {
        Log.e("JMG", "startLogout")
        val token = intent.getStringExtra("token")
        mainViewModel!!.unregisterPushTokenSuccess.observe(this, Observer {
            sharedPreferences.edit().putString("pushTokenString", "").apply()
            sharedPreferences.edit().putBoolean("pushTokenSent", false).apply()
            val editor = sharedPreferences.edit()
            editor.putBoolean(Constants.IsLogedIn, false)
            editor.apply()

            //make the api call
            logoutResult()
            token?.let { it1 -> mainViewModel!!.logout(it1) }
        })
        mainViewModel!!.unregisterPushTokenFail.observe(this, Observer {
            Log.e(TAG, "something went wrong while trying to unregister push token $it we will log user out anyways")
            val editor = sharedPreferences.edit()
            editor.putBoolean(Constants.IsLogedIn, false)
            editor.apply()

            //make the api call
            logoutResult()
            token?.let { it1 -> mainViewModel!!.logout(it1) }
        })

        Log.e("JMG", "sharedPreferences.getString(\"pushTokenString\", \"\")!!: " + sharedPreferences.getString("pushTokenString", "")!!)
        if (token != null) {
            token?.let {
                mainViewModel!!.unregisterPushToken(
                    it,
                    sharedPreferences.getString("pushTokenString", "")!!
                )

            }
        } else {
            closeAllActivities()
        }
    }

    private fun logoutResult() {
        mainViewModel!!.logoutSuccess.observe(this, Observer {
            closeAllActivities()
        })

        mainViewModel!!.logoutFail.observe(this, Observer {
            Log.e(TAG, "something went wrong while trying to log out $it we will log user out anyways")
            closeAllActivities()
        })


    }

    private fun closeAllActivities() {
        val intent = Intent(applicationContext, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        intent.putExtra("EXIT", true)
        startActivity(intent)
    }

}

private operator fun ColorStateList?.invoke(myList: ColorStateList) {

}

