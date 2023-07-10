package com.jedmahonisgroup.gamepoint.model

import android.provider.ContactsContract
import com.jedmahonisgroup.gamepoint.model.feeds.FriendRequest
import com.jedmahonisgroup.gamepoint.model.picks.UserPick
import com.jedmahonisgroup.gamepoint.model.school.School
import java.io.Serializable

data class UserResponseModel(
        val user: UserResponse
)
data class UserResponse (
        val id: Int,
        val first_name: String,
        val last_name: String,
        val email: String,
        val avatar: String,
        val birthday: String,
        val phone: String?,
        val sign_with_code: String,
        val private_posts: Boolean,
        val total_points: String,
        val current_streak: String,
        val pick_rank: String,
        val highest_streak: String,
        val this_month_event_points: String,
        val event_rank: String,
        val nickname: String,
        val redeemable: String,
        val user_picks: List<UserPick>,
        val check_ins: List<CheckIn>,
        val url: String,
        val sign_up_code: String,
        var login: Login,
        var friend_request: RequestModel?,
        var streaks_prize: StreakPrize?,
        var points_prize: PointsPrize?,
        val school: School

) : Serializable

data class Login(
        val refresh_token: String,
        val expires: String,
        val token: String
) : Serializable

data class StreakPrize(
        val title: String,
        val prize_url: String,
        val description: String,
        val image: String,
        val url: String,
        val id: Int
) : Serializable

data class PointsPrize(
        val title: String,
        val prize_url: String,
        val description: String,
        val image: String,
        val url: String,
        val id: Int
) : Serializable