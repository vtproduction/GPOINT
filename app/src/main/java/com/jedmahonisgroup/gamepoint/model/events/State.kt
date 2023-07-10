package com.jedmahonisgroup.gamepoint.model.events

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class State(
        val id: Int,
        val name: String,
        val abbrev: String
        ) : Parcelable