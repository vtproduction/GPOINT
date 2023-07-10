package com.jedmahonisgroup.gamepoint.model

import java.io.Serializable

data class CheckInBody(
        val checkIn: CheckIn
)

data class CheckIn(
        //val id: Int,
        val user_id: Int,
        val event_id: Int,
//        val redeemed: Boolean,
//        val points_awarded: Int,
        val created_at: String
//        val url: String

) : Serializable



