package com.jedmahonisgroup.gamepoint.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class DatabaseEventModel(
        @field:PrimaryKey(autoGenerate = false)
        val id: Int,

        @field:ColumnInfo(name = "events")
        var events: String

)