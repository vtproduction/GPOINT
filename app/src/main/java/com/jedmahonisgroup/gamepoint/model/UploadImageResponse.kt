package com.jedmahonisgroup.gamepoint.model

import com.jedmahonisgroup.gamepoint.model.picks.UserPick

data class UploadImageResponse(
        val id: Int,
        val first_name: String,
        val last_name: String,
        val email: String,
        val avatar: String,
        val total_points: Int,
        val this_month_pick_score: Int,
        val pick_rank: Int,
        val this_month_event_points: Int,
        val event_rank: Int,
        val redeemable: String,
        val userPick: List<UserPick>,
        val userCheckIn: List<CheckIn>,
        val url: String
)