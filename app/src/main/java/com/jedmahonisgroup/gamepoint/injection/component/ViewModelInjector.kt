package com.jedmahonisgroup.gamepoint.injection.component

import com.jedmahonisgroup.gamepoint.GeofenceTransitionsIntentService
import com.jedmahonisgroup.gamepoint.GeofenceViewModel
import com.jedmahonisgroup.gamepoint.NetworkSchedulerService
import com.jedmahonisgroup.gamepoint.injection.module.AppModule
import com.jedmahonisgroup.gamepoint.injection.module.NetworkModule
import com.jedmahonisgroup.gamepoint.injection.module.SharedPreferencesModule
import com.jedmahonisgroup.gamepoint.ui.MainViewModel
import com.jedmahonisgroup.gamepoint.ui.UploadPictureViewModel
import com.jedmahonisgroup.gamepoint.ui.auth.AuthViewModel
import com.jedmahonisgroup.gamepoint.ui.auth.ForgotPasswordViewModel
import com.jedmahonisgroup.gamepoint.ui.auth.login.LoginViewModel
import com.jedmahonisgroup.gamepoint.ui.auth.school.SchoolViewModel
import com.jedmahonisgroup.gamepoint.ui.auth.signup.SignUpViewModel
import com.jedmahonisgroup.gamepoint.ui.deals.DealDetailViewModel
import com.jedmahonisgroup.gamepoint.ui.deals.DealsViewModel
import com.jedmahonisgroup.gamepoint.ui.events.EventDetailViewModel
import com.jedmahonisgroup.gamepoint.ui.events.EventViewModel
import com.jedmahonisgroup.gamepoint.ui.comments.CommentsViewModel
import com.jedmahonisgroup.gamepoint.ui.leaderboard.PointsLeaderBoardViewModel
import com.jedmahonisgroup.gamepoint.ui.leaderboard.StreakViewModel
import com.jedmahonisgroup.gamepoint.ui.picks.PicksViewModel
import com.jedmahonisgroup.gamepoint.ui.picks.ResultsViewModel
import com.jedmahonisgroup.gamepoint.ui.settings.EditProfileViewModel
import com.jedmahonisgroup.gamepoint.ui.settings.SettingsViewModel
import com.jedmahonisgroup.gamepoint.ui.feeds.FeedsViewModel
import com.jedmahonisgroup.gamepoint.ui.createpost.FeedsCreatPostViewModel
import com.jedmahonisgroup.gamepoint.ui.feeds.SocialViewModel
import com.jedmahonisgroup.gamepoint.ui.leaderboard.GameShowLeaderBoardViewModel
import com.jedmahonisgroup.gamepoint.ui.managfriends.ManageFriendsViewModel
import com.jedmahonisgroup.gamepoint.ui.notification.NotificationViewModel
import com.jedmahonisgroup.gamepoint.ui.pickEm.PickEmViewModel
import com.jedmahonisgroup.gamepoint.ui.points.PointDetailViewModel
import com.jedmahonisgroup.gamepoint.ui.settings.PrivacySettingViewModel

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class, SharedPreferencesModule::class, AppModule::class])
interface ViewModelInjector{
    /**
     * Injects required dependencies into the specified PostListViewModel.
     * @param authViewModel AuthViewModel in which to inject the dependencies
     */
    fun inject(authViewModel: AuthViewModel)

    /**
     * Injects required dependencies into the specified PostListViewModel.
     * @param signUpViewModel SignUpViewModel in which to inject the dependencies
     */
    fun inject(signUpViewModel: SignUpViewModel)

    /**
     * Injects required dependencies into the specified PostViewModel.
     * @param loginViewModel LoginViewModel in which to inject the dependencies
     */
    fun inject(loginViewModel: LoginViewModel)

    /**
     * Injects required dependencies into the specified PostViewModel.
     * @param eventViewModel EventViewModel in which to inject the dependencies
     */
    fun inject(eventViewModel: EventViewModel)

    /**
     * Injects required dependencies into the specified PostViewModel.
     * @param eventViewModel EventViewModel in which to inject the dependencies
     */
    fun inject(pickEmViewModel: PickEmViewModel)

    /**
     * Injects required dependencies into the specified PostViewModel.
     * @param eventViewModel EventViewModel in which to inject the dependencies
     */
    fun inject(pointDetailViewModel: PointDetailViewModel)

    /**
     * Injects required dependencies into the specified PostViewModel.
     * @param streakViewModel LoginViewModel in which to inject the dependencies
     */
    fun inject(streakViewModel: StreakViewModel)
    /**
     * Injects required dependencies into the specified PostViewModel.
     * @param dealsViewModel LoginViewModel in which to inject the dependencies
     */
    fun inject(dealsViewModel: DealsViewModel)
    /**
     * Injects required dependencies into the specified PostViewModel.
     * @param pointsLeaderBoardViewModel LoginViewModel in which to inject the dependencies
     */
    fun inject(pointsLeaderBoardViewModel: PointsLeaderBoardViewModel)
    /**
     * Injects required dependencies into the specified PostViewModel.
     * @param picksViewModel LoginViewModel in which to inject the dependencies
     */
    fun inject(picksViewModel: PicksViewModel)
    /**
     * Injects required dependencies into the specified PostViewModel.
     * @param resultsViewModel LoginViewModel in which to inject the dependencies
     */
    fun inject(resultsViewModel: ResultsViewModel)

    /**
     * Injects required dependencies into the specified PostViewModel.
     * @param mainViewModel LoginViewModel in which to inject the dependencies
     */
    fun inject(mainViewModel: MainViewModel)

    /**
     * Injects required dependencies into the specified PostViewModel.
     * @param eventDetailViewModel LoginViewModel in which to inject the dependencies
     */
    fun inject(eventDetailViewModel: EventDetailViewModel)

    /**
     * Injects required dependencies into the specified PostViewModel.
     * @param settingsViewModel LoginViewModel in which to inject the dependencies
     */
    fun inject(settingsViewModel: SettingsViewModel)

    /**
     * Injects required dependencies into the specified PostViewModel.
     * @param uploadPictureViewModel LoginViewModel in which to inject the dependencies
     */
    fun inject(uploadPictureViewModel: UploadPictureViewModel)

    /**
     * Injects required dependencies into the specified PostViewModel.
     * @param dealDetailViewModel LoginViewModel in which to inject the dependencies
     */
    fun inject(dealDetailViewModel: DealDetailViewModel)
    /**
     * Injects required dependencies into the specified PostViewModel.
     * @param editProfileViewModel LoginViewModel in which to inject the dependencies
     */
    fun inject(editProfileViewModel: EditProfileViewModel)

   /**
     * Injects required dependencies into the specified PostViewModel.
     * @param forgotPasswordViewModel LoginViewModel in which to inject the dependencies
     */
    fun inject(forgotPasswordViewModel: ForgotPasswordViewModel)

   /**
     * Injects required dependencies into the specified PostViewModel. for background services
     * @param geofenceViewModel LoginViewModel in which to inject the dependencies
     */
    fun inject(geofenceViewModel: GeofenceViewModel)

    /**
     * we use this class to make check in and check out requests while the app is in the background
     * @param geofenceTransitionsIntentService
     */
    fun inject(geofenceTransitionsIntentService: GeofenceTransitionsIntentService)
    /**
     * we use this class to make check in requests while the app is in the background
     * @param networkSchedulerService
     */
    fun inject(networkSchedulerService: NetworkSchedulerService)

    fun inject(feedsViewModel: FeedsViewModel)

    fun inject(feedsCreatePostViewModel: FeedsCreatPostViewModel)

    fun inject(manageFriendsViewModel: ManageFriendsViewModel)

    fun inject(commentsViewModel: CommentsViewModel)

    fun inject(notificationViewModel: NotificationViewModel)

    /**
     * Injects required dependencies into the specified PostViewModel.
     * @param editProfileViewModel LoginViewModel in which to inject the dependencies
     */
    fun inject(privacySettingViewModel: PrivacySettingViewModel)

    fun inject(schoolViewModel: SchoolViewModel)

    fun inject(socialViewModel: SocialViewModel)

    fun inject(gameShowLeaderBoardViewModel: GameShowLeaderBoardViewModel)


    @Component.Builder
    interface Builder {
        fun build(): ViewModelInjector
        fun appModule(appModule: AppModule): Builder
        fun networkModule(networkModule: NetworkModule): Builder
        fun sharedPrefModule(sharedPrefModule: SharedPreferencesModule) : Builder
    }
}