package com.jedmahonisgroup.gamepoint.database

import androidx.room.*
import com.jedmahonisgroup.gamepoint.database.model.GameShowModel
import com.jedmahonisgroup.gamepoint.database.model.UserDatabaseModel


@Dao
interface GameShowDao {

    @get:Query("SELECT * FROM gameShow WHERE id = 1 LIMIT 1")
    val getUserObjectFromDb: GameShowModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGameShowObjectToDb( vararg response: GameShowModel)
    // currently, we must put final before user variable or you will get error when compile

    @Update
    fun updateGameShow(vararg  response: UserDatabaseModel)


}
