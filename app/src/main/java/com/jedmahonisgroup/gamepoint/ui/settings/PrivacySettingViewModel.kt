package com.jedmahonisgroup.gamepoint.ui.settings

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.database.UserDao
import com.jedmahonisgroup.gamepoint.database.model.UserDatabaseModel
import com.jedmahonisgroup.gamepoint.model.UpdateProfile
import com.jedmahonisgroup.gamepoint.model.UpdateProfilePP
import com.jedmahonisgroup.gamepoint.model.UserResponse
import com.jedmahonisgroup.gamepoint.model.UserResponseModel
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class PrivacySettingViewModel(private var userDao: UserDao) : BaseViewModel() {
    private var subscription: Disposable? = null
    private var TAG: String = PrivacySettingViewModel::class.java.simpleName

    @Inject
    lateinit var gamePointApi: GamePointApi

    init {
    }

    val userFromServerSuccess: MutableLiveData<UserResponse> = MutableLiveData()



    val userFromDbSuccess: MutableLiveData<UserResponseModel> = MutableLiveData()
    val userFromDbError: MutableLiveData<String> = MutableLiveData()

    //update profile
    val successUpdateingProfile: MutableLiveData<UserResponse> = MutableLiveData()
    val errorUpdateingProfile: MutableLiveData<String> = MutableLiveData()

    val startRequest: MutableLiveData<String> = MutableLiveData()

    @SuppressLint("CheckResult")
    private fun getUserDB() {
        subscription = Observable.fromCallable { userDao.getUserObjectFromDb.user }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onGetUserFromDbSuccess(it)
                },
                        {
                            onGetUserFromDbError(it)
                        }
                )
    }


    @SuppressLint("CheckResult")
    private fun getuserFromServer(bearerToken : String , userId : String ) {
        subscription = gamePointApi.getUser("application/json", "Bearer $bearerToken", userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onGetDataStart() }
                .doOnTerminate { onGetDataFinish() }
                .subscribe({ t ->
                    onResponsefromServerSuccess(t)
                }, { error ->
                    var message = "Something went wrong, please try again"
                    if (error is HttpException) {
                        val errorJsonString = error.response()?.errorBody()?.string()
                        val arr  = JsonParser().parse(errorJsonString)
                                .asJsonObject["errors"]
                                .asJsonArray
                        if (arr.size() > 0) {
                            message = arr.get(0).toString()
                            onGetDataError(message)
                        }
                    } else {
                        error.message?.let {
                            onGetDataError(it)
                        }
                    }
                })
    }

    private fun onGetUserFromDbSuccess(it: String) {
        Log.e(TAG, "getUserFromDbSuccess ===========> $it")

        val gson = Gson()
        val user: UserResponseModel = gson.fromJson(it, UserResponseModel::class.java)

        Log.e("$TAG, TOKEN: ", user.user.login.token)

        userFromDbSuccess.value = user
    }

    private fun onGetUserFromDbError(it: Throwable?) {
        Log.e(TAG, "onGetUserFromDbError ============= > ${it.toString()}")
        userFromDbError.value = it.toString()
    }

    /**
     * PATCH user
     */

    private fun updateProfile(bearerToken: String, userId: String, update: UpdateProfilePP) {
        subscription = gamePointApi.updateProfilePP("application/json", "Bearer $bearerToken", userId, update)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onGetDataStart() }
                .doOnTerminate { onGetDataFinish() }
                .subscribe({ t ->
                    onResponseSuccess(t)
                }, { error ->
                    var message = "Something went wrong, please try again"
                    if (error is HttpException) {
                        val errorJsonString = error.response()?.errorBody()?.string()
                        val arr  = JsonParser().parse(errorJsonString)
                                .asJsonObject["errors"]
                                .asJsonArray
                        if (arr.size() > 0) {
                            message = arr.get(0).toString()
                            onGetDataError(message)
                        }
                    } else {
                        error.message?.let {
                            onGetDataError(it)
                        }
                    }
                })
    }

    private fun onResponsefromServerSuccess(t: UserResponse?) {
        Log.i(TAG, "success =============> $t")
        userFromServerSuccess.value = t

    }

    private fun onGetDataStart() {
        startRequest.value = ""
        Log.i(TAG, "start updating user profile ")

    }

    private fun onGetDataFinish() {
        Log.i(TAG, "finished updating user profile ")

    }

    private fun onResponseSuccess(t: UserResponse?) {
        Log.i(TAG, "success =============> $t")
        successUpdateingProfile.value = t

    }

    private fun onGetDataError(it: String) {
        Log.e(TAG, "problem updating user profile $it")
        errorUpdateingProfile.value = it
    }

    /**
     * save user to db
     */

    @SuppressLint("CheckResult")
    private fun writeUserToDb(dbUser: UserDatabaseModel) {
        Observable.fromCallable { userDao.insertUserObjectToDb(dbUser) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onWriteUserToDbSuccess(it) },
                        { onWriteUserToDbError(it) }
                )
    }

    private fun onWriteUserToDbSuccess(it: Unit) {
        Log.i(TAG, "success saving user to database ========> $it")

    }

    private fun onWriteUserToDbError(it: Throwable?) {
        Log.e(TAG, "problem saving user to database ===========> $it")

    }

    fun saveUserToDb(userModel: UserDatabaseModel) {
        Log.i(TAG, "new user model l$userModel")
        writeUserToDb(userModel)
    }

    fun userFromServer(bearerToken: String,userId: String) {
        getuserFromServer(bearerToken , userId)
    }

    fun userFromDb() {
        getUserDB()
    }

    fun updateUser(bearerToken: String, userId: String, update: UpdateProfilePP) {
        updateProfile(bearerToken, userId, update)
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }


}