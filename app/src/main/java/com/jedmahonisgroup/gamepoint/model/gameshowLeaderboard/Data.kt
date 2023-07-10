package com.jedmahonisgroup.gamepoint.model.gameshowLeaderboard


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("title")
    val title: String,
    @SerializedName("users")
    val users: List<GameShowLeaderboardUser>?
)