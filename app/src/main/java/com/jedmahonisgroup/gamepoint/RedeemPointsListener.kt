package com.jedmahonisgroup.gamepoint

import androidx.recyclerview.widget.RecyclerView
import com.jedmahonisgroup.gamepoint.model.DealModel

interface RedeemPointsListener {
    fun onRedeemPointsClicked(deal: DealModel)
    fun onActiveDealClicked(deal: DealModel)
}