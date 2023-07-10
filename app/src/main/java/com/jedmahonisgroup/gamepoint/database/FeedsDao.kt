package com.jedmahonisgroup.gamepoint.database

import androidx.room.*
import com.jedmahonisgroup.gamepoint.database.model.DatabaseFeedsModel

@Dao
interface FeedsDao {
    @get:Query("SELECT * FROM feeds")
    val getFeeds: DatabaseFeedsModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inserFeeds(vararg feeds: DatabaseFeedsModel)
    // currently, we must put final before user variable or you will get error when compile

    @Update
    fun updateFeeds(vararg feeds: DatabaseFeedsModel)

}