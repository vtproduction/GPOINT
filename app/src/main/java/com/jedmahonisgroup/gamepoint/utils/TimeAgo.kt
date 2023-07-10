package com.jedmahonisgroup.gamepoint.utils

import android.util.Log
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object TimeAgo {
    public fun covertTimeToText(dataDate: String?): String? {
        var convertTime: String? = null
        val suffix = "ago"
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val pasTime: Date = dateFormat.parse(dataDate)
            val nowTime = Date()
            val dateDiff: Long = nowTime.time - pasTime.time

            if (dateDiff <= 0){

                convertTime = "A few seconds $suffix"

            }else{


            val second: Long = TimeUnit.MILLISECONDS.toSeconds(dateDiff)
            val minute: Long = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
            val hour: Long = TimeUnit.MILLISECONDS.toHours(dateDiff)
            val day: Long = TimeUnit.MILLISECONDS.toDays(dateDiff)
            Log.v("pasTime", "$pasTime ")
            Log.e("nowTime", "$nowTime ")
            Log.e("second", "$second ")
            Log.e("minute", "$minute ")
            Log.e("hour", "$hour ")
            Log.e("day", "$day ")


            convertTime = if (second < 60) {
                if (second == 1L) {
                    "$second second $suffix"
                } else {
                    "$second seconds $suffix"
                }
            } else if (minute < 60) {
                if (minute == 1L) {
                    "$minute minute $suffix"
                } else {
                    "$minute minutes $suffix"
                }
            } else if (hour < 24) {
                if (hour == 1L) {
                    "$hour hour $suffix"
                } else {
                    "$hour hours $suffix"
                }
            } else if (day >= 7) {
                if (day >= 365) {
                    val tempYear = day / 365
                    if (tempYear == 1L) {
                        "$tempYear year $suffix"
                    } else {
                        "$tempYear years $suffix"
                    }
                } else if (day >= 30) {
                    val tempMonth = day / 30
                    if (tempMonth == 1L) {
                        (day / 30).toString() + " month " + suffix
                    } else {
                        (day / 30).toString() + " months " + suffix
                    }
                } else {
                    val tempWeek = day / 7
                    if (tempWeek == 1L) {
                        (day / 7).toString() + " week " + suffix
                    } else {
                        (day / 7).toString() + " weeks " + suffix
                    }
                }
            } else {
                if (day == 1L) {
                    "$day day $suffix"
                } else {
                    "$day days $suffix"
                }
            }

            }
        } catch (e: ParseException) {
            e.printStackTrace()
            Log.e("TimeAgo", e.toString() + "")
        }

        return convertTime
    }
}