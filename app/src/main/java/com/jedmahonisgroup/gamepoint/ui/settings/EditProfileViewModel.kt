package com.jedmahonisgroup.gamepoint.ui.settings

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.database.UserDao
import com.jedmahonisgroup.gamepoint.database.model.UserDatabaseModel
import com.jedmahonisgroup.gamepoint.model.UpdateProfile
import com.jedmahonisgroup.gamepoint.model.UserResponse
import com.jedmahonisgroup.gamepoint.model.UserResponseModel
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class EditProfileViewModel(private var userDao: UserDao) : BaseViewModel() {
    private var subscription: Disposable? = null
    private var TAG: String = EditProfileViewModel::class.java.simpleName
    lateinit var context: Context
    @Inject
    lateinit var gamePointApi: GamePointApi

    init {
    }


    val userFromDbSuccess: MutableLiveData<UserResponse> = MutableLiveData()
    val userFromDbError: MutableLiveData<String> = MutableLiveData()

    //update profile
    val successUpdateingProfile: MutableLiveData<UserResponse> = MutableLiveData()
    val errorUpdateingProfile: MutableLiveData<String> = MutableLiveData()

    val startRequest: MutableLiveData<String> = MutableLiveData()

    @SuppressLint("CheckResult")
    private fun getUserDB() {
        try {
            onGetUserFromDbSuccess(GamePointApplication.shared!!.getCurrentUser(context))
        } catch (e: java.lang.Exception) {
            onGetUserFromDbError(e.localizedMessage)
        }

//        subscription = Observable.fromCallable { userDao.getUserObjectFromDb.user }
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({
//                    onGetUserFromDbSuccess(it)
//                },
//                        {
//                            onGetUserFromDbError(it)
//                        }
//                )
    }

    private fun onGetUserFromDbSuccess(it: UserResponse?) {
        Log.e(TAG, "getUserFromDbSuccess ===========> $it")


        userFromDbSuccess.value = it
    }

    private fun onGetUserFromDbError(it: String?) {
        Log.e(TAG, "onGetUserFromDbError ============= > ${it}")
        userFromDbError.value = it
    }

    /**
     * PATCH user
     */

    private fun updateProfile(bearerToken: String, userId: String, update: UpdateProfile) {
        Log.e("update profile is", update.toString())
        subscription = gamePointApi.updateProfile("application/json", "Bearer $bearerToken", userId, update)
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

    private fun onGetDataStart() {
        startRequest.value = ""
        Log.i(TAG, "start updating user profile ")

    }

    private fun onGetDataFinish() {
        Log.i(TAG, "finished updating user profile ")

    }

    private fun onResponseSuccess(t: UserResponse?) {
        Log.i(TAG, "success updating user profile $t")
        val oldUser = GamePointApplication.shared!!.getCurrentUser(context)
        t?.login = oldUser!!.login
        GamePointApplication.shared!!.setCurrentUser(t!!, context)
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
    private fun writeUserToDb(user: UserResponse) {
        try {
            GamePointApplication.shared!!.setCurrentUser(user, context)
//            onWriteUserToDbSuccess()
        } catch (e: java.lang.Exception) {
            onWriteUserToDbError(e.localizedMessage)
        }
    }

    private fun onWriteUserToDbSuccess(it: Unit) {
        Log.i(TAG, "success saving user to database ========> $it")

    }

    private fun onWriteUserToDbError(it: String?) {
        Log.e(TAG, "problem saving user to database ===========> $it")

    }

    fun saveUserToDb(userModel: UserResponse) {
        Log.i(TAG, "new user model l$userModel")
        writeUserToDb(userModel)
    }

    fun userFromDb() {
        getUserDB()
    }

    fun updateUser(bearerToken: String, userId: String, update: UpdateProfile) {
        updateProfile(bearerToken, userId, update)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            subscription?.dispose()
        } catch (e: Exception) {

        }
    }


}