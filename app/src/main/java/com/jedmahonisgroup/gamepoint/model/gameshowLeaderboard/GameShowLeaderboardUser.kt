package com.jedmahonisgroup.gamepoint.model.gameshowLeaderboard


import com.google.gson.annotations.SerializedName

data class GameShowLeaderboardUser(
    @SerializedName("name")
    val name: String,
    @SerializedName("avatar")
    val avatar: String,
    @SerializedName("points")
    val points: Int
)