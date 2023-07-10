package com.jedmahonisgroup.gamepoint.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.events.EventsModel
import com.jedmahonisgroup.gamepoint.ui.events.EventDetailActivity
import com.jedmahonisgroup.gamepoint.utils.Constants.PREFS_FILENAME


fun showNotification(context: Context, id: Int, title: String, msg: String) {
    val intent = Intent(context, EventDetailActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val builder = NotificationCompat.Builder(context, GamePointApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.white_icon)
            .setContentTitle(title)
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

    val prefs = context.applicationContext.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    val savedEventString = prefs.getString("saved_event", "")
    if (!savedEventString.isNullOrEmpty()) {
        // Set the content Intent here. If there is no event saved, it will crash when trying to load Event detail. Instead we will just let the app load normally.
        intent.putExtra("event", savedEventString)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        builder.setContentIntent(pendingIntent)
    }

    with(NotificationManagerCompat.from(context)) {
        notify(id, builder.build())
    }
}

