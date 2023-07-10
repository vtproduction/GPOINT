package com.jedmahonisgroup.gamepoint.ui

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.database.UserDao
import com.jedmahonisgroup.gamepoint.database.model.UserDatabaseModel
import com.jedmahonisgroup.gamepoint.model.UserImage
import com.jedmahonisgroup.gamepoint.model.UserResponse
import com.jedmahonisgroup.gamepoint.model.UserResponseModel
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class UploadPictureViewModel(private val userDao: UserDao) : BaseViewModel() {
    private val TAG = UploadPictureActivity::class.java.simpleName
    @Inject
    lateinit var gamePointApi: GamePointApi

    val uploadedImageSuccessfully: MutableLiveData<UserResponse> = MutableLiveData()
    val errorUploadingImage: MutableLiveData<String> = MutableLiveData()


    val writeUserToDbSucess: MutableLiveData<String> = MutableLiveData()
    val writeUserToDbError: MutableLiveData<String> = MutableLiveData()

    val getUserFromDbSuccess: MutableLiveData<UserResponseModel> = MutableLiveData()
    val getUserFromDbError: MutableLiveData<String> = MutableLiveData()


    private var subscription: Disposable? = null

    init {

    }

    private fun upload(bearerToken: String, userId: String, image: UserImage) {
        subscription = gamePointApi.uploadUserPicture("application/json","Bearer $bearerToken",userId, image)
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

    private fun onGetDataError(it: String) {
        errorUploadingImage.value = it
        Log.e(TAG, "onGetDataError ===========> there was a problem uploading the profile pic$it ")
    }

    private fun onResponseSuccess(t: UserResponse) {
        uploadedImageSuccessfully.value = t
        Log.i(TAG, "onResponseSuccess ===========> successfully uploaded profile picture $t ")
    }

    private fun onGetDataFinish() {
        Log.i(TAG, "onGetDataFinish ===========> finished ")
    }

    private fun onGetDataStart() {
        Log.i(TAG, "onGetDataStart ===========> started ")
    }

    /**
     * Read user from database
     */
    @SuppressLint("CheckResult")
    private fun getUserDB() {
        subscription = Observable.fromCallable {userDao.getUserObjectFromDb.user }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onGetUserFromDbSuccess(it)},
                        { onGetUserFromDbError(it) }
                )
    }

    private fun onGetUserFromDbSuccess(it: String?) {
        val gson = Gson()
        Log.i(TAG, "user from database success")
        val user: UserResponseModel = gson.fromJson(it, UserResponseModel::class.java)
        getUserFromDbSuccess.value = user
    }

    private fun onGetUserFromDbError(it: Throwable?) {
        Log.e(TAG, "user from database error")
        getUserFromDbError.value = it.toString()
    }

    /**
     * Save user to database
     */
    @SuppressLint("CheckResult")
    private fun UpdateuserToDb(dbUser: UserDatabaseModel) {
        Observable.fromCallable { userDao.updateUser(dbUser) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onWriteUserToDbSuccess(it) },
                        { onWriteUserToDbError(it) }
                )
    }

    private fun onWriteUserToDbSuccess(it: Unit?) {
        Log.i(TAG, "successfully saved user to database")
        writeUserToDbSucess.value = it.toString()
    }

    private fun onWriteUserToDbError(it: Throwable?) {
        Log.e(TAG, "error when trying to save user to database")
        writeUserToDbError.value = it.toString()    }

    fun saveUserTodabase(user: UserResponseModel){
        Log.i(TAG, "saving user to db")
        val gson = Gson()
        val userString = gson.toJson(user)

        val dbUserModel = UserDatabaseModel(
                id = 1, user = userString
        )
        UpdateuserToDb(dbUserModel)
    }
    fun getUserFromDatabase(){
        getUserDB()
    }

    fun uploadPicture(bearerToken: String, userId: String, image: UserImage){
        upload(bearerToken,userId,image)
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }
}
