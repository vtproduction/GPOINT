package com.jedmahonisgroup.gamepoint.model.picks

import java.io.Serializable


data class Picks(
        val user_pick: UserPick
)

data class UserPick(
        val user_id: Int,
        val pick_id: Int,
        val team: Int
//        val points_awarded: Int,
//        val team_name: String,
//        val url: String
) : Serializable