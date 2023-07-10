package com.jedmahonisgroup.gamepoint.model

data class DealRedeemModel(
        val id: Int,
        val deal_id: Int,
        val user_id: Int,
        val point_value: Int,
        val url: String,
        val created_at: String
)