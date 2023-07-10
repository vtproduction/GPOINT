package com.jedmahonisgroup.gamepoint.database

import androidx.room.*
import com.jedmahonisgroup.gamepoint.database.model.DatabaseEventModel

@Dao
interface EventsDao {
    @get:Query("SELECT * FROM events")
    val getEvents: DatabaseEventModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEvents(vararg deals: DatabaseEventModel)
    // currently, we must put final before user variable or you will get error when compile

    @Update
    fun updateEvents(vararg deals: DatabaseEventModel)

}