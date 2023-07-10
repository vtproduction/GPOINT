package com.jedmahonisgroup.gamepoint.model.gameshow

import com.google.gson.annotations.SerializedName

/**
 * Created by nienle on 18,January,2023
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
data class GameShowResponse2(
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<GameShowResponse>
)
