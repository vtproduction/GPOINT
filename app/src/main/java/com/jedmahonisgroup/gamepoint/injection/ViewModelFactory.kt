package com.jedmahonisgroup.gamepoint.injection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import androidx.appcompat.app.AppCompatActivity
import com.jedmahonisgroup.gamepoint.database.AppDatabase
import com.jedmahonisgroup.gamepoint.helpers.SharedPreferencesHelper
import com.jedmahonisgroup.gamepoint.ui.MainViewModel
import com.jedmahonisgroup.gamepoint.ui.UploadPictureViewModel
import com.jedmahonisgroup.gamepoint.ui.auth.AuthViewModel
import com.jedmahonisgroup.gamepoint.ui.auth.login.LoginViewModel
import com.jedmahonisgroup.gamepoint.ui.auth.school.SchoolViewModel
import com.jedmahonisgroup.gamepoint.ui.auth.signup.SignUpViewModel
import com.jedmahonisgroup.gamepoint.ui.deals.DealsViewModel
import com.jedmahonisgroup.gamepoint.ui.events.EventDetailViewModel
import com.jedmahonisgroup.gamepoint.ui.events.EventViewModel
import com.jedmahonisgroup.gamepoint.ui.createpost.FeedsCreatPostViewModel
import com.jedmahonisgroup.gamepoint.ui.leaderboard.PointsLeaderBoardViewModel
import com.jedmahonisgroup.gamepoint.ui.leaderboard.StreakViewModel
import com.jedmahonisgroup.gamepoint.ui.picks.PicksViewModel
import com.jedmahonisgroup.gamepoint.ui.picks.ResultsViewModel
import com.jedmahonisgroup.gamepoint.ui.settings.EditProfileViewModel
import com.jedmahonisgroup.gamepoint.ui.settings.SettingsViewModel
import com.jedmahonisgroup.gamepoint.ui.feeds.FeedsViewModel
import com.jedmahonisgroup.gamepoint.ui.feeds.SocialViewModel
import com.jedmahonisgroup.gamepoint.ui.leaderboard.GameShowLeaderBoardViewModel
import com.jedmahonisgroup.gamepoint.ui.pickEm.PickEmViewModel
import com.jedmahonisgroup.gamepoint.ui.points.PointDetailViewModel
import com.jedmahonisgroup.gamepoint.ui.settings.PrivacySettingViewModel

class ViewModelFactory(private val activity: AppCompatActivity): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        when {modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "gamepoint")
                        .fallbackToDestructiveMigration()
                        .build()
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(db.userDao(), db) as T
            }
            /**
             * MainViewModel
             * @param db.userDao
             **/
            modelClass.isAssignableFrom(MainViewModel::class.java)-> {
                val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "gamepoint")
                    .fallbackToDestructiveMigration()
                    .build()
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(db.userDao()) as T

            }

            /**
             * GameShowViewModel
             * @param db.userDao
             * @param db.gameShowDao
             **/
            modelClass.isAssignableFrom(PickEmViewModel::class.java)-> {
                val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "gamepoint")
                    .fallbackToDestructiveMigration()
                    .build()
                @Suppress("UNCHECKED_CAST")
                return PickEmViewModel(db.userDao(), db.gameShowDao()) as T

            }

            /**
             * EventDetailViewModel
             * @param db.userDao
             **/
            modelClass.isAssignableFrom(EventDetailViewModel::class.java) -> {
                val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "gamepoint")
                        .fallbackToDestructiveMigration()
                        .build()
                @Suppress("UNCHECKED_CAST")
                return EventDetailViewModel(db.userDao()) as T
            }
            /**
             * AuthViewModel
             * @param db.userDao
             **/
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->{
                val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "gamepoint")
                        .fallbackToDestructiveMigration()
                        .build()
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(db.userDao()) as T
            }
            /**
             * SignUpViewModel
             * @param db.userDao
             * @param db
             **/
            modelClass.isAssignableFrom(SignUpViewModel::class.java) ->{
                val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "gamepoint")
                        .fallbackToDestructiveMigration()
                        .build()
                @Suppress("UNCHECKED_CAST")
                return SignUpViewModel(db.userDao(), db) as T
            }
            /**
             * DealsViewModel
             * @param db.dealsDao
             **/
            modelClass.isAssignableFrom(DealsViewModel::class.java) ->{
                val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "gamepoint")
                        .fallbackToDestructiveMigration()
                        .build()
                @Suppress("UNCHECKED_CAST")
                return DealsViewModel(db.dealsDao()) as T
            }
            /**
             * SettingsViewModel
             * @param db
             **/
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->{
                val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "gamepoint")
                        .fallbackToDestructiveMigration()
                        .build()
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(db) as T
            }
            /**
             * StreakViewModel
             * @param db.streakDao
             **/
            modelClass.isAssignableFrom(StreakViewModel::class.java) ->{
                val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "gamepoint")
                        .fallbackToDestructiveMigration()
                        .build()
                @Suppress("UNCHECKED_CAST")
                return StreakViewModel(db.streakDao()) as T
            }
            /**
             * PointsLeaderBoardViewModel
             * @param db.pointsDao
             **/
            modelClass.isAssignableFrom(PointsLeaderBoardViewModel::class.java) ->{
                val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "gamepoint")
                        .fallbackToDestructiveMigration()
                        .build()
                @Suppress("UNCHECKED_CAST")
                return PointsLeaderBoardViewModel(db.pointsDao()) as T
            }
            /**
             * PicksViewModel
             * @param db.picksDao
             **/
            modelClass.isAssignableFrom(PicksViewModel::class.java) ->{
                val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "gamepoint")
                        .fallbackToDestructiveMigration()
                        .build()
                @Suppress("UNCHECKED_CAST")
                return PicksViewModel(db.picksDao()) as T
            }

            /**
             * ResultsViewModel
             * @param db.resultsDao
             **/
            modelClass.isAssignableFrom(ResultsViewModel::class.java) ->{
                val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "gamepoint")
                        .fallbackToDestructiveMigration()
                        .build()
                @Suppress("UNCHECKED_CAST")
                return ResultsViewModel(db.resultsDao()) as T
            }

            /**
             * EventViewModel
             * @param db.eventsDao
             **/
            modelClass.isAssignableFrom(EventViewModel::class.java) ->{
                val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "gamepoint")
                    .fallbackToDestructiveMigration()
                    .build()
                @Suppress("UNCHECKED_CAST")
                return EventViewModel(db.eventsDao()) as T
            }

            /**
             * PointDetailViewModel
             * @param db.eventsDao
             **/
            modelClass.isAssignableFrom(PointDetailViewModel::class.java) ->{
                val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "gamepoint")
                    .fallbackToDestructiveMigration()
                    .build()
                @Suppress("UNCHECKED_CAST")
                return PointDetailViewModel(db.userDao(), activity.applicationContext) as T
            }

            /**
             * EditProfileViewModel
             * @param db.userDao
             **/
            modelClass.isAssignableFrom(EditProfileViewModel::class.java) ->{
                val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "gamepoint")
                        .fallbackToDestructiveMigration()
                        .build()
                @Suppress("UNCHECKED_CAST")
                return EditProfileViewModel(db.userDao()) as T
            }

            /**
             * UploadPictureViewModel
             * @param db.userDao
             **/
            modelClass.isAssignableFrom(UploadPictureViewModel::class.java) ->{
                val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "gamepoint")
                        .fallbackToDestructiveMigration()
                        .build()
                @Suppress("UNCHECKED_CAST")
                return UploadPictureViewModel(db.userDao()) as T
            }

            /**
             * FeedsViewModel
             * @param db.picksDao
             **/
            modelClass.isAssignableFrom(FeedsViewModel::class.java) ->{
                val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "gamepoint")
                        .fallbackToDestructiveMigration()
                        .build()
                @Suppress("UNCHECKED_CAST")
                return FeedsViewModel(db.feedsDao()) as T
                //
            }

            modelClass.isAssignableFrom(FeedsCreatPostViewModel::class.java) ->{
                val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "gamepoint")
                    .fallbackToDestructiveMigration()
                    .build()
                @Suppress("UNCHECKED_CAST")
                return FeedsCreatPostViewModel() as T
            }


            modelClass.isAssignableFrom(GameShowLeaderBoardViewModel::class.java) ->{

                @Suppress("UNCHECKED_CAST")
                return GameShowLeaderBoardViewModel() as T
            }

            /**
             * PrivacySettingViewModel
             * @param db.userDao
             **/
            modelClass.isAssignableFrom(PrivacySettingViewModel::class.java) ->{
                val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "gamepoint")
                        .fallbackToDestructiveMigration()
                        .build()
                @Suppress("UNCHECKED_CAST")
                return PrivacySettingViewModel(db.userDao()) as T
            }

            modelClass.isAssignableFrom(SchoolViewModel::class.java) ->{
                val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "gamepoint")
                    .fallbackToDestructiveMigration()
                    .build()
                @Suppress("UNCHECKED_CAST")
                return SchoolViewModel() as T
            }

            modelClass.isAssignableFrom(SocialViewModel::class.java) ->{
                val db = Room.databaseBuilder(activity.applicationContext, AppDatabase::class.java, "gamepoint")
                    .fallbackToDestructiveMigration()
                    .build()
                @Suppress("UNCHECKED_CAST")
                return SocialViewModel() as T

            }

        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}