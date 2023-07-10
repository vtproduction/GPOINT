package com.jedmahonisgroup.gamepoint.model

data class EventGeofenceModel(
        val id: Int,
        val radius: Int,
        val latitude: Double,
        val longitude: Double
)