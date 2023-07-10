package com.jedmahonisgroup.gamepoint.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "picks")
data class DatabasePicksModel(
        @field:PrimaryKey(autoGenerate = false)
        val id: Int,

        @field:ColumnInfo(name = "picks")
        var picks: String

)