package com.jedmahonisgroup.gamepoint.model.feeds

import com.google.gson.annotations.SerializedName
import com.jedmahonisgroup.gamepoint.model.RequestModel

data class PostsModel(
        val id: Int,
        val user_id: Int,
        val body: String,
        val private: Boolean,
        val like_count: Int,
        val venue_id: Int,
        val event_id: Int,
        val created_at: String,
        val updated_at: String,
        val image: String,
        val liked: Boolean,
        val followed: Boolean,
        val friend: Boolean,
        val comment_count: Int,
        var event: Event,
        var venue: Venue,
        var comments: List<Comments>,
        var user: User,
        val url: String
)

data class Event(
        val id: Int,
        val name: String,
        val venue_id: Int,
        val sport_id: Int,
        val team1: String,
        val team2: String,
        val point_value: Int,
        val minutes_to_redeem: Int,
        val start_time: String,
        val ticket_url: String,
        val hero_image: String,
        val url: String,
        var sport: Sports
)

data class Sports(
        val id: Int,
        val name: String,
        val icon: String,
        val map_pin: String
)

data class Venue(
        val id: Int,
        val name: String,
        val radius: Int,
        val latitude: String,
        val longitude: String,
        var address: Address,
        val url: String
)

data class Address(
        val id: Int,
        val street: String,
        val city: String,
        val state_id: Int,
        val zipcode: String,
        val business_id: String,
        val venue_id: Int,
        val latitude: String,
        val longitude: String,
        var state: State
)

data class State(
        val id: Int,
        val name: String,
        val abbrev: String
)

data class User(
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
             var friend_request: RequestModel,
             val avatar: String
)