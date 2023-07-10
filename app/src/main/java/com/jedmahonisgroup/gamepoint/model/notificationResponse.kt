package com.jedmahonisgroup.gamepoint.model

import com.jedmahonisgroup.gamepoint.model.picks.UserPick

data class notificationResponseModel(
       val id: Int,
       val title: String,
val body: String,
val sent: Boolean,
val sent_at: String,
val sent_success: Int,
val push_type: String,
val url: String

)
