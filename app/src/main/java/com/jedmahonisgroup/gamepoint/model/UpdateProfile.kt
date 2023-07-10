package com.jedmahonisgroup.gamepoint.model


data class UpdateProfile(
        val user: UserUpdate
)

data class UserUpdate(
        val first_name: String,
        val last_name: String,
        val email: String,
        val birthday: String,
        val phone: String,
        val nickname: String,
        val sign_with_code: String,
        val school_id: String,
        val avatar: String
)
