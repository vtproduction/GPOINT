package com.jedmahonisgroup.gamepoint.model.gameshowLeaderboard


import com.google.gson.annotations.SerializedName

data class GameShowLeaderboardResponse(
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val `data`: Data
)