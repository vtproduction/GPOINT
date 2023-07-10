package com.jedmahonisgroup.gamepoint.model

import com.jedmahonisgroup.gamepoint.model.picks.MyPicksModel
import com.jedmahonisgroup.gamepoint.model.picks.OpenPicksModel

data class SponsorModel(

    var id : Int,
    var name : String,
    var sponsor_url :  String,
    var description : String,
    var active : Boolean,
    var image :  String,
    var logo :  String,
    var url :  String

)