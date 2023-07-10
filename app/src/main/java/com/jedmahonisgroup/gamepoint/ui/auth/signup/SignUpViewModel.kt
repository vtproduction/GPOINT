package com.jedmahonisgroup.gamepoint.ui.auth.signup

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import android.util.Log
import android.view.View
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.database.AppDatabase
import com.jedmahonisgroup.gamepoint.database.UserDao
import com.jedmahonisgroup.gamepoint.database.model.UserDatabaseModel
import com.jedmahonisgroup.gamepoint.model.UserModel
import com.jedmahonisgroup.gamepoint.model.UserResponse
import com.jedmahonisgroup.gamepoint.model.UserResponseModel
import com.jedmahonisgroup.gamepoint.model.school.School
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.lang.Exception
import javax.inject.Inject

class SignUpViewModel(private val userDao: UserDao, private val db: AppDatabase) : BaseViewModel() {

    private val TAG = SignUpViewModel::class.java.simpleName

    @Inject
    lateinit var gamePointApi: GamePointApi

    val loadingVisibility: MutableLiveData<Int> = MutableLiveData()


    val successDeletingUser: MutableLiveData<String> = MutableLiveData()
    val errorDeleteingUser: MutableLiveData<String> = MutableLiveData()

    val errorMessage: MutableLiveData<String> = MutableLiveData()
    val errorClickListener = View.OnClickListener {}

    val userSignUpSuccess: MutableLiveData<UserResponse> = MutableLiveData()

    val successClickListener = View.OnClickListener {}

    private var subscription: Disposable? = null


    val succesGetSchools: MutableLiveData<List<School>> = MutableLiveData()
    val errorGetSchools: MutableLiveData<String> = MutableLiveData()

    var context: Context? = null

    fun getSchools() {
        subscription = gamePointApi.getSchools()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onGetSchoolStart() }
                .doOnTerminate { onGetSchoolFinish() }
                .subscribe({ responseModel ->
                    onGetSchoolSuccess(responseModel)
                }, { error ->
                    var message = "Something went wrong, please try again"
                    if (error is HttpException) {
                        val errorJsonString = error.response()?.errorBody()?.string()
                        val arr  = JsonParser().parse(errorJsonString)
                                .asJsonObject["errors"]
                                .asJsonArray
                        if (arr.size() > 0) {
                            message = arr.get(0).toString()
                            onGetSchoolError(message)
                        }
                    } else {
                        var message = "Something went wrong, please try again"
                        if (error is HttpException) {
                            val errorJsonString = error.response()?.errorBody()?.string()
                            val arr  = JsonParser().parse(errorJsonString)
                                    .asJsonObject["errors"]
                                    .asJsonArray
                            if (arr.size() > 0) {
                                message = arr.get(0).toString()
                                onGetSchoolError(message)
                            }
                        } else {
                            error.message?.let {
                                onGetSchoolError(it)
                            }
                        }
                    }

                })
    }


    private fun onGetSchoolStart() {
        loadingVisibility.value = View.VISIBLE

    }


    private fun onGetSchoolFinish() {

    }

    private fun onGetSchoolSuccess(response: List<School>) {
        succesGetSchools.value = response

    }

    private fun onGetSchoolError(it: String) {
        errorMessage.value = it


    }


    private fun createUser(user: UserModel) {
        subscription = gamePointApi.createUser("application/json",user)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onCreateUserStart() }
                .doOnTerminate { onCreateUserFinish() }
                .subscribe({ responseModel ->
                    onCreateUserSuccess(responseModel)
                }, { error ->
                    var message = "Something went wrong, please try again"
                    if (error is HttpException) {
                        val errorJsonString = error.response()?.errorBody()?.string()
                        val arr  = JsonParser().parse(errorJsonString)
                                .asJsonObject["errors"]
                                .asJsonArray
                        if (arr.size() > 0) {
                            message = arr.get(0).toString()
                            onCreateUserError(message)
                        }
                    } else {
                        var message = "Something went wrong, please try again"
                        if (error is HttpException) {
                            val errorJsonString = error.response()?.errorBody()?.string()
                            val arr  = JsonParser().parse(errorJsonString)
                                    .asJsonObject["errors"]
                                    .asJsonArray
                            if (arr.size() > 0) {
                                message = arr.get(0).toString()
                                onCreateUserError(message)
                            }
                        } else {
                            error.message?.let {
                                onCreateUserError(it)
                            }
                        }
                    }

                })
    }

    private fun onCreateUserStart() {
        loadingVisibility.value = View.VISIBLE

    }

    private fun onCreateUserFinish() {

    }

    private fun onCreateUserSuccess(response: UserResponseModel) {
        //save the token
        GamePointApplication.shared!!.setCurrentUser(response.user, context!!)
        userSignUpSuccess.value = response.user

        //delete old user form db
//        deleteDb(response.user.login.token)

        //prepare to save user to db


        Log.e(TAG, "onCreateUserSuccess ==========> $response")

    }

    private fun onCreateUserError(it: String) {
        Log.e(TAG, "onCreateUserError ==========> there was an error creating the user Error: " + it)
        errorMessage.value = it


    }


    fun onSignUpClicked(user: UserModel) {
        createUser(user)
        Log.e(TAG, "onSignUpClicked ==========> Creating user$user")

    }

    @SuppressLint("CheckResult")
    private fun saveUserSessionTODb(userDatabaseModel: UserDatabaseModel) {
//        Observable.fromCallable {userDao.insertUserObjectToDb(userDatabaseModel) }
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({onSaveUserToDbSuccess()},
//                        { onSaveUserToDbError(it) }
//                )
    }

    private fun onSaveUserToDbError(it: Throwable) {
        Log.e(TAG, "onSaveUserToDbError ==========> there was a problem saving the user to database: $it")

    }

    private fun onSaveUserToDbSuccess() {
        Log.e(TAG, "onSaveUserToDbError ==========> Successfully saved user to database")
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
        errorDeleteingUser.value = it.toString()

    }

    private fun onDbDeleted(s: String, token: String) {
        // getLogout(token)
        Log.e(TAG, "==========> deleteted user db $s")
        successDeletingUser.value = s

    }


    override fun onCleared() {
        super.onCleared()
        try {
            subscription?.dispose()
        } catch (e: Exception) {

        }
    }
}