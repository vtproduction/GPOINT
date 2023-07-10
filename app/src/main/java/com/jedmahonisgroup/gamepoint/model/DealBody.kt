package com.jedmahonisgroup.gamepoint.model

data class DealBody(
        val user_id: Int,
        val deal_id: Int
)

data class RedeemDealBody(
        val deal_redeem: DealBody
)

//data class DealRedeemBody(
//        val user_id: Int,
//        val deal_id: Int
//)