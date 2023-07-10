package com.jedmahonisgroup.gamepoint.ui.leaderboard


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.adapters.leaderboard.PointsAdapter
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.UserResponse
import com.jedmahonisgroup.gamepoint.model.UserResponseModel
import com.jedmahonisgroup.gamepoint.model.leaderboard.EventLeaderboardModel
import com.jedmahonisgroup.gamepoint.ui.BaseFragment
import com.jedmahonisgroup.gamepoint.ui.settings.SettingsActivity
import com.jedmahonisgroup.gamepoint.utils.Constants
import com.jedmahonisgroup.gamepoint.utils.SharedPreferencesGamePointSharedPrefsRepo
import com.jedmahonisgroup.gamepoint.utils.gamePointSharedPrefsRepo
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_leaderboard.*

/**
 * A simple [Fragment] subclass.
 */
class PointsFragment : BaseFragment() {
    private var TAG: String = StreakFragment::class.java.simpleName


    private var listener: LeaderBoardTabsListener? = null

    private var binding: ViewDataBinding? = null
    private lateinit var pointsViewModel: PointsLeaderBoardViewModel

    private var repository: gamePointSharedPrefsRepo? = null
    private val disposables = CompositeDisposable()

    //Ui
    private var mSwipeToRefresh: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? = null
    private var pointsButton: Button? = null
    private var picksButton: Button? = null
    private var mTotalPoints: Button? = null
    private var mSettings: ImageButton? = null
    private var checkBack: View? = null
    private var tabs: View? = null
    private var prizeField: View? = null
    private var prizesView: View? = null
    private var prizeImage: CircleImageView? = null
    private var btnBack: ImageButton? = null
    private var prizeTopTextView: TextView? = null
    private var prizeBottomTextView: TextView? = null
    private var mUserModel: UserResponse? = null
//    private var tBar: Toolbar? = null
    private lateinit var pullToRefresh: SwipeRefreshLayout

    //Misc
    private var token: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_leaderboard, container, false)
        val rootView = binding?.root
        super.onCreateView(inflater, container, savedInstanceState)
        pointsViewModel = ViewModelProviders.of(this, ViewModelFactory(this.activity as AppCompatActivity)).get(PointsLeaderBoardViewModel::class.java)
        token = requireArguments().getString("token")
        Log.e(TAG + "Token", token.toString())

        repository = SharedPreferencesGamePointSharedPrefsRepo.create(requireActivity().baseContext)


        if (!isAfterAugustFirst()){
            setUpAugustFirstUi(rootView)
        }else{
            setUpUi(rootView)
        }

        pointsViewModel.getPointsFromDb()



        return rootView
    }

    private fun isAfterAugustFirst(): Boolean {
        var now = System.currentTimeMillis()
        if (now in 1590969600000..1598936400000) {
            return false
        }
        return true
    }


    override fun onResume() {
        super.onResume()
        token = requireArguments().getString("token")
        pointsViewModel.getEventLeaderboardData(token!!)

        Log.e("JMG", "toolbr_points: " + toolbar_points)
        setToolbarColors(toolbar_points)
        doSomeShit()
    }

    private fun doSomeShit(){

        /*val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.cornerRadii = floatArrayOf(0f, 0f, 60f, 60f, 60f, 60f, 0f, 0f)
        shape.setColor(resources.getColor(R.color.colorWhite))
        getSchoolColor()?.let { shape.setStroke(2, it) }
        picksButton!!.background = shape
        //getSchoolColor()?.let { picksButton!!.setTextColor(it) }

        val streakShape = GradientDrawable()
        streakShape.shape = GradientDrawable.RECTANGLE
        streakShape.cornerRadii = floatArrayOf(60f, 60f, 0f, 0.0f, 0f, 0f, 60f, 60f)
        getSchoolColor()?.let { streakShape.setColor(it) }
        getSchoolColor()?.let { streakShape.setStroke(2, it) }
        pointsButton!!.background = streakShape
        pointsButton!!.setTextColor(resources.getColor(R.color.colorWhite))*/

    }

    private fun setUpAugustFirstUi(rootView: View?){

        pullToRefresh = rootView!!.findViewById(R.id.points_swipe_refresh_layout)
        pullToRefresh.visibility = View.GONE
        checkBack = rootView?.findViewById(R.id.checkBackAugustFirst)
        checkBack?.visibility = View.VISIBLE
        tabs = rootView?.findViewById(R.id.tabs)
        tabs?.visibility = View.INVISIBLE
        pointsButton = rootView?.findViewById(R.id.pointsButton)
        picksButton = rootView?.findViewById(R.id.picksButton)
        mTotalPoints = rootView?.findViewById(R.id.totalPointCOunt)
        mSettings = rootView?.findViewById(R.id.pointsSettings)

        picksButton!!.setOnClickListener { listener!!.onPicksSelected() }

//        responseFromServer()


        setTotalPoints()

        launchSettings()

    }

    private fun setUpUi(rootView: View?) {
        pointsButton = rootView?.findViewById(R.id.pointsButton)
        picksButton = rootView?.findViewById(R.id.picksButton)
        mTotalPoints = rootView?.findViewById(R.id.totalPointCOunt)
        mSettings = rootView?.findViewById(R.id.pointsSettings)
        mSwipeToRefresh = rootView?.findViewById(R.id.points_swipe_refresh_layout)
//        tBar = rootView?.findViewById(R.id.toolbar_points)
        prizeField = rootView?.findViewById(R.id.prize_field)
        prizeImage = rootView?.findViewById(R.id.prizeImageView)
        prizeTopTextView = rootView?.findViewById(R.id.prizeTopLabel)
        prizeBottomTextView = rootView?.findViewById(R.id.prizeDescLabel)
        prizesView = rootView?.findViewById(R.id.prizesView)
//        Log.e("JMG", "color? ; " + color.toString())
//        tBar!!.background = color?.let { ColorDrawable(it) }
        mUserModel = GamePointApplication.shared!!.getCurrentUser(requireContext())

        if (mUserModel?.points_prize == null || mUserModel?.points_prize!!.title.isEmpty() || mUserModel?.points_prize!!.image.isEmpty()){
            prizeField?.visibility = View.GONE
        }else{
            prizeField?.visibility = View.VISIBLE
            prizeTopTextView?.text = mUserModel?.points_prize!!.title
            prizeBottomTextView?.text = mUserModel?.points_prize!!.description
            prizesView!!.setOnClickListener {
                goToPrizeUrl()
            }
            setPrizeImage()
        }




        //pointsButton.setOnClickListener(this);
        picksButton!!.setOnClickListener { listener!!.onPicksSelected() }
        responseFromServer()


        setTotalPoints()

        launchSettings()
        btnBack = rootView?.findViewById(R.id.back_arrow)
//        tBar = rootView?.findViewById(R.id.toolbar_points)

        //pointsButton.setOnClickListener(this);

        btnBack?.setOnClickListener { requireActivity().onBackPressed() }

        mSwipeToRefresh?.setOnRefreshListener {
            pointsViewModel.getEventLeaderboardData(token!!)
        }
    }

    private fun setPrizeImage(){
        try {
            prizeImage?.visibility = View.INVISIBLE
            if (mUserModel?.points_prize!!.image.isNotEmpty()) {
                Picasso.get()
                        .load(mUserModel?.points_prize!!.image)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.trophy_variant)
                        .into(prizeImage, object : Callback {
                            override fun onSuccess() {
                                prizeImage?.visibility = View.VISIBLE
                                //there were images from the cache
                            }

                            override fun onError(e: Exception?) {
                                Log.e(TAG, "No cached images, lead from server instead $e")
                                if (mUserModel?.points_prize!!.image.isNotEmpty()) {
                                    Picasso.get()
                                            .load(mUserModel?.points_prize!!.image)
                                            .placeholder(R.drawable.trophy_variant)
                                            .into(prizeImage, object : Callback {
                                                override fun onSuccess() {
                                                    prizeImage?.visibility = View.VISIBLE

                                                }

                                                override fun onError(e: Exception?) {
                                                    Log.e("error", "!!!!!")

                                                    //there were images from the cache

                                                }

                                            })
                                }
                            }

                        })
            }

        } catch (e: Exception) {
            prizeImage?.visibility = View.VISIBLE
            prizeImage?.background = resources.getDrawable(R.drawable.trophy_variant)
        }
    }

    private fun goToPrizeUrl(){
        val uri = Uri.parse(mUserModel?.points_prize?.prize_url) // missing 'http://' will cause crashed
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    private fun setTotalPoints() {
        disposables.add(repository?.points(Constants.TOTAL_POINTS_KEY)?.subscribe({ totalPoitns ->

            disposables.add(repository?.points(Constants.POINTS_THIS_CHECK_IN)?.subscribe({ newPts ->
                mTotalPoints?.text = totalPoitns.toInt().plus(newPts.toInt()).toString()

            }, {
                mTotalPoints?.text = totalPoitns

                Log.e("Error : ", "", it)
            })!!)
        }, {

            Log.e("Error : ", "", it)
        })!!)
    }

    private fun responseFromServer() {
        //get points from database
        pointsViewModel.pointFromDb.observe(viewLifecycleOwner, Observer {
            Log.i(TAG, "points from db resultsFromServer")
            displayData(it)
            pointsLeaderboardRv?.visibility = View.VISIBLE
            pointsNoDataView?.visibility = View.INVISIBLE

        })

        //database points failed,
        pointsViewModel.failedPointsFromDb.observe(viewLifecycleOwner, Observer {
            pointsLeaderboardRv?.visibility = View.VISIBLE
            pointsNoDataView?.visibility = View.INVISIBLE

            Log.e(TAG, "failed getting points: $it")
            try {
                pointsViewModel.getEventLeaderboardData(token!!)
            } catch (e: NullPointerException) {
                Log.e(TAG, "could not fetch points. Token is null")
                //Log user out or and try to refresh the token
            }
        })

        //try getting points from server
        pointsViewModel.leaderboardData.observe(viewLifecycleOwner, Observer { response ->
            Log.e(TAG, "here are the points data is: $response")
            mSwipeToRefresh!!.isRefreshing = false
            displayData(response)
            pointsNoDataView?.visibility = View.INVISIBLE
            pointsLeaderboardRv?.visibility = View.VISIBLE
        })

        //server failed
        pointsViewModel.errorFetchingLeaderboardData.observe(viewLifecycleOwner, Observer { response ->
            Log.e(TAG, "problem fetching points from server: $response")
            mSwipeToRefresh!!.isRefreshing = false
            pointsNoDataView?.visibility = View.VISIBLE
            pointsLeaderboardRv?.visibility = View.INVISIBLE
        })

        //saving to database failed
        pointsViewModel.errorSavingPointsToDb.observe(viewLifecycleOwner, Observer {
            Log.e(TAG, "problem saving points to db: $it")
        })


    }

    private fun displayData(response: List<EventLeaderboardModel>?) {
        try {
            if (response != null) {
                setupRecyclerview(response)
            } else {
                pointsNoDataView?.visibility = View.VISIBLE
            }
        } catch (e: NullPointerException) {
            Log.e(TAG, "points were null $e")
        }
    }

    private fun setupRecyclerview(response: List<EventLeaderboardModel>) {

        val newArr = ArrayList<EventLeaderboardModel>()
        var indexOfUser = -1

        var isInFirstEight: Boolean = false
        val isInLastPlace: Boolean = response[response.lastIndex].current_user

        val firstEight = response.take(8)


        //check to see if user in is the first 8
        for (streak in firstEight) {
            if (streak.current_user){
                isInFirstEight = true

            }
        }

        for (i in response.indices) {
            val l = response[i]
            if (l.current_user) {
                indexOfUser = i
            }
        }

        //check to see if user is the last item


        if (isInFirstEight) {
            //the user was in the first 8 so this lil thing should end here
            newArr.addAll(firstEight)
            for (i in response.indices){
                if (i in 8..14){
                    val l  = response[i]
                    newArr.add(l)

                }
            }
        } else {
            //adjust list because user was not in first 8
            val firstSeven = firstEight.take(7)
            for (i in firstSeven.indices) {
                val f = firstSeven[i]
                val newModel = EventLeaderboardModel(
                        name = f.name,
                        avatar = f.avatar,
                        total_points = f.total_points,
                        this_month_pick_points = f.this_month_pick_points,
                        this_month_event_points = f.this_month_event_points,
                        rank = f.rank,
                        current_user = f.current_user,
                        position = i
                )

                newArr.add(newModel)
            }

            //make sure user is not in last place
            if (isInLastPlace) {
                //user was in last place so add the last place plus the other points
                //newArr.addAll(firstSeven)


                if (indexOfUser != 8 && indexOfUser != 9) {
                    val second = EventLeaderboardModel(
                            name = response[indexOfUser - 2].name,
                            avatar = response[indexOfUser - 2].avatar,
                            total_points = response[indexOfUser - 2].total_points,
                            this_month_pick_points = response[indexOfUser - 2].this_month_pick_points,
                            this_month_event_points = response[indexOfUser - 2].this_month_event_points,
                            rank = response[indexOfUser - 2].rank,
                            current_user = response[indexOfUser - 2].current_user,
                            position = indexOfUser - 2
                    )

                    val first = EventLeaderboardModel(
                            name = response[indexOfUser - 1].name,
                            avatar = response[indexOfUser - 1].avatar,
                            total_points = response[indexOfUser - 1].total_points,
                            this_month_pick_points = response[indexOfUser - 1].this_month_pick_points,
                            this_month_event_points = response[indexOfUser - 1].this_month_event_points,
                            rank = response[indexOfUser - 1].rank,
                            current_user = response[indexOfUser - 1].current_user,
                            position = indexOfUser - 1
                    )
                    newArr.add(second)
                    newArr.add(first)
                }

                val last = EventLeaderboardModel(
                        name = response[response.lastIndex].name,
                        avatar = response[response.lastIndex].avatar,
                        total_points = response[response.lastIndex].total_points,
                        this_month_pick_points = response[response.lastIndex].this_month_pick_points,
                        this_month_event_points = response[response.lastIndex].this_month_event_points,
                        rank = response[response.lastIndex].rank,
                        current_user = response[response.lastIndex].current_user,
                        position = response.lastIndex
                )
                newArr.add(last)

            } else {
                //user was not in last place. they are between 7 and last place
                for (i in response.indices) {
                    val l = response[i]


                    //index of user was assigned a position
                    if (indexOfUser >= 0) {
                        //add items to list who's index is greater than item index.
                        //add 15 more items after that
                        val maxIndex = indexOfUser + 6
                        if (i in (indexOfUser - 1) until maxIndex) {
                          //  newArr.add(l)
                        //    newArr.add(response[indexOfUser])

                            val newModel = EventLeaderboardModel(
                                    name = l.name,
                                    avatar = l.avatar,
                                    total_points = l.total_points,
                                    this_month_pick_points = l.this_month_pick_points,
                                    this_month_event_points = l.this_month_event_points,
                                    rank = l.rank,
                                    current_user = l.current_user,
                                    position = i
                            )

                            newArr.add(newModel)
                        }
                    }
                }

            }
            //this new list should have the first 7 items and 7 more items after
            //user item for a total of 15 items on the list.
        }

//
//
        pointsLeaderboardRv.apply {
            //set a linear layout manager
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
            adapter = PointsAdapter(newArr.distinct(),getSchoolColorAsString())
        }
    }

    private fun launchSettings() {
        mSettings?.setOnClickListener {
            val myIntent = Intent(this.activity, SettingsActivity::class.java)
            myIntent.putExtra("token", token) //Optional parameters
            startActivity(myIntent)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PointsFragment.LeaderBoardTabsListener) {
            listener = context
        } else {
            throw ClassCastException(requireContext().toString() + " must implement PointsFragment.LeaderBoardTabsListener")
        }
    }

    interface LeaderBoardTabsListener {
        fun onPicksSelected()

    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()

    }


}
