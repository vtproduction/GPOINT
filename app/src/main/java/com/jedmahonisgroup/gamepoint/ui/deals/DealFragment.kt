package com.jedmahonisgroup.gamepoint.ui.deals


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jedmahonisgroup.gamepoint.GamePointDealDetailListener
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.adapters.deal.DealAdapter
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.Business
import com.jedmahonisgroup.gamepoint.model.CurrentRedeemingDealModel
import com.jedmahonisgroup.gamepoint.model.DealModel
import com.jedmahonisgroup.gamepoint.model.DealsUi
import com.jedmahonisgroup.gamepoint.model.events.Address
import com.jedmahonisgroup.gamepoint.model.events.State
import com.jedmahonisgroup.gamepoint.ui.BaseFragment
import com.jedmahonisgroup.gamepoint.ui.settings.SettingsActivity
import com.jedmahonisgroup.gamepoint.utils.Constants
import com.jedmahonisgroup.gamepoint.utils.SharedPreferencesGamePointSharedPrefsRepo
import com.jedmahonisgroup.gamepoint.utils.StringFormatter
import com.jedmahonisgroup.gamepoint.utils.gamePointSharedPrefsRepo
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_redeem.*
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat

/**
 * A simple [Fragment] subclass.
 */
class DealFragment : BaseFragment(), GamePointDealDetailListener {

    private var userLat: Double? = null
    private var userLng: Double? = null

    private var listener: RedeemListener? = null
    private var TAG: String = DealFragment::class.java.simpleName
    private var binding: ViewDataBinding? = null

    private var mSwipeToRefresh: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? = null

    private lateinit var dealsViewModel: DealsViewModel

    private val PREFS_FILENAME = "com.jedmahonisgroup.gamepoint"


    private var totalPts: TextView? = null
    private var repository: gamePointSharedPrefsRepo? = null
    private val disposables = CompositeDisposable()
    private var token: String? = null
    private var mRedeemedDeals = ArrayList<DealModel>()
    //ui
    private var mSettings: ImageButton? = null
    private var mOnline: Button? = null
    private var mInStore: Button? = null
    private var mBack: ImageButton? = null
    private var mOnlineInd: View? = null
    private var mInStoreInd: View? = null

    private var firstTime = true

    private var onlineDeal: Int = 0
    private var instoreDeal: Int = 0

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    var LOCATION_REQUEST_CODE = 101

    private var onlinedeal: Boolean? = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_redeem, container, false)
        val rootView = binding?.root
        super.onCreateView(inflater, container, savedInstanceState)
        dealsViewModel = ViewModelProviders.of(this, ViewModelFactory(this.activity as AppCompatActivity)).get(DealsViewModel::class.java)
        repository = SharedPreferencesGamePointSharedPrefsRepo.create(requireActivity().baseContext)
        sharedPreferences = activity?.applicationContext!!.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest.create()
        locationRequest.interval = 50000
        locationRequest.fastestInterval = 10000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                for (location in locationResult.locations){
                    userLat = location.latitude
                    userLng = location.longitude
                    //setUserLocation(location)
                }
            }
        }

        mSwipeToRefresh = rootView?.findViewById(R.id.deal_swipe_refresh_layout)
        mSwipeToRefresh?.isRefreshing = true
        dealsViewModel.getDealsFromDb()
        //observe the response
        Handler().postDelayed({
            resultFromViewModel()
        }, 1500)

        mSettings = rootView?.findViewById(R.id.dealSettings)


        getToken()

        totalPts = rootView?.findViewById<Button>(R.id.redeemTotalPointsCount)

        setUpUi(rootView)

        return rootView
    }

    @SuppressLint("MissingPermission")
    private fun obtainLocalization(){
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            userLat =  location?.latitude
            userLng = location?.longitude
        }
    }


    private fun resultFromViewModel() {
        //first, try to get data from the database
        if (view != null) {
            dealsViewModel.successGettingDeals.observe(viewLifecycleOwner, Observer {
                Log.e(TAG, "found deals in database")
                mSwipeToRefresh?.isRefreshing = false
                if (it != null) {
                    Log.i(TAG, " successGettingDeals === > $it")
                    //do something with your server eventsFromServer

                    setupRecyclerview(it)
                }
            })
            //if it fails, get data from server.
            dealsViewModel.errorGettinDealsFromDb.observe(viewLifecycleOwner, Observer {
                Log.e(TAG, "error fetching data from databse $it")
                mSwipeToRefresh?.isRefreshing = false

                try {
                    dealsViewModel.getDeals(token!!)
                } catch (e: NullPointerException) {
                    Log.e(TAG, "could not fetch data. Token is null")
                    //maybe let the user know to logout. or clear data
                }

            })

            // lets try to get you some server data
            dealsViewModel.dealsData.observe(viewLifecycleOwner, Observer { response ->
                if (response != null) {
                    mSwipeToRefresh?.isRefreshing = false

                    //dealsViewModel
                    mSwipeToRefresh!!.isRefreshing = false

//                Log.e("$TAG deals list", response.toString())
                    //do something with your server eventsFromServer
                    Log.e("JMG", "deals fragment got deals from server")
                    setupRecyclerview(response)
                }
            })

            //if server data fails, then boy (or girl) do we have a problem
            dealsViewModel.errorFetchingDealsData.observe(viewLifecycleOwner, Observer {
                mSwipeToRefresh?.isRefreshing = false

                Log.e(TAG, "error fetching data from server. display snack bar: $it")
                if (it.equals("HTTP 401 Unauthorized")) {
                    //do something if it fails...maybe show the user some info
                }
            })

            dealsViewModel.errorSavingDeals.observe(viewLifecycleOwner, Observer {
                mSwipeToRefresh?.isRefreshing = false

                Log.e(TAG, "could not save data to db: error: $it")
            })

            try {
                dealsViewModel.getDeals(token!!)

            }catch (e: java.lang.NullPointerException){
                Log.e(TAG, "Token was null in deals fragment")
            }

        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        dealsViewModel.getDeals(token!!)

    }

    private fun makeDealsUiList(deal: List<DealModel>): ArrayList<DealsUi> {
        var dealsUi: DealsUi? = null
        //this holds the new lists
        val dealsUiList: ArrayList<DealsUi> = ArrayList()
        //keeps track of the business ids
        val murArrayList: ArrayList<String> = ArrayList()
        val onlineArrayList: ArrayList<String> = ArrayList()
        val active: ArrayList<DealModel> = ArrayList()
        var d: DealModel?
        for (i in 0 until deal.size) {
            if (onlinedeal!!){
                if (!deal[i].coupon_code.isNullOrEmpty()){
                    d = deal[i]
                    active.add(d)
                    val address = com.jedmahonisgroup.gamepoint.model.events.Address(1, "Online", "", 0, "", d.business_id.toString(), 0, 0.0, 0.0, State(0, "",""))
                    dealsUi = dealsonlineUi(address, dealsUi, active, d)
                    if (!onlineArrayList.contains(d.business_id.toString())) {
                        onlineArrayList.add(d.business_id.toString())
                        dealsUiList.add(dealsUi)
                    } else {
                        for (i in 0 until dealsUiList.size){
                            if (dealsUiList[i].business.id == d.business_id){
                                dealsUiList[i].deal!!.add(d)
                                dealsUiList[i].numberOfDeals++
                                Log.e("deal", " " + dealsUiList[i].deal!!.size)
                            }
                        }
                    }
                }
            }else{
                if (deal[i].coupon_code.isNullOrEmpty()){
                    d = deal[i]
                    active.add(d)
                    val bus: Business = d!!.businesses
                    if (!bus.addresses.isNullOrEmpty()) {
                        for (address in bus.addresses) {
                            dealsUi = dealsUi(address, dealsUi, active, d)
                            if (!murArrayList.contains(address.id.toString())) {
                                murArrayList.add(address.id.toString())
                                dealsUiList.add(dealsUi)
                            } else {
                                //Add the updated deal with the number of items count
                                val f = replaceDealsUi(dealsUiList, dealsUi, d)
                                if (!f.deal.isNullOrEmpty()) {
                                    dealsUiList.add(f)
                                }
                            }
                        }
                    } else {
                        val address = com.jedmahonisgroup.gamepoint.model.events.Address(1, "Online", "", 0, "", bus.id.toString(), 0, 0.0, 0.0, State(0, "",""))
                        dealsUi = dealsUi(address, dealsUi, active, d)
                        if (!murArrayList.contains(address.id.toString())) {

                            murArrayList.add(address.id.toString())
                            dealsUiList.add(dealsUi)
                        }  else {
                            //Add the updated deal with the number of items count

                            val f = replaceDealsUi(dealsUiList, dealsUi, d)
                            if (!f.deal.isNullOrEmpty()) {
                                dealsUiList.add(f)
                            }
                        }
                    }
                }
            }
        }
        return dealsUiList
    }



    private fun dealsonlineUi(address: Address,dealsUi: DealsUi?, active: ArrayList<DealModel>, d: DealModel): DealsUi {
        var dealsUi1 = dealsUi
        val businessLocation: Location = Location("")
        val userLocation: Location = Location("")
        dealsUi1 = DealsUi(
                deal = ArrayList(active.distinctBy { it.id }),
                business = d.businesses,
                address = address,
                //addressId = addresses.id,
                dealBusinessName = d.businesses.name,
                distance = getMiles(userLocation.distanceTo(businessLocation)).toInt().toString(),
                numberOfDeals = 2,
                hero_image = d.hero_image,
                activeDealCount = 0,
                online = true
        )
        return dealsUi1
    }

    private fun dealsUi(address: Address, dealsUi: DealsUi?, active: ArrayList<DealModel>, d: DealModel): DealsUi {

        var dealsUi1 = dealsUi
        val businessLocation: Location = Location("")
        val userLocation: Location = Location("")

        if (userLat != null || userLng != null) {
            businessLocation.latitude = address.latitude
            businessLocation.longitude = address.longitude

            userLocation.longitude = userLng!!
            userLocation.latitude = userLat!!


            dealsUi1 = DealsUi(
                    deal = ArrayList(active.distinctBy { it.id }),
                    business = d.businesses,
                    address = address,
                    //addressId = addresses.id,
                    dealBusinessName = d.businesses.name,
                    distance = getMiles(userLocation.distanceTo(businessLocation)).toInt().toString(),
                    numberOfDeals = 1,
                    hero_image = d.hero_image,
                    activeDealCount = 0,
                    online = false
            )
        } else {


            dealsUi1 = DealsUi(
                    deal = ArrayList(active.distinctBy { it.id }),
                    business = d.businesses,
                    address = address,
                    //addressId = addresses.id,
                    dealBusinessName = d.businesses.name,
                    distance = null,
                    numberOfDeals = 1,
                    hero_image = d.hero_image,
                    activeDealCount = 0,
                    online = false
            )

        }
        return dealsUi1
    }

    fun getMiles(i: Float): Double {
        return i*0.000621371192;
    }

    private fun getUserLocation() {

        if (checkPermission()) {
            val lm = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager?
            val location = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            userLat = location?.latitude
            userLng = location?.longitude

        }else{
            userLat = null
            userLng = null
        }


    }

    private fun setUserLocation() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations){
                    userLat = location.latitude
                    userLng = location.longitude
                }
            }
        }
        /*
        if (checkPermission()) {
            userLat = location.latitude
            userLng = location.longitude
        }else{
            userLat = null
            userLng = null
        }
        */
    }

    private fun replaceDealsUi(dealsUiList: ArrayList<DealsUi>, dealsUi: DealsUi, deal: DealModel): DealsUi {

        var arrayOfIndicesToRemove = ArrayList<Int>()
        for (i in 0 until dealsUiList.size) {
            var d = dealsUiList[i]
            if (d.address.id == dealsUi.address.id) {

                dealsUi.numberOfDeals = d.numberOfDeals + 1
                dealsUi.deal = ArrayList(dealsUi.deal?.distinctBy { it.id }!!)
                //remove this deal from this list because we are going to add the updated one later
                arrayOfIndicesToRemove.add(i)



            }
        }

        for (i in arrayOfIndicesToRemove) {
            dealsUiList.removeAt(i)
        }

        return dealsUi
    }

    // Check for permission to access Location
    private fun checkPermission(): Boolean {
        // Ask for permission if it wasn't granted yet
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun getToken() {
        token = requireArguments().getString("token")
        Log.i(TAG, "getToken ===========> token: $token")
        try {
            dealsViewModel.getDeals(token!!)
        } catch (e: NullPointerException) {
            Log.e(TAG, "token was null inside $TAG, $e")
        }

    }

    @SuppressLint("ResourceAsColor")
    private fun setUpUi(rootView: View?) {

        mOnline = rootView!!.findViewById(R.id.onlineButton)
        mInStore = rootView.findViewById(R.id.inStoreButton)
        mInStoreInd = rootView.findViewById(R.id.inStoreButton_ind)
        mOnlineInd = rootView.findViewById(R.id.onlineButton_ind)
        mBack = rootView.findViewById(R.id.btnBack)
        val leadingText = rootView.findViewById(R.id.leadingText) as TextView
        getSchoolColor()?.let { leadingText.setTextColor(it) }

        mBack?.setOnClickListener {
            requireActivity().onBackPressed()
        }

        mOnline!!.setOnClickListener {

            makeOnline()
            onlinedeal = true
            dealsViewModel.getDeals(token!!)
            mSwipeToRefresh?.isRefreshing =true
            leadingText.setText(R.string.online_stores)
        }

        mInStore!!.setOnClickListener {

            makeInStore()
            onlinedeal = false
            dealsViewModel.getDeals(token!!)
            mSwipeToRefresh?.isRefreshing =true
            leadingText.setText(R.string.featured_stores)
        }


        mSettings?.setOnClickListener {
            val myIntent = Intent(this.activity, SettingsActivity::class.java)
            myIntent.putExtra("token", token) //Optional parameters
            startActivity(myIntent)
        }

        setTotalPoints()
//        disposables.add(repository?.points(Constants.TOTAL_POINTS_KEY)?.subscribe({
//            totalPts?.text = it
//        }, {
//            totalPointsCount?.text = 0.toString()
//
//            Log.e("Error : ", "", it)
//        })!!)

        mSwipeToRefresh?.setOnRefreshListener {            //load items

            dealsViewModel.getDeals(token!!)
        }

        //display badge

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

    private fun removeExpiredDealsFromSp(dealData: List<DealsUi>): ArrayList<CurrentRedeemingDealModel>? {
        val gson = Gson()
        val type = object : TypeToken<ArrayList<CurrentRedeemingDealModel>>() {}.type
        val redeemedStr = sharedPreferences.getString("redeemedStr", "")
        val redeemedDeals = gson.fromJson<ArrayList<CurrentRedeemingDealModel>>(redeemedStr, type)

        //time right now
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        val dt = formatter.parseDateTime(StringFormatter.getDate().toString())
        val now: LocalDateTime = LocalDateTime(dt)

        val newRedeemedList = ArrayList<CurrentRedeemingDealModel>()
        return if (!redeemedStr.isNullOrEmpty()){
            //so in this case, there exist deals
            val iterator = redeemedDeals.iterator()
            for (i in iterator){
                val expTime = StringFormatter.getFormatedTimeStamp(i.expireTimeStamp)
                if (now.isAfter(expTime)){
                    //deal is expired please remove it
                    iterator.remove()
                }
            }
            redeemedDeals
        }else{
            //in this case there are zero deals in the sp so just return the original list
            redeemedDeals
        }
    }

    private fun dealDataFromResponse(dealData: List<DealsUi>): ArrayList<DealsUi> {
        val redeemedDeals = removeExpiredDealsFromSp(dealData)
        val newList = ArrayList<DealsUi>()
        if (!redeemedDeals.isNullOrEmpty()) {

            Log.e(TAG, "yay! we got something $redeemedDeals")
            //find where the address id's and
            for (dealsUi in dealData) {
                //these are the deals we have
                for (liveDeal in dealsUi.deal!!) {
                    //these are the deals being redeemed
                    for (currentRedeeming in redeemedDeals){
                        if (dealsUi.address.id == currentRedeeming.addressId && liveDeal.id == currentRedeeming.dealId) {
                            dealsUi.activeDealCount = dealsUi.activeDealCount.inc()

                        }
                    }
                }
                newList.add(dealsUi)
            }
            return newList
        } else {
            return ArrayList(dealData)
        }
    }


    private fun setupRecyclerview(response: List<DealModel>) {
        val gson = Gson()
        //fixDeals(response)
        val test = makeDealsUiList(response)
        val dealData = dealDataFromResponse(test)
        Log.e("JMG", "dealData: " + dealData)

        if (dealData.isNullOrEmpty()){

            if(firstTime && !onlinedeal!!){
                makeOnline()
                onlinedeal = true
                firstTime = false
                dealsViewModel.getDeals(token!!)
                mSwipeToRefresh?.isRefreshing =true
                leadingText.setText(R.string.online_stores)

            }else{
                if (onlinedeal!!){

                    makeOnline()
                    onlinedeal = true



                }else{
                    makeInStore()
                    onlinedeal = false


                }
                firstTime = false
                dealsNoDataView.visibility = View.VISIBLE
                dealsRv.visibility = View.GONE

            }
        }else{
            firstTime = false
            dealsNoDataView.visibility = View.GONE
            dealsRv.visibility = View.VISIBLE
            dealsRv.apply {
                //set a linear layout manager
                layoutManager = LinearLayoutManager(context)
                adapter = DealAdapter(dealData, this@DealFragment, getSchoolColorAsString())
            }
        }


    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is RedeemListener) {
            listener = context
        } else {
            throw ClassCastException(requireContext().toString() + " must implement RedeemListener")
        }
    }

    override fun onLaunchDetailScreen(data: DealsUi) {
        listener?.onSwitchRedeemDetail(data)
        Log.i(TAG, "Launch Redeem Location fragment")
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()

    }

    interface RedeemListener {
        fun onRedeemClicked()
        fun onSwitchRedeemDetail(data: DealsUi)
    }

    private fun checkSettingsStartLocationUpdates(){
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build()
        val client: SettingsClient = LocationServices.getSettingsClient(requireContext())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder)

        task.addOnSuccessListener { _ ->
            startLocationUpdates()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult((requireContext() as Activity),
                            101)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                    sendEx.printStackTrace()
                }
            }
        }
    }

    private fun startLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        )

    }

    private fun stopLocationUpdates(){
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            checkSettingsStartLocationUpdates()
        }else
            askLocationPermission()

    }

    private fun askLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),LOCATION_REQUEST_CODE)
            }else{
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),LOCATION_REQUEST_CODE)
            }
        }
    }

    // stop receiving location update when activity not visible/foreground
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    // start receiving location update when activity  visible/foreground
    override fun onResume() {
        super.onResume()
        setToolbarColors(toolbar_redeem)
        makeInStore()
    }


    private fun makeInStore(){
        mInStoreInd?.visibility = View.VISIBLE
        mOnlineInd?.visibility = View.GONE
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.cornerRadii = floatArrayOf(0f, 0f, 60f, 60f, 60f, 60f, 0f, 0f)
        shape.setColor(resources.getColor(R.color.colorWhite))
        getSchoolColor()?.let { shape.setStroke(2, it) }
        mOnline!!.background = shape
        getSchoolColor()?.let { mOnline!!.setTextColor(it) }

        val streakShape = GradientDrawable()
        streakShape.shape = GradientDrawable.RECTANGLE
        streakShape.cornerRadii = floatArrayOf(60f, 60f, 0f, 0.0f, 0f, 0f, 60f, 60f)
        getSchoolColor()?.let { streakShape.setColor(it) }
        getSchoolColor()?.let { streakShape.setStroke(2, it) }
        inStoreButton!!.background = streakShape
        inStoreButton!!.setTextColor(resources.getColor(R.color.colorWhite))

    }

    private fun makeOnline(){

        mInStoreInd?.visibility = View.GONE
        mOnlineInd?.visibility = View.VISIBLE

        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.cornerRadii = floatArrayOf(60f, 60f, 0f, 0.0f, 0f, 0f, 60f, 60f)
        shape.setColor(resources.getColor(R.color.colorWhite))
        getSchoolColor()?.let { shape.setStroke(2, it) }
        inStoreButton!!.background = shape
        getSchoolColor()?.let { inStoreButton!!.setTextColor(it) }

        val streakShape = GradientDrawable()
        streakShape.shape = GradientDrawable.RECTANGLE
        streakShape.cornerRadii = floatArrayOf(0f, 0f, 60f, 60f, 60f, 60f, 0f, 0f)
        getSchoolColor()?.let { streakShape.setColor(it) }
        getSchoolColor()?.let { streakShape.setStroke(2, it) }
        mOnline!!.background = streakShape
        mOnline!!.setTextColor(resources.getColor(R.color.colorWhite))
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }
}
