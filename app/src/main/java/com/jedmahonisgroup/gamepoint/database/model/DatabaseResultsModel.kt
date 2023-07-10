package com.jedmahonisgroup.gamepoint.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "results")
data class DatabaseResultsModel(
        @field:PrimaryKey(autoGenerate = false)
        val id: Int,

        @field:ColumnInfo(name = "results")
        var results: String

)