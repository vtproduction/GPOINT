package com.jedmahonisgroup.gamepoint.model

data class UserModel(
        val user: User
)

data class User(
        val first_name: String,
        val last_name: String,
        val email: String,
        val birthday: String,
        val phone: String,
        val nickname: String,
        val sign_with_code: String,
        val password: String,
//        val password_confirmation: String,
        val school_id: Int
)