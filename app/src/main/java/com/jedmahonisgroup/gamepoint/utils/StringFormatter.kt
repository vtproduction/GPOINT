package com.jedmahonisgroup.gamepoint.utils

import android.annotation.SuppressLint
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.io.UnsupportedEncodingException
import java.net.URL
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.*


object StringFormatter {

    @SuppressLint("SimpleDateFormat")
    fun convertTime(timestamp: String): String {
//        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
//        val dt = formatter.parseDateTime(data.start_time).toLocalDateTime()
//
        // Format for input
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        // Parsing the mDate
        val jodatime = dtf.parseDateTime(timestamp).toLocalDateTime()
        // Format for output
        val dtfOut = DateTimeFormat.forPattern("MMM d, h:mm a")
        // Printing the mDate

        return dtfOut.print(jodatime).toString()
    }

    fun convertTimeSpan(timestamp: String, mins: Int): String {
        // Format for input
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        // Parsing the mDate
        val jodatime = dtf.parseDateTime(timestamp).toLocalDateTime()
        val jodatime2 = dtf.parseDateTime(timestamp).toLocalDateTime().plusMinutes(mins)

        // Format for output
        val dtfOut = DateTimeFormat.forPattern("MMM d, h:mm a")
        val dtfOut2 = DateTimeFormat.forPattern("h:mm a")
        // Printing the mDate
        return "${dtfOut.print(jodatime)} - ${dtfOut2.print(jodatime2)}"
    }

    @SuppressLint("SimpleDateFormat")
    fun convertTimeStamp(timestamp: String): String {
        // Format for input
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        // Parsing the mDate
        val jodatime = dtf.parseDateTime(timestamp).toLocalDateTime()
        // Format for output
        val dtfOut = DateTimeFormat.forPattern("HH:MM:SS")
        // Printing the mDate

        return dtfOut.print(jodatime).toString()
    }

    @SuppressLint("SimpleDateFormat")
    fun convertTimestampToDate(timestamp: String): String {
        // Format for input
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        // Parsing the mDate
        val jodatime = dtf.parseDateTime(timestamp).toLocalDateTime()
        // Format for output
        val dtfOut = DateTimeFormat.forPattern("MM-dd-yyyy")
        // Printing the mDate

        return dtfOut.print(jodatime).toString()

    }

    fun convertTimestampToDayOfTheWeek(timestamp: String): String {
        // Format for input
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        // Parsing the mDate
        val jodatime = dtf.parseDateTime(timestamp).toLocalDateTime()
        // Format for output
        val dtfOut = DateTimeFormat.forPattern("EEEE")
        // Printing the mDate

        return dtfOut.print(jodatime).toString()

    }

    fun convertMonthToDayOfWeek(timestamp: String): String {
        // Format for input
        val dtf = DateTimeFormat.forPattern("MM-dd-yyyy")
        // Parsing the mDate
        val jodatime = dtf.parseDateTime(timestamp).toLocalDateTime()
        // Format for output
        val dtfOut = DateTimeFormat.forPattern("EEEE")
        // Printing the mDate

        return dtfOut.print(jodatime).toString()

    }

    fun getDayOfYear(timestamp: String): Int {
        // Format for input
        val dtf = DateTimeFormat.forPattern("MM-dd-yyyy")
        // Parsing the mDate
        val jodatime = dtf.parseDateTime(timestamp).toLocalDateTime()
        // Format for output
        //val dtfOut = DateTimeFormat.forPattern("EEEE")
        // Printing the mDate

        return jodatime.dayOfYear

    }

    fun getCurrentYear(): Int {
        return Calendar.getInstance().get(Calendar.YEAR)
    }

    @SuppressLint("SimpleDateFormat")
    fun dayOfYearToDate(dayOfYear: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, dayOfYear)
        val sdf = SimpleDateFormat("MM-dd-yyyy")

        return sdf.format(calendar.time)
    }

    @SuppressLint("SimpleDateFormat")
    fun dayOfyearToNameDate(dayOfYear: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, dayOfYear)
        val sdf = SimpleDateFormat("MMM, dd, yyyy")

        return sdf.format(calendar.time)
    }



    fun getTime(timestamp: String): String {
        // Format for input
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        // Parsing the mDate
        val jodatime = dtf.parseDateTime(timestamp).toLocalDateTime()
        // Format for output
        val dtfOut = DateTimeFormat.forPattern("h:mm a")
        // Printing the mDate

        return dtfOut.print(jodatime).toString()
    }

    private fun getTimeZone(timestamp: String): String? {
        val now = Calendar.getInstance()
        val timeZone = now.timeZone
        return timeZone.displayName
    }

    @SuppressLint("SimpleDateFormat")
    fun getTodayDate(): String? {
        return SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().time)
    }

    fun getTime(): String? {
        return SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().time)
    }

    fun getTomorrowDate(): String? {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, 1)
        val formatter = SimpleDateFormat("MM-dd-yyyy")
        System.out.println(calendar.time)

        return formatter.format(calendar.time)
    }

    /**
     *
     *
     *
     */

    fun getDate(): String? {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(Calendar.getInstance().time)
    }

    fun getLocalDate(): String? {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(Calendar.getInstance().time)
    }


    fun getMonth(date: String): Int {
        // Format for input
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        // Parsing the mDate
        val jodatime = dtf.parseDateTime(date).toLocalDateTime()
        // Format for output
        val dtfOut = DateTimeFormat.forPattern("MM")
        // Printing the mDate

        return dtfOut.print(jodatime).toInt()
    }

    fun getDay(date: String): Int {
        // Format for input
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        // Parsing the mDate
        val jodatime = dtf.parseDateTime(date).toLocalDateTime()
        // Format for output
        val dtfOut = DateTimeFormat.forPattern("dd")
        // Printing the mDate

        return dtfOut.print(jodatime).toInt()
    }

    fun getYear(date: String): Int {
        val pattern = "yyyy"

        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        val formatter = SimpleDateFormat(pattern, Locale.ENGLISH)

        return formatter.format(parser.parse(date)).toInt()
    }

    fun getmHour(date: String): Int {
        // Format for input
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        // Parsing the mDate
        val jodatime = dtf.parseDateTime(date).toLocalDateTime()
        // Format for output
        val dtfOut = DateTimeFormat.forPattern("yyyy")
        // Printing the mDate

        return dtfOut.print(jodatime).toInt()
    }

    fun getMinuet(date: String): Int {
        // Format for input
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        // Parsing the mDate
        val jodatime = dtf.parseDateTime(date).toLocalDateTime()
        // Format for output
        val dtfOut = DateTimeFormat.forPattern("mm")
        // Printing the mDate

        return dtfOut.print(jodatime).toInt()
    }

    fun getSeconds(date: String): Int {
        // Format for input
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        // Parsing the mDate
        val jodatime = dtf.parseDateTime(date).toLocalDateTime()
        // Format for output
        val dtfOut = DateTimeFormat.forPattern("ss")
        // Printing the mDate

        return dtfOut.print(jodatime).toInt()
    }

    fun getFormatedTimeStamp(timestamp: String): LocalDateTime {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        return formatter.parseDateTime(timestamp).toLocalDateTime()
    }

    fun getFormattedTimeStamp2(timestamp: String): LocalDateTime {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
        return formatter.parseDateTime(timestamp).toLocalDateTime()
    }

     fun hoursDifference(date1: Date, date2: Date): Int {
        val milliToHour : Long = 1000 * 60 * 60
        return ((date1.time - date2.time) / milliToHour).toInt()
    }


    @SuppressLint("SimpleDateFormat")
     private fun getCurrentTime(): String {
        return SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(Calendar.getInstance().time)
    }

    fun isValidPhoneNumber(S: String): Boolean {
        val regex = "[^\\d]"
        val phoneDigits = S.replace(regex.toRegex(), "")
        return phoneDigits.length == 10
    }


    @Throws(UnsupportedEncodingException::class)
    fun splitQuery(url: URL): Map<String, String> {
        val queries: MutableMap<String, String> = LinkedHashMap()
        val query: String = url.query
        val pairs = query.split("&").toTypedArray()
        for (pair in pairs) {
            val idx = pair.indexOf("=")
            queries[URLDecoder.decode(pair.substring(0, idx), "UTF-8")] =
                URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
        }
        return queries
    }

    fun strDuration(duration: Long): String? {
        val ms: Int
        val s: Int
        val m: Int
        val h: Int
        val d: Int
        var dec: Double
        var time = duration * 1.0
        time /= 1000.0
        dec = time % 1
        time -= dec
        ms = (dec * 1000).toInt()
        time /= 60.0
        dec = time % 1
        time -= dec
        s = (dec * 60).toInt()
        time /= 60.0
        dec = time % 1
        time -= dec
        m = (dec * 60).toInt()
        time /= 24.0
        dec = time % 1
        time -= dec
        h = (dec * 24).toInt()
        d = time.toInt()
        return String.format("%d d - %02d:%02d:%02d.%03d", d, h, m, s, ms)
    }
}