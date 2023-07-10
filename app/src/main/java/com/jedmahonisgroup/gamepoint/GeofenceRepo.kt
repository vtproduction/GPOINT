package com.jedmahonisgroup.gamepoint

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.model.EventGeofenceModel
import com.jedmahonisgroup.gamepoint.utils.GeofenceErrorMessages


class EventRepository(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "com.jedmahonisgroup.gamepoint"
        private const val REMINDERS = "REMINDERS"
    }

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val TAG:String = EventRepository::class.java.simpleName
    private val gson = Gson()
    private val geofencingClient = LocationServices.getGeofencingClient(context)
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
                context,
                0,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun add(reminder: EventGeofenceModel, success: () -> Unit, failure: (error: String) -> Unit) {
        // 1
        val geofence = buildGeofence(reminder)
        Log.e(TAG, "adding geofence called")

        if (geofence != null
                && ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            Log.e("permission granted", "!!")
            // 2
            geofencingClient
                    .addGeofences(buildGeofencingRequest(geofence), geofencePendingIntent)
                    .addOnSuccessListener {
                        // 3
                        saveAll(getAll() + reminder)
                        success()
                        Log.i(TAG, "geofence add success")

                    }
                    .addOnFailureListener {
                        // 4
                        Log.e("failure", it.toString())
                        failure(GeofenceErrorMessages.getErrorString(context, it))
                    }
        }
    }

    private fun buildGeofence(reminder: EventGeofenceModel): Geofence? {
        val latitude = reminder.latitude
        val longitude = reminder.longitude
        val radius = reminder.radius

        if (latitude != null && longitude != null && radius != null) {
            return Geofence.Builder()
                    .setRequestId(reminder.id.toString())
                    .setCircularRegion(
                            latitude,
                            longitude,
                            radius.toFloat()
                    )
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .build()
        }

        Log.i(TAG, "buidling geofencce")
        return null
    }

    private fun buildGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(listOf(geofence))
                .build()
    }

    fun remove(event: EventGeofenceModel, success: (str: String) -> Unit, failure: (error: String) -> Unit) {
        geofencingClient
                .removeGeofences(listOf(event.id.toString()))
                .addOnSuccessListener {
                    saveAll(getAll() - event)
                    success(event.id.toString())
                }
                .addOnFailureListener {
                    failure(GeofenceErrorMessages.getErrorString(context, it))
                }
    }

    private fun saveAll(list: List<EventGeofenceModel>) {
        preferences
                .edit()
                .putString(REMINDERS, gson.toJson(list))
                .apply()
    }

    fun getAll(): List<EventGeofenceModel> {
        if (preferences.contains(REMINDERS)) {
            val remindersString = preferences.getString(REMINDERS, null)
            val arrayOfReminders = gson.fromJson(remindersString,
                    Array<EventGeofenceModel>::class.java)
            if (arrayOfReminders != null) {
                return arrayOfReminders.toList()
            }
        }
        return listOf()
    }

    fun get(requestId: String?) = getAll().firstOrNull { it.id.toString() == requestId }

    fun getLast() = getAll().lastOrNull()

}