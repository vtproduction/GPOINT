package com.jedmahonisgroup.gamepoint

import com.jedmahonisgroup.gamepoint.model.events.EventsModel

interface GamePointResultListener{
    fun onCardClicked(data: EventsModel)
}