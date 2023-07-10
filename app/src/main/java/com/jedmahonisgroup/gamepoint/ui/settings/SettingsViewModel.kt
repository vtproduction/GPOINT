package com.jedmahonisgroup.gamepoint.ui.settings

import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.JsonParser
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.database.AppDatabase
import com.jedmahonisgroup.gamepoint.helpers.SharedPreferencesHelper
import com.jedmahonisgroup.gamepoint.model.UserResponseModel
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject


class SettingsViewModel(private val db: AppDatabase) : BaseViewModel() {
    private val TAG: String = SettingsViewModel::class.java.simpleName

    private var subscription: Disposable? = null

    @Inject lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    val logoutSuccess: MutableLiveData<UserResponseModel> = MutableLiveData()
    val logoutFail: MutableLiveData<String> = MutableLiveData()

    val unregisterPushTokenSuccess: MutableLiveData<Any> = MutableLiveData()
    val unregisterPushTokenFail: MutableLiveData<String> = MutableLiveData()

    init {

    }

    @Inject
    lateinit var gamePointApi: GamePointApi

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

    private fun deleteDb(token: String) {
        subscription = Observable.fromCallable {db.clearAllTables()}
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onDbDeleted(it.toString(), token) },
                        {
                            onDbDeletedError(it) }
                )
    }

    private fun onDbDeletedError(it: Throwable?) {
        Log.e(TAG, "==========> unable to delete db $it")
    }

    private fun onDbDeleted(s: String, token: String) {
       // getLogout(token)
        Log.e(TAG, "==========> deleteted user db $s")

    }

    fun logout(token: String) {
        sharedPreferencesHelper.clearJoinedGame()
        getLogout(token)
        //deleteDb(token) don't delete the db just yet, get the
    }

     fun unregisterPushToken(bearerToken: String, firebase_key: String) {
         Log.e("JMG", "firebase_key: " + firebase_key)
        subscription = gamePointApi.unregisterPushToken("application/json", "Bearer $bearerToken", firebase_key_object(firebase_key))
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

    data class firebase_key_object (
            val firebase_key: String
    )
}