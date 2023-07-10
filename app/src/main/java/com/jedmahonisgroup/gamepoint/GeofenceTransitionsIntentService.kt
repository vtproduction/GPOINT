package com.jedmahonisgroup.gamepoint

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.util.Log
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
import com.google.android.gms.location.GeofencingEvent
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.jedmahonisgroup.gamepoint.model.CheckinsResponseModel
import com.jedmahonisgroup.gamepoint.model.EventGeofenceModel
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import com.jedmahonisgroup.gamepoint.utils.Constants
import com.jedmahonisgroup.gamepoint.utils.Constants.PREFS_FILENAME
import com.jedmahonisgroup.gamepoint.utils.GeofenceErrorMessages
import com.jedmahonisgroup.gamepoint.utils.showNotification
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject


class GeofenceTransitionsIntentService : JobIntentService() {

    private var geofenceListener: GeofenceListener? = null
    private val TAG: String = GeofenceTransitionsIntentService::class.java.simpleName
    private var geofenceViewModel: GeofenceViewModel? = null
    private var subscription: Disposable? = null


    @Inject
    lateinit var gamePointApi: GamePointApi

    override fun onCreate() {
        (application as GamePointApplication).getViewModelInjector().inject(this)
        super.onCreate()

    }

    companion object {
        private const val LOG_TAG = "GeoTrIntentService"

        private const val JOB_ID = 573

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                    context,
                    GeofenceTransitionsIntentService::class.java, JOB_ID,
                    intent)
        }
    }


    fun setCallbacks(callbacks: GeofenceListener) {
        geofenceListener = callbacks
    }

    override fun onHandleWork(intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                val errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.errorCode)
                Log.e(LOG_TAG, errorMessage)
                return
            }
            handleEvent(geofencingEvent)
        }


    }

    private fun handleEvent(event: GeofencingEvent) {
        geofenceListener
        if (event.geofenceTransition == GEOFENCE_TRANSITION_ENTER) {
            val message = "Keep this app running to earn points while you are checked in at this event"
            enterGeofenceBroadcast()
            showNotification(applicationContext, ("1-Enter").run { hashCode() }, "You're in the game", message)

        } else if (event.geofenceTransition == GEOFENCE_TRANSITION_EXIT) {
            exitGeofenceBroadcast()
            val message = "We hope you had fun!"
            checkUserOut()
            showNotification(applicationContext, ("1-Exit").run { hashCode() }, "See ya later!", message)
        }
    }

    private fun checkUserOut() {
        //try to checkout from the network
        val sharedPreferences = applicationContext.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

        val response = sharedPreferences.getString("checkin_response", "")
        val token = sharedPreferences.getString("TOKEN", "")
        val reminderStr = sharedPreferences.getString("REMINDERS", "")
        val type = object : TypeToken<ArrayList<EventGeofenceModel>>() {}.type


        //remove the checkout ui
        val editor = sharedPreferences.edit()
        editor.putBoolean(Constants.CHECKED_IN, false)
        editor.apply()

        val gson = Gson()
        val data = gson.fromJson(response, CheckinsResponseModel::class.java)
        val rm = gson.fromJson<ArrayList<EventGeofenceModel>>(reminderStr, type)

        val reminder = rm[0]

        //remove the geofence
        if (!data.equals(null)) {
            removeGeofence(reminder,token ,data.id.toString())
        }


    }


    private fun removeGeofence(reminder: EventGeofenceModel, token: String?, id: String) {
        (application as GamePointApplication).getRepository().remove(
                reminder,
                success = { eventId ->
                    checkout(token, id)
                    Log.i(TAG, "removed geofence in the background $eventId")

                },
                failure = {
                    //toast msg
                })
    }


    private fun checkout(token: String?, id: String) {
        subscription = gamePointApi.postCheckOut("Bearer $token", id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onGetDataStart() }
                .doOnTerminate { onGetDataFinish() }
                .subscribe({ t ->
                    onCheckoutSuccess(t, token, id)
                }, { error ->
                    var message = "Something went wrong, please try again"
                    if (error is HttpException) {
                        val errorJsonString = error.response()?.errorBody()?.string()
                        val arr  = JsonParser().parse(errorJsonString)
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
        Log.i(TAG, "there was a problem checking the user out in the background")


    }

    private fun onCheckoutSuccess(t: CheckinsResponseModel?, token: String?, id: String) {
          Log.i(TAG, "successfully checked user out in the background")
    }

    private fun onGetDataFinish() {
        Log.i(TAG, "finished background checkout")
    }

    private fun onGetDataStart() {
        Log.i(TAG, "starting background checkout")
    }


    private fun enterGeofenceBroadcast() {
        val enterGeofenceIntent = Intent("GEOFENCE_TRANSITION_ENTER")
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(baseContext).sendBroadcast(enterGeofenceIntent)
    }

    private fun exitGeofenceBroadcast() {
        val exitGeofenceIntent = Intent("GEOFENCE_TRANSITION_EXIT")
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(baseContext).sendBroadcast(exitGeofenceIntent)
    }


}