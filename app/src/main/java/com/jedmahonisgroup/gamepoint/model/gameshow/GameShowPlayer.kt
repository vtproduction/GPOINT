package com.jedmahonisgroup.gamepoint.model.gameshow

import com.google.gson.annotations.SerializedName

/**
 * Created by nienle on 14,January,2023
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
data class GameShowPlayer (
    @SerializedName("playerId")
    val playerId: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("avatar")
    val avatar: String?
    )