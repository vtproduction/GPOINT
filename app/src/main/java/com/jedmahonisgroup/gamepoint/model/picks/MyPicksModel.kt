package com.jedmahonisgroup.gamepoint.model.picks

import com.jedmahonisgroup.gamepoint.model.events.Sport

data class MyPicksModel(
        val id: Int,
        val name: String,
        val description: String,
        val start_time: String,
        val team1: String,
        val team2: String,
        val point_value: Int,
        val winner: String,
        val my_pick: String,
        val closed: Boolean,
        val open: Boolean,
        val sport: Sport,
        val url: String
)