package com.jedmahonisgroup.gamepoint.model.gameshow

import com.google.gson.annotations.SerializedName

/**
 * Created by nienle on 16,January,2023
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
data class JoinGameShowResponse(
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("joinedId")
    val joinedId: Int,
    @SerializedName("gameShowId")
    val gameShowId: Int,
    @SerializedName("player1")
    val player1: GameShowPlayer?,
    @SerializedName("player2")
    val player2: GameShowPlayer?,

    @SerializedName("wager") var wager: Int? = -1
)
