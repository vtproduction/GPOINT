package com.jedmahonisgroup.gamepoint.model.events

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Address (

	val id : Int,
	val street : String,
	val city : String,
	val state_id : Int,
	val zipcode : String,
	val business_id : String?,
	val venue_id : Int,
	val latitude : Double,
	val longitude : Double,
	val state: State


):Parcelable