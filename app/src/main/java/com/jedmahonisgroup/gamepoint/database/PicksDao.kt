package com.jedmahonisgroup.gamepoint.database

import androidx.room.*
import com.jedmahonisgroup.gamepoint.database.model.DatabasePicksModel

@Dao
interface PicksDao {
    @get:Query("SELECT * FROM picks")
    val getPicks: DatabasePicksModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inserPicks(vararg deals: DatabasePicksModel)
    // currently, we must put final before user variable or you will get error when compile

    @Update
    fun updatePicks(vararg deals: DatabasePicksModel)

}