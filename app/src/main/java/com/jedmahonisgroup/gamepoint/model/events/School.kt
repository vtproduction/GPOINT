package com.jedmahonisgroup.gamepoint.model.events


import com.google.gson.annotations.SerializedName

data class School(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("short_name")
    val shortName: String,
    @SerializedName("primary_color")
    val primaryColor: String,
    @SerializedName("secondary_color")
    val secondaryColor: String,
    @SerializedName("dark_primary_color")
    val darkPrimaryColor: String,
    @SerializedName("dark_secondary_color")
    val darkSecondaryColor: String,
    @SerializedName("checkin_multiplier")
    val checkinMultiplier: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("logo")
    val logo: String?,
    @SerializedName("url")
    val url: String
)