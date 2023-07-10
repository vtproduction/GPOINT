package com.jedmahonisgroup.gamepoint.model.gameshow


import com.google.gson.annotations.SerializedName

data class GameShowResult(
    @SerializedName("id")
    val id: Int,
    @SerializedName("gameShowId")
    val gameShowId: Int,
    @SerializedName("player1Id")
    val player1Id: Int?,
    @SerializedName("player2Id")
    val player2Id: Int?,
    @SerializedName("player1")
    val player1: GameShowPlayer?,
    @SerializedName("player2")
    val player2: GameShowPlayer?,
    @SerializedName("player1Answer")
    val player1Answer: String?,
    @SerializedName("player2Answer")
    val player2Answer: String?,
    @SerializedName("player1GameBreaker")
    val player1GameBreaker: Int?,
    @SerializedName("player2GameBreaker")
    val player2GameBreaker: Int?,
    @SerializedName("wager")
    val wager: Int?,
    @SerializedName("player1Score")
    val player1Score: Int?,
    @SerializedName("player2Score")
    val player2Score: Int?,
    @SerializedName("player1AnswerTime")
    val player1AnswerTime: String?,
    @SerializedName("player2AnswerTime")
    val player2AnswerTime: String?,
    @SerializedName("isFinished")
    val isFinished: Boolean?,
    @SerializedName("finishedTime")
    val finishedTime: Boolean?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?
)