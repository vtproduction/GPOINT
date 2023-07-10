package com.jedmahonisgroup.gamepoint.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.jedmahonisgroup.gamepoint.database.model.DatabaseDealsModel

@Dao
interface DealsDao {

    @get:Query("SELECT * FROM deals")
    val getDeals: DatabaseDealsModel

    @Insert(onConflict = REPLACE)
    fun insertUser( vararg deals: DatabaseDealsModel)
    // currently, we must put final before user variable or you will get error when compile

    @Update
    fun updateUser(vararg  deals: DatabaseDealsModel)


}