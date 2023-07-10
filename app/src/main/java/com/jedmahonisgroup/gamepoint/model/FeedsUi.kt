package com.jedmahonisgroup.gamepoint.model

import com.jedmahonisgroup.gamepoint.model.feeds.Comments
import com.jedmahonisgroup.gamepoint.model.feeds.Event
import com.jedmahonisgroup.gamepoint.model.feeds.User
import com.jedmahonisgroup.gamepoint.model.feeds.Venue

data class FeedsUi(

        val id: Int,
        val body: String,
        var event: Event,
        var user: User,
        val image: String,
        val created_at: String,
        val comment_count: Int,
        var like_count: Int,
        var liked: Boolean






        )
