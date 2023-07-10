package com.jedmahonisgroup.gamepoint.database

import androidx.room.*
import com.jedmahonisgroup.gamepoint.database.model.DatabasePointsModel

@Dao
interface PointsDao {
    @get:Query("SELECT * FROM points")
    val getPoints: DatabasePointsModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPoints(vararg deals: DatabasePointsModel)
    // currently, we must put final before user variable or you will get error when compile

    @Update
    fun updatePoints(vararg deals: DatabasePointsModel)

}