package com.jedmahonisgroup.gamepoint.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jedmahonisgroup.gamepoint.database.model.*

@Database(entities = [GameShowModel::class, UserDatabaseModel::class, DatabaseDealsModel::class, DatabaseStreakModel::class,
    DatabasePointsModel::class, DatabasePicksModel::class, DatabaseResultsModel::class, DatabaseEventModel::class, DatabaseFeedsModel::class], version = 12, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun dealsDao(): DealsDao
    abstract fun streakDao(): StreakDao
    abstract fun pointsDao(): PointsDao
    abstract fun picksDao(): PicksDao
    abstract fun resultsDao(): ResultsDao
    abstract fun eventsDao(): EventsDao
    abstract fun feedsDao(): FeedsDao
    abstract fun gameShowDao(): GameShowDao
}