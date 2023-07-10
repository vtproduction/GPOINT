package com.jedmahonisgroup.gamepoint.model


data class Post(
        val post: UserPost
)

data class UserPost(
        val body: String,
        val image: String,
        val venue_id: Int? = null,
        val event_id: Int? = null


)