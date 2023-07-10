package com.jedmahonisgroup.gamepoint.database

import androidx.room.*
import com.jedmahonisgroup.gamepoint.database.model.DatabaseStreakModel

@Dao
interface StreakDao {
    @get:Query("SELECT * FROM streak")
    val getStreak: DatabaseStreakModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStreak(vararg deals: DatabaseStreakModel)
    // currently, we must put final before user variable or you will get error when compile

    @Update
    fun updateStreak(vararg deals: DatabaseStreakModel)

}