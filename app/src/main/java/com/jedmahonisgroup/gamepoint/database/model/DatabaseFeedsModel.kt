package com.jedmahonisgroup.gamepoint.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feeds")
data class DatabaseFeedsModel(
        @field:PrimaryKey(autoGenerate = false)
        val id: Int,

        @field:ColumnInfo(name = "feeds")
        var feeds: String

)