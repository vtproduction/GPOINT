package com.jedmahonisgroup.gamepoint.model.gameshow

import com.google.gson.annotations.SerializedName

/**
 * Created by nienle on 05,February,2023
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
data class JoinGameShowResponseFromServer(
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: JoinGameShowResponse
)
