package com.jedmahonisgroup.gamepoint.model.feeds

import com.jedmahonisgroup.gamepoint.model.Login

data class Comments(
        val id: Int,
        val post_id: Int,
        val body: String,
        val created_at: String,
        val updated_at: String,
        var user: UserComments,
        val url: String
)

data class UserComments(
        val id: Int,
        val first_name: String,
        val last_name: String,
        val email: String,
        val nickname: String,
        val sign_up_code: String,
        val sign_with_code: String,
        val private_posts: Boolean,
        val friend: Boolean,
        val friend_count: Int,
        var friend_request: FriendRequest,
        val avatar: String
)

data class FriendRequest(
        val id: Int
)