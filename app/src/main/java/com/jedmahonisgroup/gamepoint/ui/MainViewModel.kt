package com.jedmahonisgroup.gamepoint.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.database.UserDao
import com.jedmahonisgroup.gamepoint.database.model.UserDatabaseModel
import com.jedmahonisgroup.gamepoint.model.Login
import com.jedmahonisgroup.gamepoint.model.RefreshTokenModel
import com.jedmahonisgroup.gamepoint.model.UserResponse
import com.jedmahonisgroup.gamepoint.model.UserResponseModel
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import com.jedmahonisgroup.gamepoint.ui.settings.SettingsViewModel
import com.jedmahonisgroup.gamepoint.utils.LogUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.joda.time.Seconds
import org.joda.time.format.DateTimeFormat
import retrofit2.HttpException
import javax.inject.Inject


class MainViewModel(private var userDao: UserDao) : BaseViewModel() {

    private lateinit var subscription: Disposable
    private var TAG: String = MainViewModel::class.java.simpleName

    @Inject
    lateinit var gamePointApi: GamePointApi
    lateinit var context: Context
//    val getUserFromServerSuccess: MutableLiveData<UserResponseModel> = MutableLiveData()
//    val getUserFromServerError: MutableLiveData<String> = MutableLiveData()

    val writeUserToDbSucess: MutableLiveData<UserResponse> = MutableLiveData()
    val writeUserToDbError: MutableLiveData<String> = MutableLiveData()

    val tokenRefreshsuccess: MutableLiveData<UserResponse> = MutableLiveData()
    val tokenRefresherror: MutableLiveData<String> = MutableLiveData()

    val getUserFromDbSuccess: MutableLiveData<UserResponse?> = MutableLiveData()
    val getUserFromDbError: MutableLiveData<String> = MutableLiveData()

    val sendPushTokenSuccess: MutableLiveData<Any> = MutableLiveData()
    val sendPushTokenError: MutableLiveData<String> = MutableLiveData()

    val points: MutableLiveData<Int> = MutableLiveData()
    val percentage: MutableLiveData<Int> = MutableLiveData()
    val elapsedTime: MutableLiveData<String> = MutableLiveData()
    val pointsThisCheckIn: MutableLiveData<Int> = MutableLiveData()

    val logoutSuccess: MutableLiveData<UserResponseModel> = MutableLiveData()
    val logoutFail: MutableLiveData<String> = MutableLiveData()

    val unregisterPushTokenSuccess: MutableLiveData<Any> = MutableLiveData()
    val unregisterPushTokenFail: MutableLiveData<String> = MutableLiveData()

    init {

    }

    override fun onCleared() {
        super.onCleared()
        try {
            subscription.dispose()
        } catch (e: Exception) {
            Log.e("JMG", "e onCleared MainViewModel: " + e.localizedMessage)
        }
    }

    @SuppressLint("CheckResult")
    private fun getUserDB() {
//            getUserFromDB()
//        subscription = Observable.fromCallable {userDao.getUserObjectFromDb.user }
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({
//                    onGetUserFromDbSuccess(it)},
//                        {
//                            onGetUserFromDbError(it) }
//                )
    }

    private fun onGetUserFromDbSuccess(it: UserResponse?) {
        Log.i(TAG, "getUserFromDbSuccess ===========> $it")

//        val gson = Gson()
//        val user: UserResponseModel = gson.fromJson(it, UserResponseModel::class.java)

//        Log.i("$TAG, TOKEN: ", user.user.login.token)

        getUserFromDbSuccess.value = it
    }

    private fun onGetUserFromDbError(it: String?) {
        Log.e(TAG, "onGetUserFromDbError2 ============= > ${it}")
        getUserFromDbError.value = it ?: ""
    }

    @SuppressLint("CheckResult")
    private fun UpdateuserToDb(user: UserResponse) {
//        Log.e("JMG", "dbUser: " + dbUser)
//        if (dbUser.user is String) {
//            Log.e("JMG", "dbUser is String: ")
//        }
//        Observable.fromCallable { userDao.updateUser(dbUser) }
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ onWriteUserToDbSuccess(it) },
//                        { onWriteUserToDbError(it) }
//                )
//        try {
            GamePointApplication.shared!!.setCurrentUser(user, context)
            onWriteUserToDbSuccess(user)
//        } catch (e: Exception) {
//            Log.e("JMG", "e: " + e.localizedMessage)
//            onWriteUserToDbError(e.localizedMessage)
//        }
    }

    private fun onWriteUserToDbSuccess(it: UserResponse) {
        Log.i(TAG, "onWriteUserToDbSuccess ========> $it")
        writeUserToDbSucess.value = it

    }

    private fun onWriteUserToDbError(it: String?) {
        Log.e(TAG, "onWriteUserFromDbError ===========> $it")
        writeUserToDbError.value = it ?: ""

    }

    private fun refreshToken(refreshToken: RefreshTokenModel, userId: String) {
        Log.e("JMG", "refreshToken: " + refreshToken + " userId: " + userId)
        subscription = gamePointApi.refreshToken(userId, refreshToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onRefreshTokenStart() }
                .doOnTerminate { onRefreshTokenFinish() }
                .subscribe({
                    onTokenRefreshedSuccess(it.user)
                }, { error ->
                    var message = "Something went wrong, please try again"
                    Log.e("JMG", "eerror: " + error)
                    if (error is HttpException) {
                        try {
                            val errorJsonString = error.response()?.errorBody()?.string()
                            Log.e("JMG", "errorJsonString: " + errorJsonString)
                            val arr  = JsonParser().parse(errorJsonString)
                                .asJsonObject["errors"]
                                .asJsonArray
                            if (arr.size() > 0) {
                                message = arr.get(0).toString()
                                onTokenRefreshError(message)
                            }
                        } catch (e: Exception) {
                            onTokenRefreshError(message)
                        }
                    } else {
                        error.message?.let {
                            onTokenRefreshError(it)
                        }
                    }
                })

    }

    private fun onRefreshTokenStart() {
        Log.i(TAG,"onRefreshTokenStart =========> refreshing token started")
    }

    private fun onRefreshTokenFinish() {
        Log.i(TAG,"onRefreshTokenFinish =========> finished refreshing token")
    }

    private fun onTokenRefreshedSuccess(it: UserResponse) {
        LogUtil.d("MainViewModel > onTokenRefreshedSuccess > 185: $it")
        tokenRefreshsuccess.value = it
        updateDatabaseWithUser(it)

    }

    private fun onTokenRefreshError(it: String) {
        Log.e(TAG,"onTokenRefreshError =========> $it")
        tokenRefresherror.value = it
    }

    private fun refreshUser(bearerToken: String, userId: String, loginModel: Login?) {
        subscription = gamePointApi.refreshUser("application/json", "Bearer $bearerToken", userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {onRefresUserStart()}
                .doOnTerminate { onRefreshUserFinish() }
                .subscribe({ t ->
                    onRefreshUserSuccess(t, loginModel)
                }, { error ->
                    var message = "Something went wrong, please try again"
                    if (error is HttpException) {
                        val errorJsonString = error.response()?.errorBody()?.string()
                        val arr  = JsonParser().parse(errorJsonString)
                                .asJsonObject["errors"]
                                .asJsonArray
                        if (arr.size() > 0) {
                            message = arr.get(0).toString()
                            onRefreshUserError(message)
                        }
                    } else {
                        error.message?.let {
                            onRefreshUserError(it)
                        }
                    }
                })
    }

    private fun onRefresUserStart() {
        Log.i(TAG, "onRefresUserStart")

    }

    private fun onRefreshUserFinish() {
        Log.i(TAG, "onRefreshUserFinish")
    }

    private fun onRefreshUserSuccess(t: UserResponse, loginModel: Login?) {
        LogUtil.d("MainViewModel > onRefreshUserSuccess > 232: ")
        val userModel = t
        userModel.login = loginModel!!
        updateDatabaseWithUser(userModel)
    }

    private fun onRefreshUserError(it: String) {
        LogUtil.d("MainViewModel > onRefreshUserError > 240: ")
    }

     fun sendPushTokenToServer(bearerToken: String, pushToken: String) {
         val pushBody = sendPushTokenToServerBody(pushToken, "Android")
         Log.e("JMG", "firebase_key: " + pushBody.firebase_key + " os: " + pushBody.os)
        subscription = gamePointApi.sendPushTokenToServer("application/json", "Bearer $bearerToken", pushBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {onSendPushTokenToServerStart()}
                .doOnTerminate { onSendPushTokenToServerFinish() }
                .subscribe({ t ->
                    onSendPushTokenToServerSuccess(t)
                }, { error ->
                    var message = "Something went wrong, please try again"
                    if (error is HttpException) {
                        val errorJsonString = error.response()?.errorBody()?.string()

                        try {
                            val arr = JsonParser().parse(errorJsonString)
                                    .asJsonObject["errors"]
                                    .asJsonArray
                            if (arr != null && arr.size() > 0) {
                                message = arr.get(0).toString()
                                onSendPushTokenToServerError(message)
                            }
                        } catch (e: Exception) {
                            error.message?.let {
                                onSendPushTokenToServerError(it)
                            }
                        }

                    } else {
                        error.message?.let {
                            onSendPushTokenToServerError(it)
                        }
                    }
                })
    }

    private fun onSendPushTokenToServerStart() {
        Log.i(TAG, "onRefresUserStart")

    }

    private fun onSendPushTokenToServerFinish() {
        Log.i(TAG, "onRefreshUserFinish")
    }

    private fun onSendPushTokenToServerSuccess(t: Any) {
        Log.i(TAG, "yaya! we sent the push token: $t")

        sendPushTokenSuccess.value = t
    }

    private fun onSendPushTokenToServerError(it: String) {
        Log.e(TAG, "onSendPushTokenToServerError: could not refresh user: $it")
         sendPushTokenError.value = it
    }

    /**
    *Methods to interface with the view
    */

    fun refresUser(token: String, userId: String, loginModel: Login?){
        refreshUser(token, userId, loginModel)
    }

    fun getUserFromDB() {
//        getUserDB()
        try {
            val user = GamePointApplication.shared!!.getCurrentUser(context)
            onGetUserFromDbSuccess(user)
        } catch (e: Exception) {
            onGetUserFromDbError(e.localizedMessage)
        }
    }

    fun getTokenRefresh(refreshToken: String, userId: String) {
        val refreshModel = RefreshTokenModel(
                token = refreshToken
        )

        refreshToken(refreshModel, userId)
    }

    fun updateDatabaseWithUser(user: UserResponse) {

//        val userString = Gson().toJson(user)
//        Log.e("JMG", "userString: " + userString)
//        val dbUser = UserDatabaseModel(
//                id = 1, user = userString
//        )
        UpdateuserToDb(user)
    }

    /**
     * Calculate points earned since checkIN
     */

    private fun minutesCheckedIn(eventDuration: Int, totalPoints: Int, checkInTimeStamp: String, currentTime: DateTime, eventStartTime: String, eventId: Int, previousMinutesCheckedIn: Int, sharedPreferences: SharedPreferences): Double {
        val secondsInAnMinute: Int = 60
        var elapsedMinuets: Double

      //  Log.e(TAG, "minutesCheckedIn ============> $checkInTimeStamp, current time $currentTime")

        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        val dtCheckIn = formatter.parseDateTime(checkInTimeStamp).toLocalDateTime()
        val dt = formatter.parseDateTime(eventStartTime).toLocalDateTime()

        val timeUserCheckedIn = DateTime(
                dtCheckIn.year,
                dtCheckIn.monthOfYear,
                dtCheckIn.dayOfMonth,
                dtCheckIn.hourOfDay,
                dtCheckIn.minuteOfHour,
                dtCheckIn.secondOfMinute
        )

        var eventStarts = DateTime(
                dt.year,
                dt.monthOfYear,
                dt.dayOfMonth,
                dt.hourOfDay,
                dt.minuteOfHour,
                dt.secondOfMinute
        )

        if (timeUserCheckedIn.isAfter(eventStarts)) {
            eventStarts = timeUserCheckedIn
        }


        val elapsedSeconds = Seconds.secondsBetween(eventStarts, currentTime).seconds

       // Log.e(TAG,"elapsedSeconds: $elapsedSeconds")

        elapsedMinuets = ((elapsedSeconds.toDouble() / secondsInAnMinute.toDouble()).toInt().toDouble()) + (previousMinutesCheckedIn).toDouble()
        val minsPlusOldMins = elapsedMinuets + (previousMinutesCheckedIn).toDouble()
//        Log.e(TAG, "Old Minuets Plus new: $minsPlusOldMins")
//
//        Log.e(TAG,"elapsedMinuets: $elapsedMinuets")

        Log.e(TAG, "elapsedTime.value: " + elapsedMinuets.toInt().toString())
        elapsedTime.value = elapsedMinuets.toInt().toString()
        val math = ((elapsedMinuets - previousMinutesCheckedIn) / eventDuration.toDouble()) * totalPoints.toDouble()
        Log.e(TAG, "math: " + math.toInt())
        pointsThisCheckIn.value = math.toInt()
        return elapsedMinuets
    }

    private fun percentageOfPointsEarned(eventDuration: Int, totalPoints: Int, checkInTimeStamp: String, currentTime: DateTime, eventStartTime: String, eventId: Int, previousMinutesCheckedIn: Int, sharedPreferences: SharedPreferences): Int {
//        Log.e(TAG,"percentageOfPointsEarned: ${(minutesCheckedIn(checkInTimeStamp, currentTime, eventStartTime, eventId, previousMinutesCheckedIn, sharedPreferences) / eventDuration.toDouble() * 100).toInt()}")
        var percentage = (minutesCheckedIn(eventDuration, totalPoints, checkInTimeStamp, currentTime, eventStartTime, eventId, previousMinutesCheckedIn, sharedPreferences) / eventDuration.toDouble() * 100).toInt()
        if (percentage <= 0) {
            percentage = 0
        } else if (percentage >= 100) {
            percentage = 100
        }
        return percentage
    }

    fun pointsEarned(eventDuration: Int, checkInTimeStamp: String, currentTime: DateTime, totalPoints: Int, eventStartTime: String, eventId: Int, previousMinutesCheckedIn: Int, sharedPreferences: SharedPreferences) {
        val percentPoints = percentageOfPointsEarned(eventDuration, totalPoints, checkInTimeStamp, currentTime, eventStartTime, eventId, previousMinutesCheckedIn, sharedPreferences)
        val earnedPoints = (percentageOfPointsEarned(eventDuration, totalPoints, checkInTimeStamp, currentTime, eventStartTime, eventId, previousMinutesCheckedIn, sharedPreferences).toDouble() / 100.0) * totalPoints.toDouble()

        Log.e(TAG, "percent is: $percentPoints")
        Log.e(TAG, "earned points is: $earnedPoints")

        points.value = earnedPoints.toInt()
        percentage.value = percentPoints

    }


    data class sendPushTokenToServerBody (
            val firebase_key: String,
            val os: String
    )

    private fun getLogout(bearerToken: String) {
        subscription = gamePointApi.logout("application/json", "Bearer $bearerToken")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onLogoutStart() }
            .doOnTerminate { onLogoutFinish() }
            .subscribe({ t ->
                onLogoutStartSuccess(t)
            }, { error ->
                var message = "Something went wrong, please try again"
                if (error is HttpException) {
                    val errorJsonString = error.response()?.errorBody()?.string()
                    val arr  = JsonParser().parse(errorJsonString)
                        .asJsonObject["errors"]
                        .asJsonArray
                    if (arr.size() > 0) {
                        message = arr.get(0).toString()
                        onLogoutStartError(message)
                    }
                } else {
                    error.message?.let {
                        onLogoutStartError(it)
                    }
                }
            })
    }

    private fun onLogoutStart() {
        Log.e(TAG, "onLogoutStart =============> starting to logout")
    }

    private fun onLogoutFinish() {
        Log.e(TAG, "onLogoutFinish =============> finished logout")
    }

    private fun onLogoutStartSuccess(t: UserResponseModel) {
        Log.e(TAG, "onLogoutStartSuccess =============>: logout $t")
        logoutSuccess.value = t
    }

    private fun onLogoutStartError(it: String) {
        Log.e(TAG, "onLogoutStartError =============>: problem logging user out $it")
        logoutFail.value = it
    }





    fun logout(token: String) {
        getLogout(token)
        //deleteDb(token) don't delete the db just yet, get the
    }

    fun unregisterPushToken(bearerToken: String, firebase_key: String) {
        Log.e("JMG", "firebase_key: " + firebase_key)
        subscription = gamePointApi.unregisterPushToken("application/json", "Bearer $bearerToken",
            SettingsViewModel.firebase_key_object(firebase_key)
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onUnregisterPushTokenStart() }
            .doOnTerminate { onUnregisterPushTokenFinish() }
            .subscribe({ t ->
                onUnregisterPushTokenSuccess(t)
            }, { error ->
                var message = "Something went wrong, please try again"
                if (error is HttpException) {
                    val errorJsonString = error.response()?.errorBody()?.string()
                    val arr  = JsonParser().parse(errorJsonString)
                        .asJsonObject["errors"]
                        .asJsonArray
                    if (arr.size() > 0) {
                        message = arr.get(0).toString()
                        onUnregisterPushTokenError(message)
                    }
                } else {
                    error.message?.let {
                        onUnregisterPushTokenError(it)
                    }
                }
            })
    }

    private fun onUnregisterPushTokenStart() {
        Log.e(TAG, "onLogoutStart =============> starting to logout")
    }

    private fun onUnregisterPushTokenFinish() {
        Log.e(TAG, "onLogoutFinish =============> finished logout")
    }

    private fun onUnregisterPushTokenSuccess(t: Any) {
        Log.e(TAG, "onLogoutStartSuccess =============>: logout $t")
        unregisterPushTokenSuccess.value = t
    }

    private fun onUnregisterPushTokenError(it: String) {
        Log.e(TAG, "onLogoutStartError =============>: problem logging user out $it")
        unregisterPushTokenFail.value = it
    }

}