package com.jedmahonisgroup.gamepoint.database

import androidx.room.*
import com.jedmahonisgroup.gamepoint.database.model.DatabaseResultsModel

@Dao
interface ResultsDao {
    @get:Query("SELECT * FROM results")
    val getResults: DatabaseResultsModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertResults(vararg deals: DatabaseResultsModel)
    // currently, we must put final before user variable or you will get error when compile

    @Update
    fun updateResults(vararg deals: DatabaseResultsModel)

}