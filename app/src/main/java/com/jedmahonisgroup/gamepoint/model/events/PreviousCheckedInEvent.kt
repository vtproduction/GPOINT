package com.jedmahonisgroup.gamepoint.model.events

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PreviousCheckedInEvent(
        val id: Int,
        val points_earned: String,
        val minutes_checked_in: Int

): Parcelable