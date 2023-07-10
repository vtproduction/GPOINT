package com.jedmahonisgroup.gamepoint.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jedmahonisgroup.gamepoint.model.UserResponseModel


@Entity(tableName = "UserDatabaseModel")
data class UserDatabaseModel(
        @field:PrimaryKey(autoGenerate = true)
        val id: Int,

        @field:ColumnInfo(name = "user")
        var user: String

)
