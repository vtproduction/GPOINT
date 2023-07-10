package com.jedmahonisgroup.gamepoint.model.leaderboard

data class EventLeaderboardModel(
        val name: String,
        val avatar: String,
        val total_points: Int,
        val this_month_pick_points: Int,
        val this_month_event_points: Int,
        val rank: Int,
        val current_user: Boolean,
        val position: Int
)
