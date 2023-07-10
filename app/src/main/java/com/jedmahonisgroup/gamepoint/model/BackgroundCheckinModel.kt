package com.jedmahonisgroup.gamepoint.model

data class BackgroundCheckinModel(
        val checkIn: BCheckIn
)

data class BCheckIn(
        //val id: Int,
        val user_id: Int,
        val event_id: Int,
        val checked_in: String,
        val verification_image: String

)
