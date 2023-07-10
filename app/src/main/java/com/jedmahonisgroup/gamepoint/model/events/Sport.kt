package com.jedmahonisgroup.gamepoint.model.events

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Sport (

	val id : Int,
	val name : String,
	val icon : String?,
	val map_pin: String?

):Parcelable