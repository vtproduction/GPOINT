package com.jedmahonisgroup.gamepoint.model.leaderboard

data class LeaderboardModel(
        val pick_leaderboard: List<StreakModel>,
        val event_leaderboard: List<EventLeaderboardModel>
)