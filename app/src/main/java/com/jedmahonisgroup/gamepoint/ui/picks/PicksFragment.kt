package com.jedmahonisgroup.gamepoint.ui.picks


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.BuildConfig
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.RecyclerviewClickListener
import com.jedmahonisgroup.gamepoint.adapters.picks.PicksDayAdapter
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.UserResponse
import com.jedmahonisgroup.gamepoint.model.UserResponseModel
import com.jedmahonisgroup.gamepoint.model.picks.OpenPicksModel
import com.jedmahonisgroup.gamepoint.model.picks.Picks
import com.jedmahonisgroup.gamepoint.model.picks.UserPick
import com.jedmahonisgroup.gamepoint.ui.BaseFragment
import com.jedmahonisgroup.gamepoint.ui.settings.SettingsActivity
import com.jedmahonisgroup.gamepoint.utils.Constants
import com.jedmahonisgroup.gamepoint.utils.Constants.PICKS_TIMER_TIMESTAMP
import com.jedmahonisgroup.gamepoint.utils.SharedPreferencesGamePointSharedPrefsRepo
import com.jedmahonisgroup.gamepoint.utils.StringFormatter
import com.jedmahonisgroup.gamepoint.utils.StringFormatter.getDate
import com.jedmahonisgroup.gamepoint.utils.gamePointSharedPrefsRepo
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.event_expired_error.view.*
import kotlinx.android.synthetic.main.fragment_conference.*
import kotlinx.android.synthetic.main.fragment_conference.view.*
import kotlinx.android.synthetic.main.picks_timer_layout.view.*
import org.joda.time.Hours
import org.joda.time.Minutes
import org.joda.time.Seconds
import org.joda.time.format.DateTimeFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class PicksFragment : BaseFragment(), AdapterView.OnItemSelectedListener, RecyclerviewClickListener {


    private var TAG: String = PicksFragment::class.java.simpleName

    private var spinner_button: Spinner? = null
    private var listner: FragmentChangeListener? = null

    private var binding: ViewDataBinding? = null
    private var TOKEN: String? = null
    private lateinit var picksViewModel: PicksViewModel
    private var mUser: UserResponse? = null

    //misc
    private var token: String? = null
    private var repository: gamePointSharedPrefsRepo? = null
    private val disposables = CompositeDisposable()

    //UI
    private var mSettings: ImageButton? = null
    private var mPointCount: Button? = null
    private var mUserName: TextView? = null
    private var mMonthlyTotal: TextView? = null
    private var mCurrentSreak: TextView? = null
    private var mRank: TextView? = null
    private var mProfileImage: CircleImageView? = null
    private var mSwipeToRefresh: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? = null
    private var mTriviaSwipeToRefresh: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? = null

    private var mPicksEmptyView: RelativeLayout? = null
    private var mPicksNullView: RelativeLayout? = null
    private var augustFirstView: View? = null
    private var infoBar: View? = null

    private var mRecyclerView: androidx.recyclerview.widget.RecyclerView? = null
    private var mTriviaRecyclerView: androidx.recyclerview.widget.RecyclerView? = null
    private var mAdapter: PicksDayAdapter? = null
    private var mGoToTriviaPicksView: RelativeLayout? = null
    private var mStartTriviaQuestionsTextView: TextView? = null
    private var mStartTriviaQuestionsTimeTextView: TextView? = null
    private var picksFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

    lateinit var editor: SharedPreferences.Editor

    private lateinit var pullToRefresh: SwipeRefreshLayout


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_conference, container, false)
        val rootView = binding?.root
        super.onCreateView(inflater, container, savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        editor = sharedPreferences.edit()
        getToken()


        picksViewModel = ViewModelProviders.of(this, ViewModelFactory(this.activity as AppCompatActivity)).get(PicksViewModel::class.java)
        repository = SharedPreferencesGamePointSharedPrefsRepo.create(requireActivity().baseContext)
        setUpUi(rootView)

        responseFromServer()

        return rootView
    }

    override fun onResume() {
        super.onResume()
        getToken()
        picksViewModel.getMyPicksFromServer(TOKEN!!)
        setToolbarColors(toolbar)
    }

    private fun getToken() {
        token = requireArguments().getString("token")

        TOKEN = token

        mUser = GamePointApplication.shared!!.getCurrentUser(requireContext())
    }

    private fun responseFromServer() {

        // lets observe server data response
        picksViewModel.picksFromServer.observe(viewLifecycleOwner, Observer { picks ->
            if (picks != null) {
                Log.e(TAG, "here are the picks data is: $picks")
                mSwipeToRefresh!!.isRefreshing = false
                displayData(picks)
               //myDataEmptyView?.visibility = View.INVISIBLE

            } else {
                //picks are null
                myPicksRLNoData?.visibility = View.VISIBLE

                myPicksRv?.visibility = View.GONE
                myDataEmptyView?.visibility = View.GONE

            }
            picksViewModel.getMyTriviaPicksFromServer(TOKEN!!)

        })

        picksViewModel.serverDataFailed.observe(viewLifecycleOwner, Observer { response ->
            Log.e(TAG, "problem fetching picks from server: $response")
            mSwipeToRefresh!!.isRefreshing = false
            myPicksRLNoData?.visibility = View.VISIBLE
            myDataEmptyView?.visibility = View.GONE
            myPicksRv?.visibility = View.INVISIBLE
        })


        //could not save streak to db
        picksViewModel.errorSavingPicks.observe(viewLifecycleOwner, Observer {
            Log.e(TAG, "problem saving picks to db: $it")

        })

        /**
         * Post deal response
         */

        //posted deal successfully
        picksViewModel.postSuccess.observe(viewLifecycleOwner, Observer {
            Log.i(TAG, "post resultsFromServer: $it")

        })

        //failed posting deal
        picksViewModel.postFail.observe(viewLifecycleOwner, Observer {
            Log.e(TAG, "problem posting deal: $it")
            //display alert dialog
            pickClosedAlert("" + it)

        })

        picksViewModel.picksTriviaFromServer.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) {
                goToTriviaPicksView.visibility = View.GONE
            } else {
                goToTriviaPicksView.visibility = View.VISIBLE

                if (!isNextDay()) {

                    mStartTriviaQuestionsTimeTextView!!.visibility = View.GONE
                    mStartTriviaQuestionsTextView!!.text = getString(R.string.todays_trivia_is_finished)
                } else {

                    goToTriviaPicksView.setOnClickListener {
                        triviaPicksPressed()
                    }
                    setupTriviaRecyclerview(it)
                }
            }

        })

    }

    fun triviaPicksPressed() {
        setTimerScreen(binding!!.root)
    }

    private fun pickClosedAlert(it: String) {
        Log.i(TAG, "Pick was closed")
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

    private fun displayData(picks: List<OpenPicksModel>) {
        try {
            if (picks.isEmpty()) {
                //display the empty screen
                mPicksNullView?.visibility = View.GONE
                mPicksEmptyView?.visibility = View.VISIBLE

            } else if (!picks.isNullOrEmpty()) {
                Log.e(TAG, "Picks were neither null nor empty")
                mPicksEmptyView?.visibility = View.GONE
                mPicksNullView?.visibility = View.GONE
                setupRecyclerview(picks)

            }
        } catch (e: NullPointerException) {
            Log.e(TAG, "picks were null $e")
            //display null
        }
    }

    private fun setUpUi(rootView: View?) {
        mPointCount = rootView?.findViewById(R.id.myPickPointCount)
        mSettings = rootView?.findViewById(R.id.myPickSettings)
        mUserName = rootView!!.findViewById(R.id.userName)
        mMonthlyTotal = rootView.findViewById(R.id.monthlyTotal)
        mCurrentSreak = rootView.findViewById(R.id.currentStreak)
        mRank = rootView.findViewById(R.id.rank)
        mProfileImage = rootView.findViewById(R.id.profilePicPicks)
        mSwipeToRefresh = rootView.findViewById(R.id.picks_swipe_refresh_layout)
        mRecyclerView = rootView.findViewById(R.id.myPicksRv)
        mTriviaRecyclerView = rootView.findViewById(R.id.trivia_PicksRv)
        mTriviaSwipeToRefresh = rootView.findViewById(R.id.trivia_picks_swipe_refresh_layout)
        mPicksEmptyView = rootView.findViewById(R.id.myDataEmptyView)
        mPicksNullView = rootView.findViewById(R.id.myPicksRLNoData)
        mGoToTriviaPicksView = rootView.findViewById(R.id.goToTriviaPicksView)
        mStartTriviaQuestionsTextView = rootView.findViewById(R.id.startTriviaQuestionsTextView)
        mStartTriviaQuestionsTimeTextView = rootView.findViewById(R.id.startTriviaQuestionsTimeTextView)
        mStartTriviaQuestionsTextView!!.setTextColor(color!!)
        mStartTriviaQuestionsTimeTextView!!.setTextColor(color!!)
        mSettings?.setOnClickListener {
            val myIntent = Intent(this.activity, SettingsActivity::class.java)
            myIntent.putExtra("token", token) //Optional parameters
            startActivity(myIntent)
        }

        setTotalPoints()

        mSwipeToRefresh?.setOnRefreshListener {
            //load items
            picksViewModel.getMyPicksFromServer(token!!)
        }
        mTriviaSwipeToRefresh?.setOnRefreshListener {
            picksViewModel.getMyTriviaPicksFromServer(token!!)
        }
        userCard(rootView)
        setupSpinner(rootView)


        augustFirstView = rootView.findViewById(R.id.checkBackAugustFirstPicks)
        augustFirstView?.visibility = View.GONE


//        setTimerScreen(rootView)
    }


    private fun setTimerScreen(rootView: View) {

        if (!isNextDay()) {
            //display timer wall
            picksWall(rootView, geElapsedHours(), getElapsedMinuets())
        } else {
            // it is the next day
            if (getElapsedMinuets() < 1) {
                rootView.picks_parent_view.visibility = View.VISIBLE
                mTriviaRecyclerView!!.visibility = View.VISIBLE
                mTriviaSwipeToRefresh!!.visibility = View.VISIBLE
                rootView.picks_timer_include.visibility = View.GONE

                timerTask(rootView, getDate())

                //
            } else {
                rootView.picks_parent_view.visibility = View.GONE
                mTriviaRecyclerView!!.visibility = View.GONE
                mTriviaSwipeToRefresh!!.visibility = View.GONE
                rootView.picks_timer_include.visibility = View.VISIBLE


                rootView.start_timer_btn.background = requireActivity().getDrawable(R.drawable.green_btn_filled_background)
                rootView.picksTimerTitle.text = "New picks are ready"
                rootView.start_timer_btn.text = "Get Started"
                rootView.imageView1.visibility = View.VISIBLE
                rootView.imageView2.visibility = View.GONE

                rootView.start_timer_btn.setOnClickListener {
                    //timestamp when user selected start

                    if (!isNextDay() ) {
                        rootView.picks_parent_view.visibility = View.VISIBLE
                        mTriviaRecyclerView!!.visibility = View.GONE
                        mTriviaSwipeToRefresh!!.visibility = View.GONE
                        rootView.picks_timer_include.visibility = View.GONE
                    } else {
                        Log.e("JMG", "start_timer_btn clicked")
                        editor.putString(PICKS_TIMER_TIMESTAMP, getDate())
                        editor.apply()

                        //display the views
                        rootView.picks_parent_view.visibility = View.VISIBLE
                        rootView.picks_timer_include.visibility = View.GONE
                        mTriviaSwipeToRefresh!!.visibility = View.VISIBLE
                        mTriviaRecyclerView!!.visibility = View.VISIBLE
                        //change the text of timer ui
                        rootView.start_timer_btn.background = requireActivity().getDrawable(R.drawable.green_btn_filled_background)
                        rootView.picksTimerTitle.text = "Submit your picks"
                        rootView.start_timer_btn.visibility = View.GONE
                        rootView.btn_submit_picks.visibility = View.GONE
                        mTriviaRecyclerView!!.visibility = View.VISIBLE
                        mTriviaSwipeToRefresh!!.visibility = View.VISIBLE
                        mRecyclerView!!.visibility = View.GONE
                        mSwipeToRefresh!!.visibility = View.GONE
                        goToTriviaPicksView.visibility = View.GONE
                        timerTask(rootView, getDate())
                    }

                }
            }


            Log.e(TAG, "This is not older than 24 hours")
        }


    }

    private fun isNextDay(): Boolean {
        val timeStampValue = sharedPreferences.getString(PICKS_TIMER_TIMESTAMP, "")

//        editor.putString(PICKS_TIMER_TIMESTAMP, "2020-03-31T12:23:20.672-0500")
//        editor.apply()
        if (timeStampValue.isNullOrEmpty()) {
            editor.putString(PICKS_TIMER_TIMESTAMP, "1996-04-01T12:23:20.672-0500")
            editor.apply()
            return true
        }
        val picksHourDt = picksFormatter.parseDateTime(sharedPreferences.getString(PICKS_TIMER_TIMESTAMP, ""))
        val day = picksHourDt.dayOfYear();
        val currentDay = picksFormatter.parseDateTime(getDate())
        return day < currentDay
    }

    private fun geElapsedHours(): Int {
        val timeStampValue = sharedPreferences.getString(PICKS_TIMER_TIMESTAMP, "")

//        editor.putString(PICKS_TIMER_TIMESTAMP, "1996-04-01T12:23:20.672-0500")
//        editor.apply()
        if (timeStampValue.isNullOrEmpty()) {
            editor.putString(PICKS_TIMER_TIMESTAMP, "1996-04-01T12:23:20.672-0500")
            editor.apply()
        }

        val picksHourDt = picksFormatter.parseDateTime(sharedPreferences.getString(PICKS_TIMER_TIMESTAMP, ""))
        val timerEndTime = picksHourDt.plusMinutes(1)
        val currentHourDt = picksFormatter.parseDateTime(getDate())

        return if (!timerEndTime.isAfterNow) {
            Hours.hoursBetween(picksHourDt, currentHourDt).hours
        } else {
            //this indicates the user is navigating away and returned while the timer was running
            1000
        }

    }

    private fun getElapsedMinuets(): Int {
        val picksMinuet = picksFormatter.parseDateTime(sharedPreferences.getString(PICKS_TIMER_TIMESTAMP, ""))
        val currentMinuet = picksFormatter.parseDateTime(getDate())

        return Minutes.minutesBetween(picksMinuet, currentMinuet).minutes
    }

    private fun picksWall(rootView: View, elapsedHrs: Int, elapsedMins: Int) {
        rootView.picks_parent_view.visibility = View.GONE
        rootView.picks_timer_include.visibility = View.VISIBLE

        val displayText = if (24 - elapsedHrs == 0) {
            "00:$elapsedMins minuets"
        } else {
            "${24 - elapsedHrs} hours"
        }

        rootView.picksTimerTitle.text = "It looks like you have already made your picks for today."
        rootView.start_timer_btn.visibility = View.VISIBLE
        rootView.start_timer_btn.text = "Back"
        rootView.start_timer_btn.background = requireActivity().getDrawable(R.drawable.red_button_round_corners)
//        rootView.start_timer_btn.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 70)
        rootView.picksInfoText.text = "More picks will be available tomorrow"
        rootView.imageView1.visibility = View.GONE
        rootView.imageView2.visibility = View.VISIBLE
        rootView.start_timer_btn.setOnClickListener {
            rootView.picks_parent_view.visibility = View.VISIBLE
            mTriviaRecyclerView!!.visibility = View.GONE
            mTriviaSwipeToRefresh!!.visibility = View.GONE
            mRecyclerView!!.visibility = View.VISIBLE
            mSwipeToRefresh!!.visibility = View.VISIBLE
            mGoToTriviaPicksView!!.visibility = View.VISIBLE
            mStartTriviaQuestionsTimeTextView!!.visibility = View.GONE
            mStartTriviaQuestionsTextView!!.text = getString(R.string.todays_trivia_is_finished)
            rootView.picks_timer_include.visibility = View.GONE
        }

    }

    private fun timerTask(rootView: View, date: String?) {
        val cicksHourDt = picksFormatter.parseDateTime(sharedPreferences.getString(PICKS_TIMER_TIMESTAMP, ""))
        val cimerEndTime = cicksHourDt.plusSeconds(30)
        val purrentHourDt = picksFormatter.parseDateTime(getDate())


        val t = Timer()
        var secondsToCount = Seconds.secondsBetween(purrentHourDt, cimerEndTime).seconds

        t.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    run {
                        rootView.timerText.text = "0:$secondsToCount"
                        rootView.timerText.setTextColor(color!!)
                        rootView.timerText.visibility = View.VISIBLE
                        if (secondsToCount == 0) {
                            t.cancel()
                            rootView.timerText.visibility = View.GONE
                            //show the timer wall again
                            picksWall(rootView, geElapsedHours(), getElapsedMinuets())

                            Log.e(TAG, "timer stopped")
                        } else {
                            secondsToCount -= 1
                        }
                    }
                }
            }
        }, 0, 1000)
    }


    private fun setTotalPoints() {
        disposables.add(repository?.points(Constants.TOTAL_POINTS_KEY)?.subscribe({ totalPoitns ->

            disposables.add(repository?.points(Constants.POINTS_THIS_CHECK_IN)?.subscribe({ newPts ->
                Log.e("JMG", "newPts: " + newPts)
                mPointCount?.text = totalPoitns.toInt().plus(newPts.toInt()).toString()

            }, {
                mPointCount?.text = totalPoitns
                Log.e("Error : ", "", it)
            })!!)
        }, {

            Log.e("Error : ", "", it)
        })!!)
    }

    @SuppressLint("SetTextI18n")
    private fun userCard(rootView: View?) {

        mUserName!!.setTextColor(resources.getColor(R.color.black_transparent))
        mUserName!!.text = "${mUser?.first_name?.capitalize()}" + " " + "${mUser?.last_name?.first()?.toUpperCase()}."
        mMonthlyTotal!!.text = if (mUser?.highest_streak != null) "Highest Streak: ${mUser?.highest_streak}" else "Highest Streak: 0"
        mCurrentSreak!!.text = if (mUser?.current_streak != null) "Current Streak: ${mUser?.current_streak}" else "Current Streak: 0"
        mRank!!.text = if (mUser?.pick_rank != null) "Rank:${mUser?.pick_rank}" else "Rank: 0"
        getProfilePic()
    }

    private fun getProfilePic() {
        try {
            if (!mUser!!.avatar.isNullOrEmpty()) {
                Picasso.get()
                        .load(mUser!!.avatar)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.ic_user_account)
                        .into(mProfileImage, object : Callback {
                            override fun onSuccess() {
                                //there were images from the cache
                            }

                            override fun onError(e: Exception?) {
                                Log.e(TAG, "No cached images, lead from server instead $e")
                                if (!mUser!!.avatar.isNullOrEmpty()) {
                                    Picasso.get()
                                            .load(mUser!!.avatar)
                                            .placeholder(R.drawable.ic_user_account)
                                            .into(mProfileImage, object : Callback {
                                                override fun onSuccess() {

                                                }

                                                override fun onError(e: Exception?) {
                                                    //there were images from the cache
                                                    Log.e(TAG, " Picasso Could not fetch profile pic a second time from ${BuildConfig.BASE_URL + mUser!!.avatar}, stack trace,e $e")

                                                }

                                            })
                                }
                            }

                        })
            }

        } catch (e: Exception) {
            mProfileImage?.background = resources.getDrawable(R.drawable.ic_user_account)
        }
        try {
            Log.e(TAG, "profile img url ${BuildConfig.BASE_URL + mUser!!.avatar}")
        } catch (e: Exception) {
            Log.e(TAG, "profile img url hit the catch block cuz something is null")
        }
    }

    // Store the listener (activity) that will have events fired once the fragment is attached
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PicksFragment.FragmentChangeListener) {
            listner = context
        } else {
            throw ClassCastException(context.toString() + " must implement PicksFragment.FragmentChangeListener")
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

        if (parent.getItemAtPosition(position).toString() == "Results") {
            try {
                listner?.onResultsClicked()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {

    }

    private fun setupRecyclerview(listOfitems: List<OpenPicksModel>) {
        val response = randomFourGenerator(ArrayList(listOfitems))
        val timeStamps = ArrayList<String>()
        val dayList = ArrayList<Int>()

        for (event in response) {
            val startTime = StringFormatter.convertTimestampToDate(event.start_time)
            timeStamps.add(startTime)
        }

        //get the unique time stamps. this will be the different days.
        val uniqueTimeStamps: Set<String> = HashSet<String>(timeStamps)
//        Log.e(TAG, "uniqueTimeStamps : $uniqueTimeStamps")

        //create a list of day events
        for (unique in uniqueTimeStamps) {
            dayList.add(StringFormatter.getDayOfYear(unique))
        }
        Log.i(TAG, "unique days : $dayList")

        //sort the dayList in ascending  order
        val original: Array<Int> = dayList.toList().toTypedArray()
        val sortedArray: Array<Int> = original.sortedArray()
        Log.i(TAG, "original data : $original")

        //set up recyclerview
        mAdapter = PicksDayAdapter(this@PicksFragment)
        mRecyclerView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.activity)
        mRecyclerView?.adapter = mAdapter
        mAdapter!!.refreshList(sortedArray, response)

//        myPicksRv.apply {
//            layoutManager = LinearLayoutManager(activity)
//            adapter = PicksDayAdapter(response, this@PicksFragment)
//        }
    }

    private fun setupTriviaRecyclerview(listOfitems: List<OpenPicksModel>) {
        val response = randomFourGenerator(ArrayList(listOfitems))
        val timeStamps = ArrayList<String>()
        val dayList = ArrayList<Int>()

        for (event in response) {
            val startTime = StringFormatter.convertTimestampToDate(event.start_time)
            timeStamps.add(startTime)
        }

        //get the unique time stamps. this will be the different days.
        val uniqueTimeStamps: Set<String> = HashSet<String>(timeStamps)
//        Log.e(TAG, "uniqueTimeStamps : $uniqueTimeStamps")

        //create a list of day events
        for (unique in uniqueTimeStamps) {
            dayList.add(StringFormatter.getDayOfYear(unique))
        }
        Log.i(TAG, "unique days : $dayList")

        //sort the dayList in ascending  order
        val original: Array<Int> = dayList.toList().toTypedArray()
        val sortedArray: Array<Int> = original.sortedArray()
        Log.i(TAG, "original data : $original")

        //set up recyclerview
        mAdapter = PicksDayAdapter(this@PicksFragment)
        mTriviaRecyclerView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.activity)
        mTriviaRecyclerView?.adapter = mAdapter
        mAdapter!!.refreshList(sortedArray, response)

//        myPicksRv.apply {
//            layoutManager = LinearLayoutManager(activity)
//            adapter = PicksDayAdapter(response, this@PicksFragment)
//        }
    }

    private fun randomFourGenerator(list: ArrayList<OpenPicksModel>): List<OpenPicksModel> {
        val newResponseList = ArrayList<OpenPicksModel>()
        var indexesThatHaveHappenedList = ArrayList<Int>()
        var four = 4
        if (list.size < 4) {
            four = list.size
        }
        repeat(four) {



            val index = generateRandomNumberWithSizeAndListOfIndexes(list.size, indexesThatHaveHappenedList)

            indexesThatHaveHappenedList.add(index)
            val item = list[index]

            if (newResponseList.contains(item)){
                val differentItem = list[index]
                newResponseList.add(differentItem)

            }else{
                newResponseList.add(item)
            }



        }

        return newResponseList.toList()
    }

    private fun generateRandomNumberWithSizeAndListOfIndexes(size: Int, list:ArrayList<Int>): Int {
        val randomGenerator = Random()
        val index = randomGenerator.nextInt(size)
        Log.e("JMG", "index: " + index + " size: " + size)
        if (!list.contains(index)) {
            list.add(index)
            return index
        } else {
            return  generateRandomNumberWithSizeAndListOfIndexes(size, list)
        }
    }

    private fun setupSpinner(rootView: View?) {
        spinner_button = rootView?.findViewById(R.id.flute)
        //Spinner spinner = (Spinner) findViewById(R.id.yourspinnerid);
        //        String text = spinner_button.getSelectedItem().toString();
        //  Log.e("Here", text);

        val list = ArrayList<String>()
        list.add("Big Ten Picks")
        list.add("Results")


        val adapter = ArrayAdapter(requireActivity(),
                R.layout.view_spinner_item, list)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_button!!.adapter = adapter
        spinner_button!!.onItemSelectedListener = this

    }

    override fun onTeam1Clicked(team1: String, pickId: Int) {
        val pick = Picks(
                user_pick = UserPick(
                        user_id = mUser!!.id,
                        team = 1,
                        pick_id = pickId

                )
        )
        picksViewModel.postUserPicks(TOKEN!!, pick)
        updateDBFromPickWithID(pickId, 1)
    }

    override fun onTeam2Clicked(team2: String, id: Int) {
        Log.i(TAG, "clicked =========> $team2")
        val pick = Picks(
                user_pick = UserPick(
                        user_id = mUser!!.id,
                        team = 2,
                        pick_id = id
                )
        )
        picksViewModel.postUserPicks(TOKEN!!, pick)
        updateDBFromPickWithID(id, 2)


    }

    fun updateDBFromPickWithID(id: Int, myPick: Int) {
        picksViewModel.picksFromDb.observe(this, Observer { picks ->
            if (picks != null) {
                var size = picks.size - 1
                var j = -1
                for (i in 0..size) {
                    var pck = picks.get(i)
                    Log.e("JMG", "pck.id: " + pck.id + " id: " + id)
                    if (pck.id == id) {
                        j = i
                        break
                    }
                }
                if (j > -1) {
                    var openPick = picks.get(j)
                    openPick.my_pick = myPick
                    picksViewModel.updatePickInDB(openPick)
                } else {
                    Toast.makeText(context, "THIS SUCKS", Toast.LENGTH_LONG)
                }
//            myDataEmptyView?.visibility = View.INVISIBLE
            } else {
                //pciks were null
                myPicksRLNoData?.visibility = View.INVISIBLE

            }

        })

    }

    interface FragmentChangeListener {
        fun onResultsClicked()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

}
