package com.jedmahonisgroup.gamepoint.model

import com.jedmahonisgroup.gamepoint.model.events.Address

data class DealsUi(
        var deal: ArrayList<DealModel>?,
        val business: Business,
        val address: Address,
        val dealBusinessName: String,
        val distance: String?,
        var hero_image: String?,
        var numberOfDeals: Int,
        var activeDealCount: Int,
        val online: Boolean
)