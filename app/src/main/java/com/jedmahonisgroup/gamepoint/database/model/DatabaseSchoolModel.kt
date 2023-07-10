package com.jedmahonisgroup.gamepoint.database.model

import androidx.room.Entity

@Entity(tableName = "DatabaseSchoolModel")
    data class DatabaseSchoolModel(
        @field:androidx.room.PrimaryKey(autoGenerate = true)
        val id: Int,

        @field:androidx.room.ColumnInfo(name = "school")
            var response: String
    )
