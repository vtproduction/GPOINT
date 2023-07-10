package com.jedmahonisgroup.gamepoint.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "points")
data class DatabasePointsModel(
        @field:PrimaryKey(autoGenerate = false)
        val id: Int,

        @field:ColumnInfo(name = "points")
        var points: String

)