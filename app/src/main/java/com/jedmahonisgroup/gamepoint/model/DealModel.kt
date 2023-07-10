package com.jedmahonisgroup.gamepoint.model

import com.jedmahonisgroup.gamepoint.model.events.Address

data class DealModel(
        val id: Int,
        val name: String,
        val business_id: Int,
        val point_value: Int,
        val description: String,
        val hero_image: String,
        val start_date: String,
        val end_date: String,
        val coupon_code: String,
        val businesses: Business,
        var isBeingRedeemed:Boolean = false,
        val url: String,
        val subtitle: String


)

data class Business(
        val id: Int,
        val name: String,
        val active: Boolean,
        val phone: String,
        val website: String,
        val image: String,
        val logo: String,
        val active_deals: String,
        val addresses: List<Address>,
        val url: String

)