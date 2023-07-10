package com.jedmahonisgroup.gamepoint

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        GeofenceTransitionsIntentService.enqueueWork(context, intent)
    }
}