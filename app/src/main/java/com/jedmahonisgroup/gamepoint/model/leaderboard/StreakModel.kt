package com.jedmahonisgroup.gamepoint.model.leaderboard

data class StreakModel(
        val name: String,
        val avatar: String,
        val total_points: Int,
        val current_streak: Int,
        val highest_streak: Int,
        val this_month_pick_points: Int,
        val rank: Int,
        val current_user: Boolean,
        val position: Int
)