package com.jedmahonisgroup.gamepoint.model

import com.jedmahonisgroup.gamepoint.model.picks.MyPicksModel
import com.jedmahonisgroup.gamepoint.model.picks.OpenPicksModel

data class PicksModel(
        val open_picks: List<OpenPicksModel>,
        val my_picks: List<MyPicksModel>
)