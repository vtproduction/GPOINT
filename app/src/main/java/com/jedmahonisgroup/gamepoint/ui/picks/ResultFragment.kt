package com.jedmahonisgroup.gamepoint.ui.picks


import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.BuildConfig
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.adapters.results.ResultsDayAdapter
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.UserResponse
import com.jedmahonisgroup.gamepoint.model.UserResponseModel
import com.jedmahonisgroup.gamepoint.model.picks.MyPicksModel
import com.jedmahonisgroup.gamepoint.ui.BaseFragment
import com.jedmahonisgroup.gamepoint.ui.settings.SettingsActivity
import com.jedmahonisgroup.gamepoint.utils.Constants
import com.jedmahonisgroup.gamepoint.utils.SharedPreferencesGamePointSharedPrefsRepo
import com.jedmahonisgroup.gamepoint.utils.StringFormatter
import com.jedmahonisgroup.gamepoint.utils.gamePointSharedPrefsRepo
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_conference.*
import kotlinx.android.synthetic.main.fragment_result.*
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * A simple [Fragment] subclass.
 *
 */
class ResultFragment : BaseFragment(), AdapterView.OnItemSelectedListener {

    private val TAG: String = ResultFragment::class.java.simpleName

    private var spinner: Spinner? = null
    private var listner: SwitchFragmentListener? = null
    private lateinit var resultsViewModel: ResultsViewModel
    private var binding: ViewDataBinding? = null

    private var mToken: String? = null
    private var mUser: UserResponse? = null

    //ui
    private var mUserName: TextView? = null
    private var mMonthlyTotal: TextView? = null
    private var mCurrentSreak: TextView? = null
    private var mRank: TextView? = null
    private var mProfileImage: CircleImageView? = null

    private var mSwipeToRefresh: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? = null
    private var mSettings: ImageButton? = null
    private var mPointCount: Button? = null
    private var augustFirstView: View? = null
    private var infoBar: View? = null

    private var mRecyclerView: androidx.recyclerview.widget.RecyclerView? = null
    private var mAdapter: ResultsDayAdapter? = null

    private var repository: gamePointSharedPrefsRepo? = null
    private val disposables = CompositeDisposable()
    private lateinit var pullToRefresh: SwipeRefreshLayout


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_result, container, false)
        val rootView = binding?.root
        super.onCreateView(inflater, container, savedInstanceState)


        resultsViewModel = ViewModelProviders.of(this, ViewModelFactory(this.activity as AppCompatActivity)).get(ResultsViewModel::class.java)
        repository = SharedPreferencesGamePointSharedPrefsRepo.create(requireActivity().baseContext)

        resultsViewModel.getDbResults()
        getData()
        if (!isAfterAugustFirst()) {
            setUpAugustFirstUI(rootView)
        }else {
            resultsFromServer()

            setUpUi(rootView)
        }

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
        getData()
        resultsViewModel.getPicksFromServer(mToken!!)
        setToolbarColors(results_toolbar)
    }

    private fun getData() {
        val token = requireArguments().getString("token")

        Log.i(TAG, "Token $token")

        mToken = token
        mUser = GamePointApplication.shared!!.getCurrentUser(requireContext())

        Log.e(TAG, "user  ==========> ${mUser.toString()}")

    }

    private fun setUpUi(rootView: View?) {
        mUserName = rootView!!.findViewById(R.id.userName)
        mMonthlyTotal = rootView.findViewById(R.id.monthlyTotal)
        mRank = rootView.findViewById(R.id.rank)
        mProfileImage = rootView.findViewById(R.id.infoBarUserImage)
        mCurrentSreak = rootView.findViewById(R.id.resultsCurrentStreak)

        mSwipeToRefresh = rootView.findViewById(R.id.results_swipe_refresh_layout)
        mSettings = rootView?.findViewById(R.id.resultsSettings)
        mRecyclerView = rootView?.findViewById(R.id.resultsRv)
        mPointCount = rootView?.findViewById(R.id.myResultsPointCount)

        mSettings?.setOnClickListener {
            val myIntent = Intent(this.activity, SettingsActivity::class.java)
            myIntent.putExtra("token", mToken) //Optional parameters
            startActivity(myIntent)
        }

        setTotalPoints()
//        disposables.add(repository?.points(Constants.TOTAL_POINTS_KEY)?.subscribe({
//            mPointCount?.text = it.toString()
//        }, {
//            mPointCount?.text = 0.toString()
//            Log.e("Error : ", "", it)
//        })!!)

        mSwipeToRefresh?.setOnRefreshListener {
            //load items
            resultsViewModel.getPicksFromServer(mToken!!)
        }

        userCard(rootView)
        setupSpinner(rootView)
    }

    private fun setUpAugustFirstUI(rootView: View?) {


        pullToRefresh = rootView!!.findViewById(R.id.results_swipe_refresh_layout)
        pullToRefresh.visibility = View.GONE

        infoBar = rootView?.findViewById(R.id.infoBar)
        infoBar?.visibility = View.GONE
        augustFirstView = rootView?.findViewById(R.id.checkBackAugustFirstPicks)
        augustFirstView?.visibility = View.VISIBLE
        mPointCount = rootView?.findViewById(R.id.myResultsPointCount)
        setTotalPoints()
        setupSpinner(rootView)
    }

        private fun setTotalPoints() {
        disposables.add(repository?.points(Constants.TOTAL_POINTS_KEY)?.subscribe({ totalPoitns ->

            disposables.add(repository?.points(Constants.POINTS_THIS_CHECK_IN)?.subscribe({ newPts ->
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
        Log.e("JMG", "mUser?.user?.first_name?: " + mUser)
        mUserName!!.text = "${mUser?.first_name?.capitalize()}" + " " + "${mUser?.last_name?.first()?.toUpperCase()}."
        mMonthlyTotal!!.text = if (mUser?.highest_streak != null) "Highest Streak: ${mUser?.highest_streak}" else "Highest Streak: 0"
        mCurrentSreak!!.text = if (mUser?.current_streak != null) "Current Streak: ${mUser?.current_streak}" else "Current Streak: 0"
        mRank!!.text = if (mUser?.pick_rank != null) "Rank:${mUser?.pick_rank}" else "Rank: 0"

        getProfilePic()

    }

    private fun getProfilePic() {
        try {
            if (!mUser?.avatar.isNullOrEmpty()) {
                Picasso.get()
                        .load(mUser?.avatar)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.ic_user_account)
                        .into(mProfileImage, object : Callback {
                            override fun onSuccess() {
                                //there were images from the cache
                            }

                            override fun onError(e: Exception?) {
                                Log.e(TAG, "No cached images, lead from server instead $e")
                                if (!mUser?.avatar.isNullOrEmpty()) {
                                    Picasso.get()
                                            .load(mUser?.avatar)
                                            .placeholder(R.drawable.ic_user_account)
                                            .into(mProfileImage, object : Callback {
                                                override fun onSuccess() {

                                                }

                                                override fun onError(e: Exception?) {
                                                    //there were images from the cache
                                                    try {
                                                        Log.e(TAG, " Picasso Could not fetch profile pic a second time from ${BuildConfig.BASE_URL + mUser!!.avatar}, stack trace,e $e")
                                                    } catch (e: Exception) {
                                                        Log.e(TAG, " Assume we are here because user.avatar is null")
                                                    }

                                                }

                                            })
                                }
                            }

                        })
            }
        } catch (e: java.lang.NullPointerException) {
            mProfileImage?.background = resources.getDrawable(R.drawable.ic_user_account)

        }

        try {
            Log.e(TAG, "profile img url ${BuildConfig.BASE_URL + mUser!!.avatar}")
        } catch (e: Exception) {
            Log.e(TAG, "profile img url hit the catch block cuz something is null")
        }
    }

    private fun resultsFromServer() {
        //first, try to get data from the database
        resultsViewModel.resultsFromDb.observe(viewLifecycleOwner, androidx.lifecycle.Observer { picks ->
            Log.i(TAG, "results from db resultsFromServer")
            displayData(picks)
            resultsRv?.visibility = View.VISIBLE
            myResultsNoDataView?.visibility = View.INVISIBLE
            mDataEmptyView?.visibility = View.INVISIBLE
        })

        resultsViewModel.failedResultsFromDb.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            myResultsNoDataView?.visibility = View.INVISIBLE
            mDataEmptyView?.visibility = View.INVISIBLE
            resultsRv?.visibility = View.VISIBLE

            Log.e(TAG, "failed getting results: $it")
            try {
                resultsViewModel.getPicksFromServer(mToken!!)
            } catch (e: NullPointerException) {
                Log.e(TAG, "could not fetch user results. Token is null")
                //here we should log user out or try refreshing token
            }
        })

        // lets observe server data response
        resultsViewModel.resultsFromServer.observe(viewLifecycleOwner, androidx.lifecycle.Observer { picks ->
            Log.i(TAG, "here are the results data is: $picks")
            mSwipeToRefresh!!.isRefreshing = false
            displayData(picks)
            myResultsNoDataView?.visibility = View.INVISIBLE
            mDataEmptyView?.visibility = View.INVISIBLE
            resultsRv?.visibility = View.VISIBLE
        })

        resultsViewModel.resultsFromServerFailed.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            Log.e(TAG, "problem fetching results from server: $it")
            mSwipeToRefresh!!.isRefreshing = false
            myResultsNoDataView?.visibility = View.VISIBLE
            mDataEmptyView?.visibility = View.INVISIBLE
            resultsRv?.visibility = View.INVISIBLE
        })

        resultsViewModel.errorSavingResults.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            Log.e(TAG, "problem saving results to db: $it")

        })


    }

    private fun displayData(picks: List<MyPicksModel>?) {
        try {
            if (picks == null) {
                //display the server screen
                myResultsNoDataView?.visibility = View.VISIBLE
                mDataEmptyView?.visibility = View.INVISIBLE
            }

            if (picks!!.isEmpty()) {
                //display the empty screen
                myResultsNoDataView?.visibility = View.INVISIBLE
                mDataEmptyView?.visibility = View.VISIBLE

            }

            setupRecyclerview(picks)

        } catch (e: NullPointerException) {
            Log.e(TAG, "picks were null $e")
            //display null
        }
    }

    private fun setupRecyclerview(response: List<MyPicksModel>) {
        val timeStamps = ArrayList<String>()
        val dayList = ArrayList<Int>()

        for (event in response) {
            val startTime = StringFormatter.convertTimestampToDate(event.start_time)
            timeStamps.add(startTime)
        }

        //get the unique time stamps. this will be the different days.
        val uniqueTimeStamps: Set<String> = HashSet<String>(timeStamps)
        Log.e(TAG, "uniqueTimeStamps : $uniqueTimeStamps")

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
        mAdapter = ResultsDayAdapter()
        mRecyclerView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.activity)
        mRecyclerView?.adapter = mAdapter
        mAdapter!!.refreshList(sortedArray, response)

//        resultsRv.apply {
//            layoutManager = LinearLayoutManager(activity)
//            adapter = ResultsDayAdapter(response)
//        }
    }

    private fun setupSpinner(view: View?) {
        spinner = view?.findViewById(R.id.spinner)

        val list = ArrayList<String>()
        list.add("Results")
        list.add("Big Ten Picks")

        val adapter = ArrayAdapter(activity?.baseContext!!,
                R.layout.view_spinner_item, list)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.adapter = adapter
        spinner?.onItemSelectedListener = this
    }

    // Store the listener (activity) that will have events fired once the fragment is attached
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SwitchFragmentListener) {
            listner = context
        } else {
            throw ClassCastException(requireContext().toString() + " must implement ResultFragment.SwitchFragmentListener")
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent?.getItemAtPosition(position).toString() == "Big Ten Picks") {
            listner?.onBigTenPicksClicked()
        }
    }

    interface SwitchFragmentListener {
        fun onBigTenPicksClicked()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }


}
