package com.jedmahonisgroup.gamepoint

import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.jedmahonisgroup.gamepoint.model.BCheckIn
import com.jedmahonisgroup.gamepoint.model.CheckinsResponseModel
import com.jedmahonisgroup.gamepoint.model.events.EventsModel
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import com.jedmahonisgroup.gamepoint.utils.Constants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject


class NetworkSchedulerService : JobService(), ConnectivityReceiver.ConnectivityReceiverListener {
    private val TAG = NetworkSchedulerService::class.java.simpleName
    private var mConnectivityReceiver: ConnectivityReceiver? = null

    private var subscription: Disposable? = null

    @Inject
    lateinit var gamePointApi: GamePointApi


    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service created")
        mConnectivityReceiver = ConnectivityReceiver(this)
        (application as GamePointApplication).getViewModelInjector().inject(this)

    }


    /**
     * When the app's NetworkConnectionActivity is created, it starts this service. This is so that the
     * activity and this service can communicate back and forth. See "setUiCallback()"
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand")
        return Service.START_NOT_STICKY
    }


    override fun onStartJob(params: JobParameters): Boolean {
        Log.i(TAG, "onStartJob" + mConnectivityReceiver!!)
        registerReceiver(mConnectivityReceiver, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        Log.i(TAG, "onStopJob")
        unregisterReceiver(mConnectivityReceiver)
        return true
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
//        Log.i(TAG, "onNetworkConnectionChanged =====> $isConnected")
//        val message = if (isConnected) "Good! Connected to Internet" else "Sorry! Not connected to internet"
//        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        if (isConnected) {
            //user was connected to the network
            //show the user a notification to let them know they were checked in
//           val message = "WE FOUND IT!!!!!"
//           showNotification(applicationContext, ("1-Exit").run { hashCode() }, "See..told ya!", message)

            //read from sp
            val sharedPreferences = applicationContext.getSharedPreferences(Constants.PREFS_FILENAME, Context.MODE_PRIVATE)
            val base64Proof = sharedPreferences.getString("CONFIRM_CHECK_IN_IMG", "")
            val timestamp = sharedPreferences.getString("CONFIRM_CHECK_IN_TIMESTAMP", "")
            val token = sharedPreferences.getString("TOKEN", "")
            val eventStr = sharedPreferences.getString("event_data", "")
            val userId = sharedPreferences.getString("USER_ID", "")
            val confirmStatus = sharedPreferences.getBoolean("CONFIRM_CHECK_IN_STATUS", false)

            val gson = Gson()
            val event = gson.fromJson(eventStr, EventsModel::class.java)

            //we only care about the right one
            if (confirmStatus) {
                if (userId?.toInt() != null && !timestamp.isNullOrEmpty() && !base64Proof.isNullOrEmpty()) {
                    val checkin = BCheckIn(
                            event_id = event.id,
                            user_id = userId.toInt(),
                            checked_in = timestamp,
                            verification_image = base64Proof
                    )
                    val str = gson.toJson(checkin)
                    Log.i(TAG, "da request:  $str")

                    checkIn(token, checkin)
                } else {
                    Log.e(TAG, "something was null while trying to check in on the background")
                }
            } else {
                //we don't care about these network change events.
            }
        } else {
            val sharedPreferences = applicationContext.getSharedPreferences(Constants.PREFS_FILENAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("SOS", false)
            editor.apply()

        }
        //right now we only care for when the user is connected to the internet, not disconnected

    }

    private fun checkIn(token: String?, checkin: BCheckIn) {

        subscription = gamePointApi.backgroundChecking("Bearer $token", checkin)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onGetDataStart() }
                .doOnTerminate { onGetDataFinish() }
                .subscribe({ t ->
                    onCheckoutSuccess(t)
                }, { error ->
                    var message = "Something went wrong, please try again"
                    if (error is HttpException) {
                        val errorJsonString = error.response()?.errorBody()?.string()
                        val arr = JsonParser().parse(errorJsonString)
                                .asJsonObject["errors"]
                                .asJsonArray
                        if (arr.size() > 0) {
                            message = arr.get(0).toString()
                            onCheckOutError(message)
                        }
                    } else {
                        error.message?.let {
                            onCheckOutError(it)
                        }
                    }
                })
    }

    private fun onCheckOutError(it: String) {
        Log.e(TAG, "we made amistake somewhere $it")
    }

    private fun onCheckoutSuccess(t: CheckinsResponseModel?) {
        Log.i(TAG, "WAY TOO GOOOO!!! we did it.")
        val gson = Gson()
        val responseStr = gson.toJson(t)
        //delet that other stuff and set check in to true
        val sharedPreferences = applicationContext.getSharedPreferences(Constants.PREFS_FILENAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        //because we just checked in
        editor.putBoolean(Constants.CHECKED_IN, true)

        //now lets delet these other stuff
        editor.putString("CONFIRM_CHECK_IN_IMG", "")
//        editor.putString("CONFIRM_CHECK_IN_TIMESTAMP", "")
        // editor.putString("TOKEN", "")
        // editor.putString("event_data", "")
        editor.putString("USER_ID", "")
        editor.putBoolean("CONFIRM_CHECK_IN_STATUS", false)
        editor.putString("checkin_response", responseStr)
        editor.putBoolean("SOS", false)

        editor.apply()
        networkIsAvailableBroadcast()

    }

    private fun onGetDataFinish() {
        Log.i(TAG, "")
    }

    private fun onGetDataStart() {
        Log.i(TAG, "")
    }


    private fun networkIsAvailableBroadcast() {
        val networkConnectedIntent = Intent("INTERNET_CONNECTED")
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(baseContext).sendBroadcast(networkConnectedIntent)
    }


}