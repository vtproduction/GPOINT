package com.jedmahonisgroup.gamepoint.model.gameshow


import com.google.gson.annotations.SerializedName



data class GameShowJoinResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("gameShowId")
    val gameShowId: Int,
    @SerializedName("player1Id")
    val player1Id: Int,
    @SerializedName("player2Id")
    val player2Id: Int,
    @SerializedName("player1Answer")
    val player1Answer: Any?,
    @SerializedName("player2Answer")
    val player2Answer: Any?,
    @SerializedName("player1GameBreaker")
    val player1GameBreaker: Any?,
    @SerializedName("player2GameBreaker")
    val player2GameBreaker: Any?,
    @SerializedName("wager")
    val wager: Int,
    @SerializedName("player1Score")
    val player1Score: Any?,
    @SerializedName("player2Score")
    val player2Score: Any?,
    @SerializedName("player1AnswerTime")
    val player1AnswerTime: Any?,
    @SerializedName("player2AnswerTime")
    val player2AnswerTime: Any?,
    @SerializedName("isFinished")
    val isFinished: Boolean,
    @SerializedName("finishedTime")
    val finishedTime: Any?,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)