package com.jedmahonisgroup.gamepoint.database

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jedmahonisgroup.gamepoint.database.model.DatabaseSchoolModel

interface SchoolDao {
    @get:Query("SELECT * FROM DatabaseSchoolModel")
    val getSchoolObjectFromDb: DatabaseSchoolModel


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSchoolObjectToDb( vararg response: DatabaseSchoolModel)
    // currently, we must put final before user variable or you will get error when compile

    @Update
    fun updateSchool(vararg  response: DatabaseSchoolModel)
}