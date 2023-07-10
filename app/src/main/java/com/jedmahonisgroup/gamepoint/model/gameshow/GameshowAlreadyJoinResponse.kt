package com.jedmahonisgroup.gamepoint.model.gameshow


import com.google.gson.annotations.SerializedName


data class GameShowAlreadyJoinedResponse(
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("joinedId")
    val joinedId: Int,
    @SerializedName("gameShowId")
    val gameShowId: Int,
    @SerializedName("player1")
    val player1: GameShowPlayer?,
    @SerializedName("player2")
    val player2: GameShowPlayer?
)

