package com.jedmahonisgroup.gamepoint.model

data class CurrentRedeemingDealModel(
        val redeemTimeStamp: String,
        val expireTimeStamp: String,
        val addressId: Int,
        val dealId: Int
)