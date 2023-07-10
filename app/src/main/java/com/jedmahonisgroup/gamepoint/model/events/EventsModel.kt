package com.jedmahonisgroup.gamepoint.model.events

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class EventsModel(
        @SerializedName("id")
        val id: Int,
        @SerializedName("name")
        val name: String,
        @SerializedName("venue_id")
        val venueId: Int,
        @SerializedName("sport_id")
        val sportId: Int,
        @SerializedName("team1")
        val team1: String,
        @SerializedName("team2")
        val team2: String,
        @SerializedName("point_value")
        val pointValue: Int,
        @SerializedName("minutes_to_redeem")
        val minutesToRedeem: Int,
        @SerializedName("start_time")
        val startTime: String,
        @SerializedName("ticket_url")
        val ticketUrl: String,
        @SerializedName("hero_image")
        val heroImage: String,
        @SerializedName("venue")
        val venue: Venue,
        @SerializedName("sport")
        val sport: Sport?,
        @SerializedName("school")
        val school: School?,
        @SerializedName("away_school")
        val awaySchool: School?,
        @SerializedName("pick_id")
        val pickId: Int?,
        @SerializedName("url")
        val url: String

)

data class EventsModelUi(
        val id: Int,
        val name: String,
        val venue_id: Int,
        val sport_id: Int,
        val team1: String,
        val team2: String,
        val point_value: Int,
        val minutes_to_redeem: Int,
        val start_time: String,
        val venue: Venue,
        val sport: Sport,
        val pick_id: Int,
        val ticket_url: String,
        val url: String,
        val hero_image: String,
        var selected: Boolean

)



