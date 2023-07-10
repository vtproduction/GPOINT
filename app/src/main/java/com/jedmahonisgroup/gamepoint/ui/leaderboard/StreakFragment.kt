package com.jedmahonisgroup.gamepoint.ui.leaderboard


import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.adapters.leaderboard.StreakAdapter
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.UserResponse
import com.jedmahonisgroup.gamepoint.model.UserResponseModel
import com.jedmahonisgroup.gamepoint.model.leaderboard.EventLeaderboardModel
import com.jedmahonisgroup.gamepoint.model.leaderboard.StreakModel
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
import kotlinx.android.synthetic.main.fragment_leaderboard_picks.*

/**
 * A simple [Fragment] subclass.
 */
class StreakFragment : BaseFragment() {
    private var TAG: String = StreakFragment::class.java.simpleName

    private var pointsButton: Button? = null
    private var picksButton: Button? = null
    private var totalPts: TextView? = null
    private var listener: PointsTouchedListener? = null

    private var binding: ViewDataBinding? = null
    private lateinit var streakViewModel: StreakViewModel

    private var repository: gamePointSharedPrefsRepo? = null
    private val disposables = CompositeDisposable()

    private var token: String? = null

    //ui
    private var mSettings: ImageButton? = null
    private var mNoDataView: RelativeLayout? = null
    private var mSwipeToRefresh: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? = null
    private var prizeField: View? = null
    private var prizesView: View? = null
    private var prizeImage: CircleImageView? = null
    private var prizeTopTextView: TextView? = null
    private var prizeBottomTextView: TextView? = null
    private var mUser: UserResponse? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_leaderboard_picks, container, false)
        val rootView = binding?.root
        super.onCreateView(inflater, container, savedInstanceState)


        streakViewModel = ViewModelProviders.of(this, ViewModelFactory(this.activity as AppCompatActivity)).get(StreakViewModel::class.java)
        repository = SharedPreferencesGamePointSharedPrefsRepo.create(requireActivity().baseContext)

        token = requireArguments().getString("token")
        Log.e(TAG + "Token", token.toString())

        streakViewModel.getDbStreak()
        //onFetchDataSucessDisplayRv()
        onFetchDataError()

        pointsButton = rootView?.findViewById(R.id.pointsButton)
        mSettings = rootView?.findViewById(R.id.streakSettings)
        totalPts = rootView?.findViewById(R.id.totalPointCOunt)
        picksButton = rootView?.findViewById(R.id.picksButton)
        mSwipeToRefresh = rootView?.findViewById(R.id.streak_swipe_refresh_layout)
        mNoDataView = rootView?.findViewById(R.id.noDataView)

        setUpView(rootView)
        responseFromStreaksViewModel()
        switchTabs()

        return rootView
    }

    override fun onResume() {
        super.onResume()
        token = requireArguments().getString("token")
        streakViewModel.getPickLeaderboardData(token!!)
        setToolbarColors(streak_toolbar)
        doSomeShit()
    }

    private fun doSomeShit(){

        /*val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.cornerRadii = floatArrayOf(60f, 60f, 0f, 0.0f, 0f, 0f, 60f, 60f)
        shape.setColor(resources.getColor(R.color.colorWhite))
        getSchoolColor()?.let { shape.setStroke(2, it) }
        pointsButton!!.background = shape
        //getSchoolColor()?.let { pointsButton!!.setTextColor(it) }

        val streakShape = GradientDrawable()
        streakShape.shape = GradientDrawable.RECTANGLE
        streakShape.cornerRadii = floatArrayOf(0f, 0f, 60f, 60f, 60f, 60f, 0f, 0f)
        getSchoolColor()?.let { streakShape.setColor(it) }
        getSchoolColor()?.let { streakShape.setStroke(2, it) }
        picksButton!!.background = streakShape
        picksButton!!.setTextColor(resources.getColor(R.color.colorWhite))*/

    }


    private fun setUpView(rootView: View?) {
        mSettings?.setOnClickListener {
            val myIntent = Intent(this.activity, SettingsActivity::class.java)
            myIntent.putExtra("token", token) //Optional parameters
            startActivity(myIntent)
        }
        setTotalPoints()

        prizeField = rootView?.findViewById(R.id.prize_field)
        prizeImage = rootView?.findViewById(R.id.prizeImageView)
        prizeTopTextView = rootView?.findViewById(R.id.prizeTopLabel)
        prizeBottomTextView = rootView?.findViewById(R.id.prizeDescLabel)
        prizesView = rootView?.findViewById(R.id.prizesView)


        mUser = GamePointApplication.shared!!.getCurrentUser(requireContext())

        if (mUser?.streaks_prize == null || mUser?.streaks_prize!!.title.isEmpty() || mUser?.streaks_prize!!.image.isEmpty()){
            prizeField?.visibility = View.GONE
        }else{
            prizeField?.visibility = View.VISIBLE
            prizeTopTextView?.text = mUser?.streaks_prize!!.title
            prizeBottomTextView?.text = mUser?.streaks_prize!!.description
            prizesView!!.setOnClickListener {
                goToPrizeUrl()
            }
            setPrizeImage()
        }





        mSwipeToRefresh?.setOnRefreshListener {
            //load items
            streakViewModel.getPickLeaderboardData(token!!)
        }


    }

    private fun setPrizeImage(){
        try {
            prizeImage?.visibility = View.INVISIBLE
            if (mUser?.streaks_prize!!.image.isNotEmpty()) {
                Picasso.get()
                        .load(mUser?.streaks_prize!!.image)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.trophy_variant)
                        .into(prizeImage, object : Callback {
                            override fun onSuccess() {
                                prizeImage?.visibility = View.VISIBLE
                                //there were images from the cache
                            }

                            override fun onError(e: Exception?) {
                                Log.e(TAG, "No cached images, lead from server instead $e")
                                if (mUser?.streaks_prize!!.image.isNotEmpty()) {
                                    Picasso.get()
                                            .load(mUser?.streaks_prize!!.image)
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
        val uri = Uri.parse(mUser?.streaks_prize?.prize_url) // missing 'http://' will cause crashed
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }


    private fun setTotalPoints() {
        disposables.add(repository?.points(Constants.TOTAL_POINTS_KEY)?.subscribe({ totalPoitns ->

            disposables.add(repository?.points(Constants.POINTS_THIS_CHECK_IN)?.subscribe({ newPts ->
                totalPts?.text = totalPoitns.toInt().plus(newPts.toInt()).toString()

            }, {
                totalPts?.text = totalPoitns

                Log.e("Error : ", "", it)
            })!!)
        }, {

            Log.e("Error : ", "", it)
        })!!)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PointsTouchedListener) {
            listener = context
        } else {
            throw ClassCastException(requireContext().toString() + " must implement StreakFragment.PointsTouchedListener")
        }
    }


    private fun switchTabs() {
        //left button selected
        pointsButton!!.setOnClickListener {
            //Set the button's appearance
            listener!!.onPointsSelected()
        }

    }


    interface PointsTouchedListener {
        fun onPointsSelected()
    }

    private fun responseFromStreaksViewModel() {
        //first, try to get data from the database
        streakViewModel.streaksFromDb.observe(viewLifecycleOwner, Observer { streaks ->
            Log.i(TAG, "streak from db picksLeaderBoardFromServer")
            displayData(streaks)
            picksRv?.visibility = View.VISIBLE
            mNoDataView?.visibility = View.INVISIBLE


            //display the deals
        })
        //if it fails, get data from server.
        streakViewModel.failedStreaksFromDb.observe(viewLifecycleOwner, Observer {
            mNoDataView?.visibility = View.INVISIBLE
            picksRv?.visibility = View.VISIBLE

            Log.e(TAG, "failed getting streaks: $it")
            try {
                streakViewModel.getPickLeaderboardData(token!!)
            } catch (e: NullPointerException) {
                Log.e(TAG, "could not fetch data. Token is null")
                //here we should log user out or try refreshing token
            }
        })
        // lets try to get you some server data
        streakViewModel.streaksFromServer.observe(viewLifecycleOwner, Observer { response ->
            Log.i(TAG, "here are the streaks data is: $response")
            mSwipeToRefresh!!.isRefreshing = false
            displayData(response)
            mNoDataView?.visibility = View.INVISIBLE
            picksRv?.visibility = View.VISIBLE

        })

        //server data failed
        streakViewModel.errorFetchingLeaderboardData.observe(viewLifecycleOwner, Observer { response ->
            Log.e(TAG, "problem fetching streaks from server: $response")
            mSwipeToRefresh!!.isRefreshing = false
            mNoDataView?.visibility = View.VISIBLE
            picksRv?.visibility = View.INVISIBLE

        })

        //could not save streak to db
        streakViewModel.errorSavingStreaks.observe(viewLifecycleOwner, Observer { it ->
            Log.e(TAG, "problem saving streaks to db: $it")

        })


    }

    private fun displayData(response: List<StreakModel>?) {
        try {
            if (response != null) {
                setupRecyclerview(response)
            } else {
                mNoDataView?.visibility = View.VISIBLE
            }
        } catch (e: NullPointerException) {
            Log.e(TAG, "streaks were null $e")
        }

    }


    private fun setupRecyclerview(response: List<StreakModel>) {

        val newArr = ArrayList<StreakModel>()
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
            //add 15 more
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
                val newModel = StreakModel(
                        name = f.name,
                        avatar = f.avatar,
                        total_points = f.total_points,
                        current_streak = f.current_streak,
                        highest_streak = f.highest_streak,
                        this_month_pick_points = f.this_month_pick_points,
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
                    val second = StreakModel(
                            name = response[indexOfUser - 2].name,
                            avatar = response[indexOfUser - 2].avatar,
                            total_points = response[indexOfUser - 2].total_points,
                            current_streak = response[indexOfUser - 2].current_streak,
                            highest_streak = response[indexOfUser - 2].highest_streak,
                            this_month_pick_points = response[indexOfUser - 2].this_month_pick_points,
                            rank = response[indexOfUser - 2].rank,
                            current_user = response[indexOfUser - 2].current_user,
                            position = indexOfUser - 2
                    )

                    val first =  StreakModel(
                            name = response[indexOfUser - 1].name,
                            avatar = response[indexOfUser - 1].avatar,
                            total_points = response[indexOfUser - 1].total_points,
                            current_streak = response[indexOfUser - 1].current_streak,
                            highest_streak = response[indexOfUser - 1].highest_streak,
                            this_month_pick_points = response[indexOfUser - 1].this_month_pick_points,
                            rank = response[indexOfUser - 1].rank,
                            current_user = response[indexOfUser - 1].current_user,
                            position = indexOfUser - 1
                    )
                    newArr.add(second)
                    newArr.add(first)
                }

                val last =  StreakModel(
                        name = response[response.lastIndex].name,
                        avatar = response[response.lastIndex].avatar,
                        total_points = response[response.lastIndex].total_points,
                        current_streak = response[response.lastIndex].current_streak,
                        highest_streak = response[response.lastIndex].highest_streak,
                        this_month_pick_points = response[response.lastIndex].this_month_pick_points,
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
                            val newModel = StreakModel(
                                    name = l.name,
                                    avatar = l.avatar,
                                    total_points = l.total_points,
                                    current_streak = l.current_streak,
                                    highest_streak = l.highest_streak,
                                    this_month_pick_points = l.this_month_pick_points,
                                    rank = l.rank,
                                    current_user = l.current_user,
                                    position = i
                            )

                            newArr.add(newModel)
//                            newArr.add(l)
//                            newArr.add(response[indexOfUser])
                        }
                    }
                }

            }
            //this new list should have the first 7 items and 7 more items after
            //user item for a total of 15 items on the list.
        }

//
//


        picksRv.apply {
            //set a linear layout manager
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
            adapter = StreakAdapter(newArr, getSchoolColorAsString())
        }
    }

    private fun onFetchDataError() {
        streakViewModel.errorFetchingLeaderboardData.observe(viewLifecycleOwner, Observer { response ->
            Log.e(TAG, response)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }


}// Required empty public constructor
