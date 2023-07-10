package com.jedmahonisgroup.gamepoint.model


data class UpdateProfilePP(
        val user: UserUpdatePP
)

data class UserUpdatePP(
        val private_posts: Boolean

)
