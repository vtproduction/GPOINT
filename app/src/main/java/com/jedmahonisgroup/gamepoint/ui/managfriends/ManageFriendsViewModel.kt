package com.jedmahonisgroup.gamepoint.ui.managfriends

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.model.FriendADRequestModel
import com.jedmahonisgroup.gamepoint.model.FriendsModel
import com.jedmahonisgroup.gamepoint.model.feeds.PostsModel
import com.jedmahonisgroup.gamepoint.model.feeds.User
import com.jedmahonisgroup.gamepoint.model.searchModel
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class ManageFriendsViewModel : BaseViewModel() {
    private var TAG: String = ManageFriendsViewModel::class.java.simpleName

    private lateinit var subscription: Disposable

    //get friends
    val friendsFromServer: MutableLiveData<FriendsModel> = MutableLiveData()
    val searchfriendsFromServer: MutableLiveData<List<User>> = MutableLiveData()




    val serverDataFailed: MutableLiveData<String> = MutableLiveData()




    //post deals
    val postSuccess: MutableLiveData<PostsModel> = MutableLiveData()
    val postFail: MutableLiveData<String> = MutableLiveData()

    init {

    }

    override fun onCleared() {
        super.onCleared()

    }

    @set:Inject
    lateinit var gamePointApi: GamePointApi

    private fun getfriendsFromServer(bearerToken: String) {

        subscription = gamePointApi.getFriends("Bearer $bearerToken")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onGetDataStart() }
                .doOnTerminate { onGetDataFinish() }
                .subscribe({ t ->
                    Log.i(TAG,t.toString())
                    onGetDataSuccess(t)
                }, { error ->
                    var message = "Something went wrong, please try again"
                    if (error is HttpException) {
                        val errorJsonString = error.response()?.errorBody()?.string()
                        try {
                            val arr  = JsonParser().parse(errorJsonString)
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

    fun getsearchresult(bearerToken: String, searchquery: Array<String>) {

        subscription = gamePointApi.searchResult("Bearer $bearerToken" , searchModel(names = searchquery))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onGetDataStart() }
                .doOnTerminate { onGetDataFinish() }
                .subscribe({ t ->
                    Log.i(TAG,t.toString())
                    onGetDataSuccessSR(t)
                }, { error ->
                    var message = "Something went wrong, please try again"
                    if (error is HttpException) {
                        val errorJsonString = error.response()?.errorBody()?.string()
                        try {
                            val arr  = JsonParser().parse(errorJsonString)
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

    private fun onGetDataSuccessSR(t: List<User>) {
        Log.i(TAG, "get search  resultsFromServer $t")

        searchfriendsFromServer.value = t
    }


    private fun onGetDataError(error: String) {
        Log.e("$TAG feeds model error", error)
        serverDataFailed.value = error
    }

    private fun onGetDataSuccess(response: FriendsModel) {
        val gson = Gson()

        friendsFromServer.value = response


        Log.i(TAG, "get Event resultsFromServer $response")

        val feedsString = gson.toJson(response)


    }

    private fun onGetDataFinish() {
        Log.i("$TAG", "onGetDataFinish")
    }

    private fun onGetDataStart() {
        Log.i("$TAG", "onGetDataStart")
    }

    fun getFriends(bearerToken: String){
        getfriendsFromServer(bearerToken)
    }

    fun Request(bearerToken : String , iD : String, rep: FriendADRequestModel) {


        subscription = gamePointApi.Requestanswer("Application/JSON","Bearer $bearerToken", iD , rep)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onGetDataStart() }
                .doOnTerminate { onGetDataFinish() }
                .subscribe({ t ->
                    Log.i(TAG,t.toString())
                    getfriendsFromServer(bearerToken)


                }, { error ->
                    var message = "Something went wrong, please try again"
                    if (error is HttpException) {
                        val errorJsonString = error.response()?.errorBody()?.string()
                        try {
                            val arr  = JsonParser().parse(errorJsonString)
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


}
