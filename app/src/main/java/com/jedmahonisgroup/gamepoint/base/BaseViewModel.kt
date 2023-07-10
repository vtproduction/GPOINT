package com.jedmahonisgroup.gamepoint.base

import androidx.lifecycle.ViewModel
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.GeofenceViewModel
import com.jedmahonisgroup.gamepoint.injection.component.DaggerViewModelInjector
import com.jedmahonisgroup.gamepoint.injection.component.ViewModelInjector
import com.jedmahonisgroup.gamepoint.injection.module.AppModule
import com.jedmahonisgroup.gamepoint.injection.module.NetworkModule
import com.jedmahonisgroup.gamepoint.injection.module.SharedPreferencesModule
import com.jedmahonisgroup.gamepoint.ui.MainViewModel
import com.jedmahonisgroup.gamepoint.ui.UploadPictureViewModel
import com.jedmahonisgroup.gamepoint.ui.auth.AuthViewModel
import com.jedmahonisgroup.gamepoint.ui.auth.ForgotPasswordViewModel
import com.jedmahonisgroup.gamepoint.ui.auth.login.LoginViewModel
import com.jedmahonisgroup.gamepoint.ui.auth.signup.SignUpViewModel
import com.jedmahonisgroup.gamepoint.ui.deals.DealDetailViewModel
import com.jedmahonisgroup.gamepoint.ui.deals.DealsViewModel
import com.jedmahonisgroup.gamepoint.ui.events.EventDetailViewModel
import com.jedmahonisgroup.gamepoint.ui.events.EventViewModel
import com.jedmahonisgroup.gamepoint.ui.comments.CommentsViewModel
import com.jedmahonisgroup.gamepoint.ui.feeds.FeedsViewModel
import com.jedmahonisgroup.gamepoint.ui.createpost.FeedsCreatPostViewModel
import com.jedmahonisgroup.gamepoint.ui.feeds.SocialViewModel
import com.jedmahonisgroup.gamepoint.ui.leaderboard.GameShowLeaderBoardViewModel
import com.jedmahonisgroup.gamepoint.ui.managfriends.ManageFriendsViewModel
import com.jedmahonisgroup.gamepoint.ui.leaderboard.PointsLeaderBoardViewModel
import com.jedmahonisgroup.gamepoint.ui.leaderboard.StreakViewModel
import com.jedmahonisgroup.gamepoint.ui.notification.NotificationViewModel
import com.jedmahonisgroup.gamepoint.ui.pickEm.PickEmViewModel
import com.jedmahonisgroup.gamepoint.ui.picks.PicksViewModel
import com.jedmahonisgroup.gamepoint.ui.picks.ResultsViewModel
import com.jedmahonisgroup.gamepoint.ui.points.PointDetailViewModel
import com.jedmahonisgroup.gamepoint.ui.settings.EditProfileViewModel
import com.jedmahonisgroup.gamepoint.ui.settings.PrivacySettingViewModel
import com.jedmahonisgroup.gamepoint.ui.settings.SettingsViewModel

abstract class BaseViewModel : ViewModel() {
    private val injector: ViewModelInjector = GamePointApplication.shared!!.getViewModelInjector()

    init {
        inject()
    }

    /**
     * Injects the required dependencies
     */
    private fun inject() {
        when (this) {
            is AuthViewModel -> injector.inject(this)
            is SignUpViewModel -> injector.inject(this)
            is LoginViewModel -> injector.inject(this)
            is EventViewModel -> injector.inject(this)
            is PointDetailViewModel -> injector.inject(this)
            is DealsViewModel -> injector.inject(this)
            is StreakViewModel -> injector.inject(this)
            is PointsLeaderBoardViewModel -> injector.inject(this)
            is PicksViewModel -> injector.inject(this)
            is PickEmViewModel -> injector.inject(this)
            is ResultsViewModel -> injector.inject(this)
            is MainViewModel -> injector.inject(this)
            is EventDetailViewModel -> injector.inject(this)
            is SettingsViewModel -> injector.inject(this)
            is UploadPictureViewModel -> injector.inject(this)
            is DealDetailViewModel -> injector.inject(this)
            is GameShowLeaderBoardViewModel -> injector.inject(this)
            is EditProfileViewModel -> injector.inject(this)
            is ForgotPasswordViewModel -> injector.inject(this)
            is GeofenceViewModel -> injector.inject(this)
            is FeedsViewModel -> injector.inject(this)
            is FeedsCreatPostViewModel -> injector.inject(this)
            is ManageFriendsViewModel -> injector.inject(this)
            is CommentsViewModel -> injector.inject(this)
            is NotificationViewModel -> injector.inject(this)
            is PrivacySettingViewModel -> injector.inject(this)
            is SocialViewModel -> injector.inject(this)



        }
    }
}