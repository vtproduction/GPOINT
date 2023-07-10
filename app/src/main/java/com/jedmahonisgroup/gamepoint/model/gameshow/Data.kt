package com.jedmahonisgroup.gamepoint.model.gameshow


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("message")
    val message: String?,
    @SerializedName("gameShowId")
    val gameShowId: Int,
    @SerializedName("joinedId")
    val joinedId: Int
)