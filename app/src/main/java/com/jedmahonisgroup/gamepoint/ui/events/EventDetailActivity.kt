package com.jedmahonisgroup.gamepoint.ui.events

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.drjacky.imagepicker.ImagePicker
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.NetworkSchedulerService
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.CheckIn
import com.jedmahonisgroup.gamepoint.model.CheckinsResponseModel
import com.jedmahonisgroup.gamepoint.model.EventGeofenceModel
import com.jedmahonisgroup.gamepoint.model.UserResponseModel
import com.jedmahonisgroup.gamepoint.model.events.EventsModel
import com.jedmahonisgroup.gamepoint.ui.BaseActivity
import com.jedmahonisgroup.gamepoint.ui.MainActivity
import com.jedmahonisgroup.gamepoint.ui.sharedPreferences
import com.jedmahonisgroup.gamepoint.utils.StringFormatter
import com.jedmahonisgroup.gamepoint.utils.StringFormatter.getDate
import com.jedmahonisgroup.gamepoint.utils.StringFormatter.getFormatedTimeStamp
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.above_18_alert.view.*
import kotlinx.android.synthetic.main.activity_event_detail.*
import kotlinx.android.synthetic.main.content_profile.*
import kotlinx.android.synthetic.main.event_expired_error.view.*
import kotlinx.android.synthetic.main.redeem_now_alert.view.*
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.concurrent.schedule


class EventDetailActivity : BaseActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private lateinit var mEvent: EventsModel
    private val TAG: String = EventDetailActivity::class.java.simpleName
    private val TAG2 = "NIEN_TAG2"
    private val REQ_PERMISSION = 100
    private val BACKGROUND_LOCATION_PERMISSION_CODE = 101
    val CAMERA_REQUEST_CODE = 4444

    private lateinit var map: GoogleMap

    private var latitude: Double? = null
    private var longitude: Double? = null
    private var userlatitude: Double? = null
    private var userlongitude: Double? = null
    private var ID: Int? = null
    private var latLong: LatLng? = null
    private var mRadius: Double = 200.0

    private var mMarker: Bitmap? = null
    private var mData: EventsModel? = null
    private var mMap: GoogleMap? = null
    private var mUser: UserResponseModel? = null
    private var mToken: String? = null

    private val PREFS_FILENAME = "com.jedmahonisgroup.gamepoint"
    private val CHECKED_IN: String = "CHECKED_IN"
    private var isCheckedIn: Boolean = false
    private lateinit var photoURI: Uri

    private var mResponse: CheckinsResponseModel? = null

    private lateinit var eventDetailViewModel: EventDetailViewModel

    private val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            Log.d(TAG2, "broadCastReceiver: ${intent?.action}")
            when (intent?.action) {
                "GEOFENCE_TRANSITION_ENTER" -> userInsdeGeofenceBroadcast()
                "GEOFENCE_TRANSITION_EXIT" -> checkUserOut()
                "INTERNET_CONNECTED" -> checkInUser()
            }
        }
    }


    // Location Stuff added by Rob
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null
    private var mLocationManager: LocationManager? = null

    private var mLocationRequest: LocationRequest? = null
    private val listener: com.google.android.gms.location.LocationListener? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 2 secs */

//    private val listener: LocationListener? = null
//    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */

    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */
    private var stillFindingLocation: Boolean = false
    private var timeOutError: Boolean = false

    private var locationManager: LocationManager? = null

    private val isLocationEnabled: Boolean
        get() {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }

    val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val uri = it.data?.data!!
            // Use the uri to load the image
//            if (requestCo == PROFILE_IMAGE_REQ_CODE) {
            Toast.makeText(this, "made it successfully ", Toast.LENGTH_SHORT).show()


            val filePath: String = uri!!.path.toString()
            //save to sp
            val imgBase64 = convertToBase64(filePath)
            val editor = sharedPreferences.edit()
            Log.e(TAG, "base 64 = $imgBase64")
            editor.putString("CONFIRM_CHECK_IN_IMG", imgBase64)
            editor.putString("CONFIRM_CHECK_IN_TIMESTAMP", getDate())
            editor.putBoolean("CONFIRM_CHECK_IN_STATUS", true)
            editor.apply()
            //start looking for network connection
            scheduleJob()
            //show them checkin scree
            showCheckInScreen()
//            }

        } else if (it.resultCode == ImagePicker.RESULT_ERROR) {

            Toast.makeText(this, ImagePicker.getError(it.data!!), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)
        setToolbarColors(toolbar_event_detail)
        eventDetailViewModel = ViewModelProviders.of(this, ViewModelFactory(this)).get(EventDetailViewModel::class.java)
        getUser()

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this@EventDetailActivity)

        initLocationStuff()
//        attemptCheckIn()
        successfull()
        Log.e(TAG + "calling check in", "method")

        back()


        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this).registerReceiver(userInsdeGeofenceBroadcast(), IntentFilter("GEOFENCE_TRANSITION_ENTER"))
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this).registerReceiver(checkUserOut(), IntentFilter("GEOFENCE_TRANSITION_EXIT"))
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this).registerReceiver(checkInUser(), IntentFilter("INTERNET_CONNECTED"))


    }

    private fun checkInUser(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                //check the user in and undo some of the stuff we did
                //close the page
                closeActivity()

            }

        }
    }


    private fun closeActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    fun initLocationStuff() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        mLocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    fun userInsdeGeofenceBroadcast(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
//                val checkin = CheckIn(
//                        event_id = mData!!.id,
//                        user_id = mUser!!.user.id,
//                        created_at = getDate()!!
//                )
//                eventDetailViewModel.postUserCheckin(mToken!!, checkin)

                //save timstamp and give it to
                Log.i(TAG, "geofence enter detected")

            }
        }
    }

    private fun checkUserOut(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                //check user out
                Log.e("$TAG checkUserOut: ", "==========> BYE!!!")

            }
        }
    }

    private fun getUser() {
        eventDetailViewModel.getUserFromDB(applicationContext)
        eventDetailViewModel.dbUserSuccess.observe(this, androidx.lifecycle.Observer {
            mUser = it
            mToken = it!!.user.login.token
            val editor = sharedPreferences.edit()
            editor.putString("USER_ID", mUser!!.user.id.toString())
            editor.apply()

        })

        eventDetailViewModel.dbUserFail.observe(this, androidx.lifecycle.Observer {
        })

    }

    private fun successfull() {
        eventDetailViewModel.successCheckIn.observe(this, Observer {
            if (it != null) {
                mResponse = it

                isCheckedIn = true
                val editor = sharedPreferences.edit()
                editor.putBoolean(CHECKED_IN, true)
                Log.i(TAG, "isCheckedIn ============> $isCheckedIn")
                Log.e(TAG, "checkin_response is =============> ${it}")

                editor.apply()
                sendDatatToMainActivity()
                finish()
            }
        })

        eventDetailViewModel.failedCheckin.observe(this, Observer {
            isCheckedIn = true
            val editor = sharedPreferences.edit()
            editor.putBoolean(CHECKED_IN, false)
            Log.i(TAG, "isCheckedIn ============> $isCheckedIn")

            editor.apply()
            alertUser(it.replace("\"",""), "Check in failed!")
        })

    }

    // Add the myMapView lifecycle to the activity's lifecycle methods
    public override fun onResume() {
        super.onResume()

        val gson = Gson()
        val data = gson.fromJson(intent.getStringExtra("event"), EventsModel::class.java)

        val userString = gson.toJson(data, EventsModel::class.java)
        mData = data
        Log.e("$TAG Received Data: ", data.toString())
        val editor = sharedPreferences.edit()
        editor.putString("event_data", userString)
        editor.apply()

        latitude = data.venue.latitude
        //        val strLat =
//        val strLng =
        //latitude = 44.954880
        longitude = data.venue.longitude
        //longitude = -93.375035
        //ID = data.id
        ID = 1
        Log.e(TAG, "================> Geofence ID $ID")

        latLong = LatLng(latitude!!, longitude!!)
        mRadius = data.venue.radius
        mEvent = data
        getMarker(data, data.sport?.map_pin, data.venue.name)



        val btnShape = GradientDrawable()
        btnShape.shape = GradientDrawable.RECTANGLE
        btnShape.cornerRadius= 30.0f
        btnShape.setColor(Color.parseColor(getSchoolColorAsString()))
        directions!!.background = btnShape
        //eventTeams.setTextColor(Color.parseColor(getSchoolColorAsString()))

        val dealsShape = GradientDrawable()
        dealsShape.shape = GradientDrawable.RECTANGLE
        dealsShape.cornerRadius= 60.0f
        dealsShape.setStroke(6, Color.parseColor(getSchoolColorAsString()))
        /*getTicketsBtn.setTextColor(Color.parseColor(getSchoolColorAsString()))
        getTicketsBtn!!.background = dealsShape*/

        setUpUi(data)
    }

    private fun getMarker(data: EventsModel, mapPin: String?, name: String) {
        if (mapPin != null && mapPin.isNotEmpty()) {
            //try to load a network image as the marker.
            //if its null though, load a system marker
            Picasso.get()
                    .load(mapPin)
                    .into(object : Target {
                        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                            /* Save the bitmap or do something with it here */
                            // Set it in the ImageView
                            mMarker = bitmap
                        }

                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                    })
        } else {
            //the url for marker was null, display a system marker


        }

    }

    private fun attemptCheckIn() {
        //display alert dialog here.


        val eventStartTime = mData?.startTime?.let { getFormatedTimeStamp(it) }
        //val now = getFormatedTimeStamp(getDate()!!)
        val now = eventStartTime!!
        Log.e(TAG, "eventStartTime === > $eventStartTime")

        val eventEndTime = mData?.startTime?.let { getFormatedTimeStamp(it) }?.plusMinutes(mData!!.minutesToRedeem)
        Log.e(TAG, "eventEndTime === > $eventEndTime")

        if (eventStartTime != null && eventEndTime != null) {
            Log.e("JMG", "now: " + now + " eventStart: " + eventStartTime + " eventEndTime: " + eventEndTime)

            if (now.isBefore(eventStartTime.minusHours(1)) || now.isAfter(eventEndTime)) {
                Log.e(TAG, "event has not started or its expired")
                //event is either not started, or trying to check to an event that is over. display alert dialog.
                var msg = "Looks like the event has not started yet, please wait till the event starts and try checking in again."
                if (now.isAfter(eventEndTime)) {
                    msg = "This event has ended. Please try attending a different event to earn more points!"
                }
                val title = "Oops"
                alertUser(msg, title)
            } else {
                Log.e(TAG, "event is not expired")
                //event is not expired, its still going on.
                startLookingForGeoFence()
//                checkLocation()
            }
        } else {
            Log.e(TAG, "event has not started or its expired and it was null")
            startLookingForGeoFence()
//            checkLocation()
        }
    }

    private fun startLookingForGeoFence() {
        timeOutError = false

        checkIfUserIsAround()
    }

    @SuppressLint("MissingPermission")
    private fun checkIfUserIsAround() {

        //testing purposes

        if (userlatitude == null || userlongitude == null) {
            Toast.makeText(this, "Getting Updated Location", Toast.LENGTH_LONG).show()
            stillFindingLocation = true


            Timer("locationFindTimeOut", false).schedule(15000) {
                Log.e(TAG, "waiting 10 seconds")
                runOnUiThread {
                    timeOutDetected()
                }
            }
            return
        }
        val lm = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

        //TODO FIX THIS
        val userLocation = Location("")
        userLocation.latitude = userlatitude!!
        userLocation.longitude = userlongitude!!
        /*userLocation.latitude = mEvent.venue.latitude
        userLocation.longitude = mEvent.venue.longitude*/

        val venueLocation = Location("")

        //TODO FIX THIS
        venueLocation.latitude = mEvent.venue.latitude
        venueLocation.longitude = mEvent.venue.longitude

        /*venueLocation.latitude = 0.0
        venueLocation.longitude = 0.0*/

        Log.e("JMG", "radius: " + mEvent.venue.radius)
        Log.e("JMG", "distanceTo: " + userLocation.distanceTo(venueLocation).toDouble())

        if (userLocation.distanceTo(venueLocation).toDouble() == mEvent.venue.radius || userLocation.distanceTo(venueLocation) < mEvent.venue.radius) {
            //check for internet connection
            if (isNetworkAvailable()) {
                val geoEventModel = EventGeofenceModel(
                        id = 1,
                        radius = mRadius.toInt(),
                        latitude = latitude!!.toDouble(),
                        longitude = longitude!!.toDouble()
                )
                //pass in the event being tracked
                addGeofence(geoEventModel)

            } else {
                stillFindingLocation = true
                timeOutDetected()
                //user is inside the geofence but the network is not available.
//                val msg = "Network is not available, connect to the internet and try again."
//                val title = "Network error"
//                alertUser(msg, title)
            }

        } else {
            Log.e(TAG, "user is not at event, therefore she can't checkin")
            //TODO CHECK THIS
            val geoEventModel = EventGeofenceModel(
                id = 1,
                radius = mRadius.toInt(),
                latitude = latitude!!.toDouble(),
                longitude = longitude!!.toDouble()
            )
            //pass in the event being tracked
            //addGeofence(geoEventModel)
            val msg = "To start getting points, you must be at the event venue. Head over there and try checking in again! "
            alertUser(msg, "Oops!")
        }

    }

    private fun timeOutDetected() {
        Log.e("JMG", "timeOutDetected")
        if (!stillFindingLocation) {
            return
        }
        timeOutError = true

        val title = "Oops"
        val msg = "We were unable to verify with location that you are at the event venue. Please take a photo of the event and we will keep trying to connect to verify you are at the event."

        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.above_18_alert, null)
        //AlertDialogBuilder
        val mBuilder = this.let { AlertDialog.Builder(this).setView(mDialogView) }
        //show dialog
        try {
            val mAlertDialog = mBuilder?.show()
            mAlertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val mTitle: TextView = mAlertDialog?.findViewById(R.id.required)!!
            val mInfo: TextView = mAlertDialog.findViewById(R.id.aboveEighteenText)!!
            val mPostBtn: TextView = mAlertDialog.findViewById(R.id.okBtn)!!
            val mNegBtn: TextView = mAlertDialog.findViewById(R.id.termsOfServiceBtn)!!

            mTitle.text = title
            mInfo.text = msg
            mPostBtn.text = "Take Photo"
            mNegBtn.text = "Cancel"
            // mPostBtn.alig

            //login button cick of custom layout
            mDialogView.okBtn.setOnClickListener {
                showCamera()
                mAlertDialog.dismiss()


            }

            mDialogView.termsOfServiceBtn.setOnClickListener {
                mAlertDialog.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showCamera() {
        ImagePicker.with(this)
                .cropSquare() // Crop Square image(Optional)
                .cameraOnly()    //User can only capture image using Camera
                .createIntentFromDialog { launcher.launch(it) }


    }

    private fun showCheckInScreen() {
        val gson = Gson()
        val checkinResponse = CheckinsResponseModel(
                created_at = getDate().toString(),
                event_id = mEvent.id,
                id = 0,
                points_awarded = 0,
                redeemed = false,
                url = "",
                user_id = mUser!!.user.id
        )

        val checkInResStr = gson.toJson(checkinResponse)
        val savedEvent = gson.toJson(mEvent)
        val editor = sharedPreferences.edit()
        editor.putString("checkin_response", checkInResStr)
        editor.putString("saved_event", savedEvent)
        editor.putBoolean("CHECKED_IN", true)
        editor.putBoolean("SOS", true)
        editor.apply()

        //force close this activity
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

        finish()
    }

    //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("MissingPermission")
    private fun scheduleJob() {

        val myJob = JobInfo.Builder(0, ComponentName(this, NetworkSchedulerService::class.java))
                .setRequiresCharging(false)
                .setMinimumLatency(1000)
                .setOverrideDeadline(2000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build()

        val jobScheduler: JobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(myJob)
    }

    private fun convertToBase64(filePath: String): String? {

        val bm = BitmapFactory.decodeFile(filePath)

        val baos = ByteArrayOutputStream()

        bm.compress(Bitmap.CompressFormat.JPEG, 50, baos)

        val byteArrayImage = baos.toByteArray()

        return Base64.encodeToString(byteArrayImage, Base64.DEFAULT)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
                        Activity.RESULT_OK -> {
                            //Image Uri will not be null for RESULT_OK


                        }

                    }

        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val fileUri = data.data
                    Toast.makeText(this, "made it successfully ", Toast.LENGTH_SHORT).show()


                    val filePath: String = fileUri!!.path.toString()
                    //save to sp
                    val imgBase64 = convertToBase64(filePath)
                    val editor = sharedPreferences.edit()
                    Log.e(TAG, "base 64 = $imgBase64")
                    editor.putString("CONFIRM_CHECK_IN_IMG", imgBase64)
                    editor.putString("CONFIRM_CHECK_IN_TIMESTAMP", getDate())
                    editor.putBoolean("CONFIRM_CHECK_IN_STATUS", true)
                    editor.apply()
                    //start looking for network connection
                    scheduleJob()
                    //show them checkin scree
                    showCheckInScreen()

//                    ImagePicker.RESULT_ERROR -> Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
//                    else -> Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this, "Unrecognized request code", Toast.LENGTH_SHORT)
            }
        }
    }

    private fun addGeofence(event: EventGeofenceModel) {
        getRepository().add(event,
                success = {
                    //addCircle()

                    setResult(Activity.RESULT_OK)
                    val checkin = CheckIn(
                            event_id = mData!!.id,
                            user_id = mUser!!.user.id,
                            created_at = getDate()!!
                    )
                    eventDetailViewModel.postUserCheckin(mToken!!, checkin)
                },
                failure = {
                    Log.i(TAG, "attemptCheckIn ------------> $it")
                    Snackbar.make(parent_detail, it, Snackbar.LENGTH_LONG).show()
                    //TODO change this back please!

//                    addCircle()
//
//                    setResult(Activity.RESULT_OK)
//                    val checkin = CheckIn(
//                            event_id = mData!!.id,
//                            user_id = mUser!!.user.id,
//                            created_at = getDate()!!
//                    )
//                    eventDetailViewModel.postUserCheckin(mToken!!, checkin)
                })
    }

    @SuppressLint("MissingPermission")
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager: ConnectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun alertUser(msg: String, title: String) {
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.event_expired_error, null)
        //AlertDialogBuilder
        val mBuilder = this.let { it1 ->
            AlertDialog.Builder(it1)
                    .setView(mDialogView)
        }
        val mInfo = mDialogView.findViewById<TextView>(R.id.eventDetailAlertInfo)
        val mTitle = mDialogView.findViewById<TextView>(R.id.checkedIn)
        mInfo.text = msg
        mTitle.text = title
        //show dialog
        val mAlertDialog = mBuilder?.show()
        mAlertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        //login button cick of custom layout
        mDialogView.close.setOnClickListener {
            mAlertDialog?.dismiss()

        }
    }

    @SuppressLint("MissingPermission")
    private fun addCircle() {
        mMap!!.addCircle(CircleOptions()
            .center(latLong)
            .radius(mRadius)
            .strokeColor(Color.parseColor(getSchoolColorAsString()))
            .fillColor(ContextCompat.getColor(this@EventDetailActivity, R.color.colorMapRadius)))
        map.isMyLocationEnabled = true
        //startLookingForGeoFence()
        attemptCheckIn()
    }

    private fun launchDirections(longitude: Double, latitude: Double, name: String) {
        directions.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=$latitude,$longitude ($name)"))
            startActivity(intent)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User granted location permission
                // Now check if android version >= 11, if >= 11 check for Background Location Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // Background Location Permission is granted so do your work here
                        addCircle()
                    } else {
                        // Ask for Background Location Permission
                        askPermissionForBackgroundUsage();
                    }
                }
            } else {
                // User denied location permission
                Toast.makeText(this, "Location request failed!", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == BACKGROUND_LOCATION_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User granted for Background Location Permission.
                addCircle()
            } else {
                // User declined for Background Location Permission.
                Toast.makeText(this, "Location request failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun back() {
        back_arrow.setOnClickListener {
            removeGeofence()
            finish()

        }
    }

    override fun onBackPressed() {
        if (mResponse != null) {
            sendDatatToMainActivity()
        }

        super.onBackPressed()
//        finish()

    }

    private fun setUpUi(data: EventsModel) {
        val month = StringFormatter.getMonth(data.startTime)
        val day = StringFormatter.getDay(data.startTime)
        val time = StringFormatter.getTime(data.startTime)

        var suffixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",  //    10    11    12    13    14    15    16    17    18    19
                "th", "th", "th", "th", "th", "th", "th", "th", "th", "th",  //    20    21    22    23    24    25    26    27    28    29
                "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",  //    30    31
                "th", "st")
        val dayStr: String = day.toString() + suffixes.get(day)

        val eventName = "${data.name} - ${data.venue.name}\n"
        val eventTime = "${data.sport?.name} ${monthToStr(month)}. $dayStr $time\n"
        val eventAddress = "${data.venue.address.street}, ${data.venue.address.city}, " + "${data.venue.address.state.abbrev}, " + "${data.venue.address.zipcode}\n"

        val text = eventName + eventAddress + eventTime

        findViewById<TextView>(R.id.txtContent).text = text
        findViewById<TextView>(R.id.txtPoint).text = data.pointValue.toString()
        Picasso.get().load(data.heroImage).fit().into(findViewById<ImageView>(R.id.imgHero))

        if (!data.sport?.icon.isNullOrEmpty()){
            Picasso.get()
                .load( data.sport?.icon)
                // .placeholder(R.drawable.event_fallback1000)
                .error(R.drawable.bg_event_list_item)
                .into(findViewById<ImageView>(R.id.img_logo_0))
        }
        if (!data.school?.logo.isNullOrEmpty()){
            Picasso.get()
                .load( data.school?.logo)
                // .placeholder(R.drawable.event_fallback1000)
                .error(R.drawable.bg_event_list_item)
                .into(findViewById<ImageView>(R.id.img_logo_1))
        }
        if (!data.awaySchool?.logo.isNullOrEmpty()){
            Picasso.get()
                .load( data.awaySchool?.logo)
                // .placeholder(R.drawable.event_fallback1000)
                .error(R.drawable.bg_event_list_item)
                .into(findViewById<ImageView>(R.id.img_logo_2))
        }

        findViewById<TextView>(R.id.txt_location)?.text = data.venue.name
        findViewById<TextView>(R.id.txt_time)?.text = StringFormatter.convertTimeSpan(data.startTime,data.minutesToRedeem)



        val pointsEveryTenMinutes = ((10.0 / data.minutesToRedeem.toDouble()) * data.pointValue.toDouble()).toInt()
        //checkInDetail.text = "Check in for ${pointsEveryTenMinutes} points / 10 Minutes"
        if (data.ticketUrl == null || data.ticketUrl.isEmpty()) {
            getTicketsBtn?.visibility = View.GONE
        } else {
            getTicketsBtn?.visibility = View.VISIBLE
        }
        checkInDetail?.setOnClickListener {
            /*if (checkPermission()) {
                attemptCheckIn()
            }else{
                askPermission()
            }*/
            checkPermission()
        }
        val cancelShape = GradientDrawable()
        cancelShape.shape = GradientDrawable.RECTANGLE
        cancelShape.cornerRadius = 70.0f
        cancelShape.setStroke(2, Color.parseColor(getSchoolColorAsString()))
        //getTicketsBtn.background = cancelShape
        //getTicketsBtn.setTextColor(Color.parseColor(getSchoolColorAsString()))

        getTicketsBtn.setOnClickListener {
            var url = data.ticketUrl
            if (!url.startsWith("http://") && !url.startsWith("https://")){
                url = "http://" + url;
        }

            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
    }

    private fun monthToStr(month: Int): String {
        var monStr = ""
        if (month == 1){
            monStr = "Jan"
        }else if (month == 2){
            monStr = "Feb"
        }else if (month == 3) {
            monStr = "Mar"
        }else if (month == 4) {
            monStr = "Apr"
        }else if (month == 5) {
            monStr = "May"
        }else if (month == 6) {
            monStr = "Jun"
        }else if (month == 7) {
            monStr = "Jul"
        }else if (month == 8) {
            monStr = "Aug"
        }else if (month == 9) {
            monStr = "Sep"
        }else if (month == 10) {
            monStr = "Oct"
        }else if (month == 11) {
            monStr = "Nov"
        }else if (month == 12) {
            monStr = "Dec"
        }


        return monStr
    }

    private fun centerCamera() {
        val zoom: Float = 14.526846.toFloat()
        val location = LatLng(latitude!!, longitude!!)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoom))

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap!!
        map.uiSettings.isMapToolbarEnabled = false
        map.run {
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isMapToolbarEnabled = false
        }

        Log.i(TAG + "marker", mMarker.toString())

        if (mMarker != null) {
            map.addMarker(MarkerOptions()
                    .position(LatLng(latitude!!, longitude!!))
                    //.title("Hello world")
                    .icon(BitmapDescriptorFactory.fromBitmap(mMarker))
            )

            mMap = map


        } else {
            map.addMarker(MarkerOptions()
                    .position(LatLng(latitude!!, longitude!!))
                    //.title("Hello world")
            )

            mMap = map

        }

        //addCircle()

        centerCamera()
        launchDirections(latitude!!, longitude!!, mData?.venue?.name!!)
    }

    interface RedeemPressed {
        fun onRedeemPressed()
    }

    // Check for permission to access Location
    private fun checkPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // Background Location Permission is granted so do your work here
                    addCircle()
                } else {
                    // Ask for Background Location Permission
                    askPermissionForBackgroundUsage()
                }
            }else{
                addCircle()
            }
        } else {
            // Fine Location Permission is not granted so ask for permission
            askForLocationPermission()
        }
    }

    private fun askForLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@EventDetailActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            AlertDialog.Builder(this)
                .setTitle("Permission Needed!")
                .setMessage("Location Permission Needed!")
                .setPositiveButton(
                    "OK"
                ) { _, _ ->
                    ActivityCompat.requestPermissions(
                        this@EventDetailActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQ_PERMISSION
                    )
                }
                .setNegativeButton(
                    "CANCEL"
                ) { _, _ ->
                    // Permission is denied by the user
                }
                .create().show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQ_PERMISSION
            )
        }
    }

    private fun askPermissionForBackgroundUsage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@EventDetailActivity,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        ) {
            AlertDialog.Builder(this)
                .setTitle("Permission Needed!")
                .setMessage("Background Location Permission Needed!, tap \"Allow all time in the next screen\"")
                .setPositiveButton(
                    "OK"
                ) { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this@EventDetailActivity,
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        BACKGROUND_LOCATION_PERMISSION_CODE
                    )
                }
                .setNegativeButton(
                    "CANCEL"
                ) { dialog, which ->
                    // User declined for Background Location Permission.
                }
                .create().show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                BACKGROUND_LOCATION_PERMISSION_CODE
            )
        }
    }



    // Asks for permission
    private fun askPermission() {
        Log.e(TAG2, "===============> askPermission()")


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            val backPermList = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            AlertDialog.Builder(this)
                .setTitle("Background location permission")
                .setMessage("Allow location permission to get location updates in background")
                .setPositiveButton("Allow") { _, _ ->
                    requestPermissions(
                        backPermList,
                        REQ_PERMISSION
                    )
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            val permList = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            ActivityCompat.requestPermissions(this, permList, REQ_PERMISSION)
        }else {
            val permList = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            ActivityCompat.requestPermissions(this, permList, REQ_PERMISSION)
        }

        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQ_PERMISSION)
    }

    private fun removeGeofence() {

    }

    override fun finish() {
        super.finish()
        removeGeofence()

    }

    private fun sendDatatToMainActivity() {
        // turn user object to a json string
        val gSon = Gson()
        val userString = gSon.toJson(mData)
        val checkInResponse = gSon.toJson(mResponse)

        val intent: Intent = Intent()
        intent.putExtra("data", userString)
        intent.putExtra("checkin_response", checkInResponse)
        setResult(RESULT_OK, intent)
        Log.e(TAG, "======= send result ${userString}")

        //finish();


    }

    override fun onDestroy() {
        super.onDestroy()
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(broadCastReceiver)
    }

    @SuppressLint("MissingPermission")
    override fun onConnected(p0: Bundle?) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        startLocationUpdates()

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)

        if (mLocation == null) {
            startLocationUpdates()
        }
        if (mLocation != null) {

            // mLatitudeTextView.setText(String.valueOf(mLocation.getLatitude()));
            //mLongitudeTextView.setText(String.valueOf(mLocation.getLongitude()));
        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnectionSuspended(i: Int) {
        Log.i(TAG, "Connection Suspended")
        mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.errorCode)
    }

    override fun onStart() {
        super.onStart()
        if (mGoogleApiClient != null) {
            mGoogleApiClient!!.connect()
        }
              // Start service and provide it a way to communicate with this class.
        val startServiceIntent: Intent =  Intent(this, NetworkSchedulerService::class.java);
        startService(startServiceIntent);
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient!!.isConnected) {
            mGoogleApiClient!!.disconnect()
        }

          // A service can be "started" and/or "bound". In this case, it's "started" by this Activity
        // and "bound" to the JobScheduler (also called "Scheduled" by the JobScheduler). This call
        // to stopService() won't prevent scheduled jobs to be processed. However, failing
        // to call stopService() would keep it alive indefinitely.
        stopService( Intent(this, NetworkSchedulerService::class.java))
        super.onStop();
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this)
        Log.d("reque", "--->>>>")
    }

    override fun onLocationChanged(location: Location) {

        val msg = "Updated Location: " +
                java.lang.Double.toString(location.latitude) + "," +
                java.lang.Double.toString(location.longitude)
        userlatitude = location.latitude
        userlongitude = location.longitude
        if (stillFindingLocation) {
            stillFindingLocation = false
            checkIfUserIsAround()
        }
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        // You can now create a LatLng Object for use with maps
    }

    private fun checkLocation(): Boolean {
        if (!isLocationEnabled)
            showAlert()
        return isLocationEnabled
    }

    private fun showAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
                .setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                }
                .setNegativeButton("Cancel") { paramDialogInterface, paramInt -> }
        dialog.show()
    }

    companion object {

//        private val TAG = "MainActivity"
    }
}

