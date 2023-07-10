package com.jedmahonisgroup.gamepoint.model.gameshow


import com.google.gson.annotations.SerializedName

data class SubmitGameShowResponseFromServer(
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val `data`: Data?
)