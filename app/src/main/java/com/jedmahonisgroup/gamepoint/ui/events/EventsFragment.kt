package com.jedmahonisgroup.gamepoint.ui.events


import android.annotation.SuppressLint
import android.app.Activity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import android.os.Bundle
import android.provider.Settings
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.GamePointResultListener
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.adapters.DayAdapter
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.CheckinsResponseModel
import com.jedmahonisgroup.gamepoint.model.events.EventsModel
import com.jedmahonisgroup.gamepoint.model.events.PreviousCheckedInEvent
import com.jedmahonisgroup.gamepoint.ui.BaseFragment
import com.jedmahonisgroup.gamepoint.ui.MainActivity
import com.jedmahonisgroup.gamepoint.ui.settings.SettingsActivity
import com.jedmahonisgroup.gamepoint.utils.*
import com.jedmahonisgroup.gamepoint.utils.Constants.PERCENT_KEY
import com.jedmahonisgroup.gamepoint.utils.Constants.POINTS_KEY
import com.jedmahonisgroup.gamepoint.utils.Constants.POINTS_THIS_CHECK_IN
import com.jedmahonisgroup.gamepoint.utils.Constants.PREVIOUS_MINUETS_KEY
import com.jedmahonisgroup.gamepoint.utils.Constants.TOTAL_POINTS_KEY
import com.jedmahonisgroup.gamepoint.utils.StringFormatter.convertTimestampToDate
import com.jedmahonisgroup.gamepoint.utils.StringFormatter.getDay
import com.jedmahonisgroup.gamepoint.utils.StringFormatter.getDayOfYear
import com.jedmahonisgroup.gamepoint.utils.StringFormatter.getMonth
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.above_18_alert.view.*
import kotlinx.android.synthetic.main.fragment_events.*
import org.json.JSONObject


/**
 * A simple [Fragment] subclass.
 *
 */
class EventsFragment : BaseFragment(), GamePointResultListener, OnMapReadyCallback {


    private var mSos: Boolean? = false
    private var TAG: String = EventsFragment::class.java.simpleName
    private var TAG2: String = "NIEN__EVENTFRAG"
    private var binding: ViewDataBinding? = null
    private lateinit var eventViewModel: EventViewModel
    private var adapter: DayAdapter? = null
    private var mToken: String? = null

    private val PREFS_FILENAME = "com.jedmahonisgroup.gamepoint"
//    private lateinit var sharedPreferences: SharedPreferences
    private val CHECKED_IN: String = "CHECKED_IN"
    private var isCheckedIn: Boolean = false

    //UI
    private var checkOutBtn: Button? = null
    private var checkInBtn: Button? = null
    private var checkedinUi: RelativeLayout? = null
    private var eventName: TextView? = null
    private var eventPoint: TextView? = null
    private var address: TextView? = null
    private var eventTime: TextView? = null
    private var mPoints: TextView? = null
    private var mEmptyView: RelativeLayout? = null
    private var mEmptyTextView: TextView? = null
    private var mNullView: RelativeLayout? = null
    private var userPoints: Button? = null
    private var mSwipeToRefresh: SwipeRefreshLayout? = null
    private var mSettings: ImageButton? = null
    private var mTopToolbar: Toolbar? = null
    private var contentLayout: View? = null
    private var circularProgress: CircularProgressIndicator? = null

    private var mData: EventsModel? = null
    private val REQUEST_CODE = 100

    private var recyclerView: androidx.recyclerview.widget.RecyclerView? = null
    private var listener: EventsFragmentListener? = null
    private var rootView: View? = null


    private var repository: gamePointSharedPrefsRepo? = null
    private val disposables = CompositeDisposable()

    var tracker: String = "0"
    private var token: String? = null

    private val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {

            when (intent?.action) {
                "GEOFENCE_TRANSITION_EXIT" -> checkUserOut()
            }
        }
    }

    private val eventOverBroadcaseReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {

            when (intent?.action) {
                "EVENT_OVER" -> checkUserOut()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView2) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment'
        if (binding?.root == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_events, container, false)
//            val rootView = binding?.root

            rootView = binding?.root


            //Important
            eventViewModel = ViewModelProviders.of(this, ViewModelFactory(this.activity as AppCompatActivity)).get(EventViewModel::class.java)
            repository = SharedPreferencesGamePointSharedPrefsRepo.create(requireActivity().baseContext)
            sharedPreferences = requireActivity().getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

            eventViewModel.getEventsFromDb()

            responseFromServer()
        }

        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(requireActivity().baseContext).registerReceiver(checkUserOut(), IntentFilter("GEOFENCE_TRANSITION_EXIT"))
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(requireActivity().baseContext).registerReceiver(checkUserOut(), IntentFilter("EVENT_OVER"))
        return binding?.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check which request we're responding to
        Log.e(TAG, "onActivityResult")
        if (requestCode == REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {

                if (data != null) {
                    val MBuddle = data.extras
                    val userString = MBuddle?.getString("data")
                    val checkInResposne = MBuddle?.getString("checkin_response")
                    // The user picked a contact.
                    // The Intent's data Uri identifies which contact was selected.

                    val editor = sharedPreferences.edit()
                    editor.putString("event_data", userString)
                    editor.putString("checkin_response", checkInResposne)
                    Log.i(TAG, "onActivityResult saving data to shared pref")
                    editor.apply()
                    isCheckedIn = sharedPreferences.getBoolean(CHECKED_IN, false)
                    if (isCheckedIn) {
                        Log.e(TAG, "is checked in onActivityResult")
                        showCheckedInUI()
                    } else {
                        hideCheckedInUI()
                    }
                    // Do something with the contact here (bigger example below)
                    //val mString = data?.getStringExtra("event_data")

                    Log.e(TAG, "======= fragment result ${userString.toString()}")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mSos = sharedPreferences.getBoolean("SOS", false)

        setUpUI(rootView)

        Log.e(TAG, "===========> Freagment onResume")
        Log.e(TAG, "===========> onResume mDatamData = ${mData}")
        //autoCheckOut()
        //  checkedInUi(mData!!)
        getData()
        eventViewModel.getEventsData(token.toString())
        if (mSos!!) {
            //we are in crisis mode
            //hide the error no data
            mNullView?.visibility = View.GONE
            mEmptyView?.visibility = View.GONE
            showCheckedInUI()
            Log.e(TAG, "crisis mode")


        }

    }

    private fun checkUserOut(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.e(TAG, "fragment user exit geofence listener")
                checkOutLogic()
            }
        }
    }

    private fun getData() {
        token = requireArguments().getString("token")
        LogUtil.d("EventsFragment > getData > 245: $token")
        mToken = token
        //save the token to the sp so when the app is in the bg we can use it to check out
        val editor = sharedPreferences.edit()
        editor.putString("TOKEN", token)
        editor.apply()
    }

    private fun setUpUI(rootView: View?) {
        recyclerView = rootView!!.findViewById(R.id.dayRv)
        checkOutBtn = rootView.findViewById(R.id.checkoutBtn)
        contentLayout = rootView.findViewById(R.id.contentLayout)
        checkInBtn = rootView.findViewById(R.id.checkinBtn)
        checkedinUi = rootView.findViewById(R.id.checkedinUi)
        eventName = rootView.findViewById(R.id.event_Name)
        eventTime = rootView.findViewById(R.id.eventTime)
        address = rootView.findViewById(R.id.address)
        mPoints = rootView.findViewById(R.id.points)
        userPoints = rootView.findViewById(R.id.userPoints)
        eventPoint = rootView.findViewById(R.id.txtPoint)
        circularProgress = rootView.findViewById(R.id.circular_progress)
        mEmptyView = rootView.findViewById(R.id.eventEmptyView)
        mEmptyTextView = rootView.findViewById(R.id.errorEventText)
        mNullView = rootView.findViewById(R.id.eventNullView)
        mSwipeToRefresh = rootView.findViewById(R.id.swipeContainer)
        mSettings = rootView.findViewById(R.id.eventSettings)
        mTopToolbar = rootView.findViewById(R.id.eventsTopToolbar)
        mTopToolbar!!.setBackgroundColor(Color.parseColor(sharedPreferences.getString(Constants.PRIMARY_COLOR, R.color.colorPrimary.toString())))
        mSettings!!.setOnClickListener {
            val myIntent = Intent(this.activity, SettingsActivity::class.java)
            myIntent.putExtra("token", mToken) //Optional parameters
            startActivity(myIntent)
        }
        ///here boy

        /*eventName!!.setTextColor(Color.parseColor(getSchoolColorAsString()))
        points.setTextColor(Color.parseColor(getSchoolColorAsString()))
        circularProgress!!.progressColor= Color.parseColor(getSchoolColorAsString())
        circularProgress!!.textColor= Color.parseColor(getSchoolColorAsString())*/



        disposables.add(repository?.points(TOTAL_POINTS_KEY)?.subscribe({ totalPoitns ->

            disposables.add(repository?.points(POINTS_KEY)?.subscribe({ newPts ->
                tracker = totalPoitns.toInt().plus(newPts.toInt()).toString()

            }, {

                userPoints?.text = totalPoitns
                Log.e("Error : ", "", it)
            })!!)
            disposables.add(repository?.points(POINTS_THIS_CHECK_IN)?.subscribe({ newPts ->
                userPoints?.text = totalPoitns.toInt().plus(newPts.toInt()).toString()

            }, {

                userPoints?.text = totalPoitns
                Log.e("Error : ", "", it)
            })!!)
        }, {

            Log.e("Error : ", "", it)
        })!!)



        isCheckedIn = sharedPreferences.getBoolean(CHECKED_IN, false)
        Log.i(TAG, "isCheckedIn ============> $isCheckedIn")

        mSwipeToRefresh?.setOnRefreshListener {
            try {
                eventViewModel.getEventsData(token.toString())

            } catch (e: java.lang.NullPointerException) {
                mNullView?.visibility = View.VISIBLE
                mEmptyView?.visibility = View.GONE
                Log.e(TAG, "token was null when swiping to refresh")

            }
        }
        Log.e(TAG, "setUpUI isCheckedIn: " + isCheckedIn)
        if (isCheckedIn) {
            Log.e(TAG, "isCheckedIn onResume")
            showCheckedInUI()

        } else {
            hideCheckedInUI()
        }

        checkOutBtn!!.setOnClickListener {
            //save running total
            //saveNewTotalPts(tracker)
            if (mSos == true){
                Snackbar.make(this.binding!!.root,"Error, connect to the internet and try again", Snackbar.LENGTH_LONG).show()

            }else{
                checkOutLogic()

            }
        }
        checkInBtn?.setOnClickListener {
            it.visibility = View.GONE
            contentLayout?.visibility = View.VISIBLE
            checkedInUi()
        }

    }

    private fun dd() {

        val title = "Internet connection error"
        val msg = "Make sure you are connected to the internet and try again. "

        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(activity).inflate(R.layout.above_18_alert, null)
        //AlertDialogBuilder
        val mBuilder = this.let { AlertDialog.Builder(requireActivity().baseContext).setView(mDialogView) }
        //show dialog
        val mAlertDialog = mBuilder?.show()
        mAlertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val mTitle: TextView = mAlertDialog?.findViewById(R.id.required)!!
        val mInfo: TextView = mAlertDialog.findViewById(R.id.aboveEighteenText)!!
        val mPostBtn: TextView = mAlertDialog.findViewById(R.id.okBtn)!!
        val mNegBtn: TextView = mAlertDialog.findViewById(R.id.termsOfServiceBtn)!!

        mTitle.text = title
        mInfo.text = msg
        mPostBtn.text = "Ok"
        // mPostBtn.alig

        //login button cick of custom layout
        mDialogView.okBtn.setOnClickListener {
            mAlertDialog.dismiss()


        }

        mDialogView.termsOfServiceBtn.visibility = View.GONE
    }


    private fun internetConnectionAlert() {
        val mDialogView = LayoutInflater.from(activity).inflate(R.layout.above_18_alert, null)
        val mBuilder = activity?.let { it1 ->
            AlertDialog.Builder(it1)
                    .setView(mDialogView)
        }

        val title = "Internet connection error"
        val msg = "Make sure you are connected to the internet and try again. "


        val mAlertDialog = mBuilder?.show()

        val mTitle: TextView = mAlertDialog?.findViewById(R.id.required)!!
        val mInfo: TextView = mAlertDialog.findViewById(R.id.aboveEighteenText)!!
        val mPostBtn: TextView = mAlertDialog.findViewById(R.id.okBtn)!!
        val mNegBtn: TextView = mAlertDialog.findViewById(R.id.termsOfServiceBtn)!!

        mTitle.text = title
        mInfo.text = msg
        mPostBtn.text = "Ok"
        // mPostBtn.alig

        //login button cick of custom layout
        mDialogView.okBtn.setOnClickListener {
            mAlertDialog.dismiss()


        }

        mDialogView.termsOfServiceBtn.visibility = View.GONE
    }


    private fun saveNewTotalPts(pts: String) {
        disposables.add(repository!!.savePoints(TOTAL_POINTS_KEY, pts).subscribe())

    }

    private fun showCheckedInUI() {
        checkedinUi!!.visibility = View.VISIBLE
        dayRv?.visibility = View.GONE
        recyclerView?.visibility = View.GONE
        mSwipeToRefresh?.visibility = View.GONE
        eventNullView?.visibility = View.GONE
        eventEmptyView?.visibility = View.GONE
        //addMarker()


        //checkedInUi()
    }

    private fun hideCheckedInUI() {
        checkedinUi!!.visibility = View.GONE
        recyclerView?.visibility = View.VISIBLE
        dayRv?.visibility = View.VISIBLE
        responseFromServer()
    }

    private fun responseFromServer() {
        //first, try to get data from the database
        Log.e("JMG", "responseFromServer")
        eventViewModel.eventsFromDb.observe(viewLifecycleOwner, Observer { events ->
            if (events != null) {
                Log.i(TAG, "picks from db resultsFromServer")
                events.sortedBy { it.startTime }
                displayData(events)
                dayRv?.visibility = View.VISIBLE
                mNullView?.visibility = View.GONE
                mEmptyView?.visibility = View.GONE
//            myDataEmptyView?.visibility = View.INVISIBLE
            } else {
                //events were null
                mNullView?.visibility = View.GONE
                dayRv?.visibility = View.GONE
                mEmptyView?.visibility = View.VISIBLE
                try {
                    eventViewModel.getEventsData(token.toString())
                } catch (e: NullPointerException) {
                    Log.e(TAG, "could not fetch events. Token is null")
                    mNullView?.visibility = View.GONE

                    //here we should log user out or try refreshing token
                }
            }

        })

        eventViewModel.failedEventsFromDb.observe(viewLifecycleOwner, Observer {
            //            myPicksRLNoData?.visibility = View.INVISIBLE
//            myDataEmptyView?.visibility = View.INVISIBLE

            dayRv?.visibility = View.VISIBLE

            Log.e(TAG, "failed getting events: $it")
            try {
                eventViewModel.getEventsData(token.toString())
            } catch (e: NullPointerException) {
                Log.e(TAG, "could not fetch events. Token is null")
                mNullView?.visibility = View.GONE

                //here we should log user out or try refreshing token
            }
        })

        // lets observe server data response
        eventViewModel.eventsFromServer.observe(viewLifecycleOwner, Observer { events ->
            if (events != null) {
                Log.i(TAG, "here are the picks data is: $events")
                mSwipeToRefresh!!.isRefreshing = false
                dayRv?.visibility = View.VISIBLE
                mNullView?.visibility = View.GONE
                mEmptyView?.visibility = View.GONE
                events.sortedBy { it.startTime }
                displayData(events)
                //myDataEmptyView?.visibility = View.INVISIBLE

            } else {
                Log.e("eventsss", "Are bulll")
                //picks are null
                if (mSos!!){

                }else{
                    dayRv?.visibility = View.GONE
                    mNullView?.visibility = View.VISIBLE
                    mEmptyView?.visibility = View.GONE
                    mEmptyTextView?.text = "There was a problem communicating with the server. Please Try Again."
                }

            }


        })

        eventViewModel.errorEventsFromServer.observe(viewLifecycleOwner, Observer { response ->
            Log.e(TAG, "problem fetching events from server: $response")

            if (mSos!!){
                mSwipeToRefresh!!.isRefreshing = false
                mEmptyView?.visibility = View.GONE
                dayRv?.visibility = View.GONE
                mEmptyTextView?.text = response
            }else{
                mSwipeToRefresh!!.isRefreshing = false

                mEmptyView?.visibility = View.GONE
                dayRv?.visibility = View.GONE
                mNullView?.visibility = View.VISIBLE
                mEmptyTextView?.text = response
            }

        })

        //could not save streak to db
        eventViewModel.errorSavingEvents.observe(viewLifecycleOwner, Observer {
            Log.e(TAG, "problem saving picks to db: $it")

        })


        /**
         * checkout listeners
         *
         */
        eventViewModel.checkoutSuccess.observe(viewLifecycleOwner, Observer { checkoutResponse ->
            //save a new user response model
            listener!!.onRefreshUserInDatabase(checkoutResponse!!.user_id)
        })

    }

    private fun displayData(events: List<EventsModel>) {
        try {
            if (!mSos!!) {

                if (events.isEmpty()) {
                    //display the empty screen

                    dayRv?.visibility = View.GONE
                    mNullView?.visibility = View.GONE
                    mEmptyView?.visibility = View.VISIBLE
//                    Log.e(TAG, "events were empty: " + mEmptyView?.visibility)

                } else if (!events.isNullOrEmpty()) {
//                    Log.e(TAG, "events were neither null nor empty")
                    mNullView?.visibility = View.GONE
                    mEmptyView?.visibility = View.GONE
                    dayRv?.visibility = View.VISIBLE
                    setupRecyclerview(events)
//                if (isCheckedIn) {
//                    checkedinUi!!.visibility = View.VISIBLE
//                }
                }

            }else{
                mNullView?.visibility = View.GONE
                mEmptyView?.visibility = View.GONE
            }
        } catch (e: NullPointerException) {
            Log.e(TAG, "events were null $e")
            //display null
            val sos  = sharedPreferences.getBoolean("SOS", false)

            if (sos){
                mNullView?.visibility = View.GONE
                mEmptyView?.visibility = View.GONE


            }else{
                mNullView?.visibility = View.VISIBLE
                mEmptyView?.visibility = View.GONE
                mEmptyTextView?.text = e.localizedMessage
            }


        }
    }

    private fun checkOutLogic() {
        var response = sharedPreferences.getString("checkin_response", "")
        val gson = Gson()

        val data = gson.fromJson(response, CheckinsResponseModel::class.java)

        Log.e(TAG, "checkOut response $data ")
        // saveMinutesFromCheckIn()


        //call checkout
        if (!data.equals(null)) {
            eventViewModel.postCheckOut(mToken, data.id.toString())
        }

        //could not save streak to db
        view?.post {
            eventViewModel.errorCheckout.observe(viewLifecycleOwner, Observer {
                val dialog = context?.let { it1 -> AlertDialog.Builder(it1) }
                dialog!!.setTitle("Error")
                    .setMessage(it)
                    .setPositiveButton("Retry") { paramDialogInterface, paramInt ->
                        checkOutLogic()
                    }
                    .setNegativeButton("Cancel") { paramDialogInterface, paramInt ->
                    }
                dialog.show()

            })
        }


        /**
         * checkout listeners
         *
         */
        view?.post {
            eventViewModel.checkoutSuccess.observe(viewLifecycleOwner, Observer { checkoutResponse ->
                //save a new user response model
                val editor = sharedPreferences.edit()
                isCheckedIn = false
                editor.putBoolean(CHECKED_IN, false)
                editor.putString(PERCENT_KEY, "0")
                editor.putString(POINTS_KEY, "0")
                editor.putString(POINTS_THIS_CHECK_IN, "0")
                editor.putBoolean("SOS", false)
                editor.apply()

                hideCheckedInUI()

                listener!!.onRefreshUserInDatabase(checkoutResponse!!.user_id)
                listener?.onCheckOut()

            })
        }


        //  handler.removeCallbacks(runnableCode)

    }

    private fun saveMinutesFromCheckIn() {
        val gson = Gson()
        var event = sharedPreferences.getString("event_data", "")
        val data = gson.fromJson(event, EventsModel::class.java)
        var checkedInEventsArray = ArrayList<PreviousCheckedInEvent>()
        var obj: JSONObject
        try {
            obj = gson.fromJson(sharedPreferences.getString("previousCheckedInEvents", ""), JSONObject::class.java)
            checkedInEventsArray = obj.get("array") as ArrayList<PreviousCheckedInEvent>

        } catch (e: Exception) {
            obj = JSONObject()
        }
        var minutes = ((sharedPreferences.getString(PERCENT_KEY, "")?.toDouble() ?: 0.0) / 100.0) * data.minutesToRedeem
        var points = ((sharedPreferences.getString(PERCENT_KEY, "")?.toDouble() ?: 0.0) / 100.0) * data.pointValue
        var pcie = PreviousCheckedInEvent(data.id, points.toString(), minutes.toInt())
        var j = -1
        for (i in 0..checkedInEventsArray.size) {
            val previousCheckedInEvent = checkedInEventsArray.get(i)
            if (previousCheckedInEvent.id == data.id) {
                j = i
                break
            }
        }



        if (j > -1) {

            checkedInEventsArray.set(j, pcie)
        } else {
            checkedInEventsArray.add(pcie)
        }
        obj.put("array", checkedInEventsArray)
        val editor = sharedPreferences.edit()
        editor.putString("previousCheckedInEvents", gson.toJson(obj)).apply()
    }

    private fun addMarker(){

        var user = sharedPreferences.getString("event_data", "")
        if (user.isNullOrEmpty()) return
        val gson = Gson()
        val data = gson.fromJson(user, EventsModel::class.java)

        map.addMarker(
            MarkerOptions()
                .position(LatLng(data.venue.latitude, data.venue.longitude))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_map)))
        map.addCircle(CircleOptions()
            .center(LatLng(data.venue.latitude, data.venue.longitude))
            .radius(100.0)
            .strokeWidth(0.0f)
            .fillColor(ContextCompat.getColor(requireActivity(), R.color.mapCircleColor)))
    }

    @SuppressLint("SetTextI18n")
    private fun checkedInUi() {

        var user = sharedPreferences.getString("event_data", "")
        var response = sharedPreferences.getString("checkin_response", "")


        Log.e(TAG, "=============> ${user}")
        Log.e(TAG, "checkin_response is =============> ${response}")

        val gson = Gson()
        val data = gson.fromJson(user, EventsModel::class.java)
        val checkInResponse = gson.fromJson(response, CheckinsResponseModel::class.java)
        checkForPreviousMinutes(data)
        circularProgress!!.maxProgress = 100.0
        circularProgress!!.setProgressTextAdapter(TIME_TEXT_ADAPTER)

        listener?.onCheckedIn(data.minutesToRedeem, checkInResponse.created_at, data.pointValue, data.startTime, data.id)
        readFromSP(mPoints, data.pointValue.toString())
        if (data != null) {
            val month = getMonth(data.startTime)
            val day = getDay(data.startTime)
            val time = StringFormatter.getTime(data.startTime)

            eventPoint?.text = data.pointValue.toString()
            eventName?.text = data.name
            eventTime?.text = "${data.sport!!.name} $month \u2022 ${day}th $time"
            address?.text = "${data.venue.address.street}, ${data.venue.address.city}, " + "${data.venue.address.state.abbrev}, " + "${data.venue.address.zipcode}"
            Log.i(TAG, "===========> mData = ${data}")

        } else {
            Log.e(TAG, "===========> mData = ${data}")
        }



        ///////MAP
        Log.e("NIEN-TAG", "MAP INIT: ${::map.isInitialized}")
        if (!::map.isInitialized) return
        map.run {
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isMapToolbarEnabled = false
        }

        /*if (mMarker != null) {
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(data.venue.latitude, data.venue.longitude))
                    //.title("Hello world")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_map))
            )
        } else {
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(data.venue.latitude, data.venue.longitude))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_map))
                //.title("Hello world")
            )
        }*/
        val zoom: Float = 14.526846.toFloat()
        val location = LatLng(data.venue.latitude, data.venue.longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoom))
    }

    private lateinit var map: GoogleMap
    private var mMarker: Bitmap? = null
    private fun initMap(googleMap: GoogleMap?){
        map = googleMap!!
        map.uiSettings?.isMapToolbarEnabled = false
        addMarker()
    }

    override fun onMapReady(p0: GoogleMap?) {
        Log.d("NIEN-TAG", "onMapReady")
        initMap(p0)
    }

    private fun checkForPreviousMinutes(event: EventsModel) {
        val gson = Gson()


        var checkedInEventsArray = ArrayList<PreviousCheckedInEvent>()
        var obj: JSONObject
        try {
            obj = gson.fromJson(sharedPreferences.getString("previousCheckedInEvents", ""), JSONObject::class.java)
            checkedInEventsArray = obj.get("array") as ArrayList<PreviousCheckedInEvent>

        } catch (e: Exception) {
            obj = JSONObject()
        }
        if (checkedInEventsArray.size > 0) {
            for (i in 0..checkedInEventsArray.size) {
                val previousCheckedInEvent = checkedInEventsArray.get(i)
                if (previousCheckedInEvent.id == event.id) {
                    val editor = sharedPreferences.edit()
                    editor.putInt(PREVIOUS_MINUETS_KEY, previousCheckedInEvent.minutes_checked_in).apply()
                    break
                }
            }
        }

    }

    private fun autoCheckOut() {
        if (isCheckedIn) {
            Log.e(TAG, "isCheckedIn: " + isCheckedIn)
            //if user is checked in and the event is over
//            val now = StringFormatter.getFormatedTimeStamp(StringFormatter.getDate()!!)
//            Log.e(TAG, "current time is $now")
//            Log.e(TAG, "mData?.start_time!!: " + mData?.start_time!!)
//            Log.e(TAG, "math: " + StringFormatter.getFormatedTimeStamp(mData?.start_time!!).plusMinutes(mData!!.minutes_to_redeem))
//            val eventEndTime = StringFormatter.getFormatedTimeStamp(mData?.start_time!!).plusMinutes(mData!!.minutes_to_redeem)
//            Log.e(TAG, "event ends at $eventEndTime")
//            if (now.isAfter(eventEndTime)) {
            //event is over right now. check user out.
            checkOutLogic()
//            }
        }
    }

    private fun readFromSP(textView: TextView?, totalPoints: String) {


        if (isCheckedIn) {
            Log.e(TAG, "read from sp not c")

            var points: String
            var percent: String

            disposables.add(repository?.points(POINTS_KEY)?.subscribe({
                textView?.text = "$it/$totalPoints"
            }, {

                Log.e("Error : ", "", it)
            })!!)


            disposables.add(repository?.percent(PERCENT_KEY)?.subscribe({
                // textView?.text =  "$it/$totalPoints"
                Log.e(TAG, "there is a problem here")
                circularProgress?.setCurrentProgress(it.toDouble())


                Log.e(TAG, "it.toDouble(): " + it.toDouble())
                if (it.toDouble() >= 100.0) {
                    Log.e(TAG, "is 100.0")
                    autoCheckOut()
                }

            }, {
                Log.e("Error : ", "", it)
            })!!)
        }
    }

    private fun setupRecyclerview(response: List<EventsModel>) {
        val timeStamps = ArrayList<String>()
        val dayList = ArrayList<Int>()


        for (event in response) {
            val startTime = convertTimestampToDate(event.startTime)

            timeStamps.add(startTime)
        }

        //get the unique time stamps. this will be the different days.
        val uniqueTimeStamps: Set<String> = HashSet<String>(timeStamps)
        Log.e(TAG, "uniqueTimeStamps : $uniqueTimeStamps")

        //create a list of day events
        for (unique in uniqueTimeStamps) {
            dayList.add(getDayOfYear(unique))
        }
        Log.i(TAG, "unique days : $dayList")


        //sort the dayList in accending order
        val original: Array<Int> = dayList.toList().toTypedArray()
        val sortedArray: Array<Int> = original.sortedArray()
//        Log.e(TAG, "original : $original")

        //set up recyclerview
        adapter = DayAdapter(this@EventsFragment, getSchoolColorAsString(),getSchoolSecondaryColorAsString())
        recyclerView?.layoutManager = LinearLayoutManager(this.activity)
        recyclerView?.adapter = adapter
        adapter!!.refreshList(sortedArray, response)
    }


    /**
     * OnClickListner from recyclerview adapter
     *
     * */
    override fun onCardClicked(data: EventsModel) {
        //start activity for result
        //this was causeing a IllegalStateException so I changed it to gson
      val gson = Gson()
        val eventDataStr = gson.toJson(data)

        val myIntent = Intent(this.activity, EventDetailActivity::class.java)
        myIntent.putExtra("event", eventDataStr) //Optional parameters
        startActivityForResult(myIntent, REQUEST_CODE)
        Log.i(TAG, "==========> card clicked")





    }

    // Store the listener (activity) that will have events fired once the fragment is attached
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EventsFragment.EventsFragmentListener) {
            listener = context
        } else {
            throw ClassCastException(context.toString() + " must implement ResultFragment.SwitchFragmentListener")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(requireActivity().baseContext)
                .unregisterReceiver(broadCastReceiver)
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(requireActivity().baseContext).unregisterReceiver(eventOverBroadcaseReceiver)

    }

    interface EventsFragmentListener {
        fun onCheckedIn(minutes_to_redeem: Int, created_at: String, point_value: Int, start_time: String, id: Int)
        fun onCheckOut()
        fun onRefreshUserInDatabase(userId: Int)
    }

    private val TIME_TEXT_ADAPTER = CircularProgressIndicator.ProgressTextAdapter { time ->
        val sb = StringBuilder()

        sb.append(time.toInt()).append("%")

        sb.toString()
    }


}
