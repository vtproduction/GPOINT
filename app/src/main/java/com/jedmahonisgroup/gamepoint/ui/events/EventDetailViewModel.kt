package com.jedmahonisgroup.gamepoint.ui.events

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.database.UserDao
import com.jedmahonisgroup.gamepoint.model.CheckIn
import com.jedmahonisgroup.gamepoint.model.CheckinsResponseModel
import com.jedmahonisgroup.gamepoint.model.UserResponseModel
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class EventDetailViewModel(private var userDao: UserDao) : BaseViewModel() {

    private val TAG: String = EventDetailViewModel::class.java.simpleName

    private  var subscription: Disposable? = null

    val dbUserSuccess: MutableLiveData<UserResponseModel> = MutableLiveData()
    val dbUserFail: MutableLiveData<String> = MutableLiveData()

    val successCheckIn: MutableLiveData<CheckinsResponseModel> = MutableLiveData()
    val failedCheckin: MutableLiveData<String> = MutableLiveData()

    init {

    }

    @Inject
    lateinit var gamePointApi: GamePointApi


    @SuppressLint("CheckResult")
    private fun getUserDB() {
        Observable.fromCallable { userDao.getUserObjectFromDb.user }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onGetUserToDbSuccess(it) },
                        { onGetUserToDbError(it) }
                )
    }


    private fun onGetUserToDbError(it: Throwable?) {
        Log.e("$TAG: onGetUserToDbError: ", it.toString())
        dbUserFail.value = it.toString()
    }

    private fun onGetUserToDbSuccess(it: String) {
        Log.e("$TAG,onGetUserToDbSuccess: ", it)

        val gson = Gson()
        val user: UserResponseModel = gson.fromJson(it, UserResponseModel::class.java)
        Log.e("$TAG, TOKEN: ", user.user.login.token)

        dbUserSuccess.value = user
    }

    private fun postCheckIn(bearerToken: String, checkin: CheckIn) {
        subscription = gamePointApi.postCheckins("Bearer $bearerToken", checkin)
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
                        val arr  = try{
                            JsonParser().parse(errorJsonString)
                                .asJsonObject["checked_in"]
                                .asJsonArray
                        }catch(t: Throwable){
                            JsonArray()
                        }
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
        Log.e(TAG, "onGetDataError: ==========> $it")
        failedCheckin.value = it
    }

    private fun onResponseSuccess(t: CheckinsResponseModel) {
        successCheckIn.value = t
        Log.i(TAG, "onResponseSuccess: ==========> $t")

    }

    private fun onGetDataFinish() {
        Log.i(TAG, "onGetDataFinish")
    }

    private fun onGetDataStart() {
        Log.i(TAG, "onGetDataStart")
    }


    fun postUserCheckin(bearerToken: String, checkin: CheckIn) {
        postCheckIn(bearerToken, checkin)
    }


    fun getUserFromDB(context: Context) {
        //getUserDB()
        val user = GamePointApplication.shared!!.getCurrentUser(context)
        if (user!= null){
            Log.i(TAG, "getUserFromDB SUCCESS")
            dbUserSuccess.postValue(UserResponseModel(user))
        }else{
            Log.i(TAG, "getUserFromDB FAIL")
            dbUserFail.postValue("NULL USER")
        }


    }


    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }
}