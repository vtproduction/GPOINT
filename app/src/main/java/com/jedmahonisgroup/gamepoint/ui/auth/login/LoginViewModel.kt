package com.jedmahonisgroup.gamepoint.ui.auth.login

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.database.AppDatabase
import com.jedmahonisgroup.gamepoint.database.UserDao
import com.jedmahonisgroup.gamepoint.database.model.UserDatabaseModel
import com.jedmahonisgroup.gamepoint.model.LoginModel
import com.jedmahonisgroup.gamepoint.model.UserResponse
import com.jedmahonisgroup.gamepoint.model.UserResponseModel
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class LoginViewModel(private val userDao: UserDao, private val db: AppDatabase) : BaseViewModel() {

    private val TAG: String = LoginViewModel::class.java.simpleName
    @Inject
    lateinit var gamePointApi: GamePointApi

    val successMessage: MutableLiveData<UserResponse> = MutableLiveData()
    val errorMessage: MutableLiveData<String> = MutableLiveData()

    val successDeletingUser: MutableLiveData<String> = MutableLiveData()
    val errorDeleteingUser: MutableLiveData<String> = MutableLiveData()

    val loginStarting: MutableLiveData<String> = MutableLiveData()


    private  var subscription: Disposable? = null

    var context: Context? = null

    private fun authenticateUser(loginModel: LoginModel) {
        Log.e("JMG", "loginModel: " + loginModel)
        subscription = gamePointApi.login("application/json", loginModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onAuthenticateUserStart() }
                .doOnTerminate { onAuthenticateUserFinish() }
                .subscribe({
                    onAuthenticateUserSuccess(it)
                }, { error ->
                    var message = "Something went wrong, please try again"
                    if (error is HttpException) {
                        val errorJsonString = error.response()?.errorBody()?.string()
                        val arr  = JsonParser().parse(errorJsonString)
                                .asJsonObject["errors"]
                                .asJsonArray
                        if (arr.size() > 0) {
                            message = arr.get(0).toString()
                            onAuthenticateUserError(message)
                        }
                    } else {
                        error.message?.let {
                            onAuthenticateUserError(it)
                        }
                    }
                })
    }

    private fun onAuthenticateUserStart() {
        Log.e("Auth: ", "Starting")
        loginStarting.value = "starting"

    }

    private fun onAuthenticateUserFinish() {
        Log.e("Auth: ", "finished")

    }

    private fun onAuthenticateUserSuccess(response: UserResponseModel) {
        Log.e("Success Login: ", response.toString())
        GamePointApplication.shared!!.setCurrentUser(response.user, context!!)
        //before we save the session, we should check to see if this user is the same as the existing one
        //Save the session

        val user = Gson().toJson(response.user)
        val userDatabaseModel = UserDatabaseModel(1, user)
        saveUserSessionTODb(userDatabaseModel)



        successMessage.value = response.user

    }

    private fun onAuthenticateUserError(it: String) {
        Log.e(TAG, "onAuthenticateUserError ========> there was a problem logining in$it")
        errorMessage.value = it
    }

    @SuppressLint("CheckResult")
    private fun saveUserSessionTODb(userDatabaseModel: UserDatabaseModel) {
        Observable.fromCallable {userDao.insertUserObjectToDb(userDatabaseModel) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({onSaveUserToDbSuccess()},
                        { onSaveUserToDbError(it) }
                )
    }

    private fun onSaveUserToDbError(it: Throwable) {
        Log.e("SAVED TO DB:: ", "Error + $it")

    }

    private fun onSaveUserToDbSuccess() {
        Log.e("SAVED TO DB: ", "Success")

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


    fun onLoginClicked(loginModel: LoginModel) {
        authenticateUser(loginModel)
    }

    fun saveUserToDb(userDatabaseModel: UserDatabaseModel){
        saveUserSessionTODb(userDatabaseModel)
    }

    fun deleteUser(token: String){
        deleteDb(token)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            subscription?.dispose()
        } catch (e: Exception) {

        }
    }
}
