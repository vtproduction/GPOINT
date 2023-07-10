package com.jedmahonisgroup.gamepoint.model

import com.jedmahonisgroup.gamepoint.model.feeds.User


data class RequestModelUi(
        val id: Int? = null,
        val user_id: Int? = null,
        val friend_id: Int? = null,
        val approval: String? = null,
        val created_at: String? = null,
        val updated_at: String? = null,
        val user: User? = null,
        val friend: User? = null,
        val url: String? = null

        )





