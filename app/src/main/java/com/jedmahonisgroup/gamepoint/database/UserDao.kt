package com.jedmahonisgroup.gamepoint.database

import androidx.room.*
import com.jedmahonisgroup.gamepoint.database.model.UserDatabaseModel


@Dao
interface UserDao {

    @get:Query("SELECT * FROM UserDatabaseModel WHERE id = 1 LIMIT 1")
    val getUserObjectFromDb: UserDatabaseModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserObjectToDb( vararg response: UserDatabaseModel)
    // currently, we must put final before user variable or you will get error when compile

    @Update
    fun updateUser(vararg  response: UserDatabaseModel)


}
