package com.jedmahonisgroup.gamepoint.ui.notification

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.database.FeedsDao
import com.jedmahonisgroup.gamepoint.model.FriendSentRequestModel
import com.jedmahonisgroup.gamepoint.model.feeds.PostsModel
import com.jedmahonisgroup.gamepoint.model.feeds.User
import com.jedmahonisgroup.gamepoint.model.msgResponseModel
import com.jedmahonisgroup.gamepoint.model.notificationResponseModel
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class NotificationViewModel : BaseViewModel() {
    private var TAG: String = NotificationViewModel::class.java.simpleName

    private lateinit var subscription: Disposable

    //get feeds
    val notificationFromServer: MutableLiveData<List<notificationResponseModel>> = MutableLiveData()

    val serverDataFailed: MutableLiveData<String> = MutableLiveData()

    init {

    }

    override fun onCleared() {
        super.onCleared()

    }

    @set:Inject
    lateinit var gamePointApi: GamePointApi

    fun getNotificationFromServer(bearerToken: String) {


        subscription = gamePointApi!!.notification("Bearer $bearerToken")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onGetDataStart() }
                .doOnTerminate { onGetDataFinish() }
                .subscribe({ t ->
                    Log.i(TAG, t.toString())
                    onGetDataSuccess(t)
                }, { error ->
                    var message = "Something went wrong, please try again"
                    if (error is HttpException) {
                        val errorJsonString = error.response()?.errorBody()?.string()
                        try {
                            val arr = JsonParser().parse(errorJsonString)
                                    .asJsonObject["errors"]
                                    .asJsonArray
                            if (arr.size() > 0) {
                                message = arr.get(0).toString()
                                onGetDataError(message)
                            }
                        } catch (e: Exception) {
                            error.message?.let {
                                onGetDataError(it)
                            }
                        }
                    } else {
                        error.message?.let {
                            onGetDataError(it)
                        }
                    }
                })


    }




    private fun onGetDataError(error: String) {
        Log.e("$TAG feeds model error", error)
        serverDataFailed.value = error
    }

    private fun onGetDataSuccess(response: List<notificationResponseModel>) {
        notificationFromServer.value = response

        Log.i(TAG, "get feeds resultsFromServer $response")


    }




    private fun onGetDataFinish() {
        Log.i("$TAG", "onGetDataFinish")
    }

    private fun onGetDataStart() {
        Log.i("$TAG", "onGetDataStart")
    }








}