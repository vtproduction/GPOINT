package com.jedmahonisgroup.gamepoint.model.events

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Venue (

	val id : Int,
	val name : String,
	val radius: Double,
	val latitude: Double,
	val longitude: Double,
	val address : Address,
	val url : String
):Parcelable