package com.jedmahonisgroup.gamepoint.ui.feeds

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonParser
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.model.FriendSentRequestModel
import com.jedmahonisgroup.gamepoint.model.SponsorModel
import com.jedmahonisgroup.gamepoint.model.UserBlockModel
import com.jedmahonisgroup.gamepoint.model.feeds.PostsModel
import com.jedmahonisgroup.gamepoint.model.feeds.User
import com.jedmahonisgroup.gamepoint.model.reportModel
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class SocialViewModel : BaseViewModel()
{
    private var TAG: String = SocialViewModel::class.java.simpleName

    private lateinit var subscription: Disposable

    //get feeds
    val feedsFromServer: MutableLiveData<List<PostsModel>> = MutableLiveData()

    val serverDataFailed: MutableLiveData<String> = MutableLiveData()

    val feedsFromDb: MutableLiveData<List<PostsModel>> = MutableLiveData()
    val failedFeedsFromDb: MutableLiveData<String> = MutableLiveData()

    val errorSavingFeeds: MutableLiveData<String> = MutableLiveData()

    val SponsorsFromServer: MutableLiveData<ArrayList<SponsorModel>> by lazy { MutableLiveData<ArrayList<SponsorModel>>() }

    //post deals
    val postSuccess: MutableLiveData<String> = MutableLiveData()
    val postFail: MutableLiveData<String> = MutableLiveData()

    init {

    }

    override fun onCleared() {
        super.onCleared()
        subscription.dispose()
    }

    @set:Inject
    lateinit var gamePointApi: GamePointApi

    private fun getFeedsFromServer(bearerToken: String) {

        Log.e("JMG", "getFeedsFromServer called")
        subscription = gamePointApi.getFeeds("Bearer $bearerToken")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onGetDataStart() }
            .doOnTerminate { onGetDataFinish() }
            .subscribe({ t ->
                Log.e(TAG, "subscribe t: " + t.toString())
                onGetDataSuccess(t.posts)
            }, { error ->
                Log.e("JMG", "error: " + error)
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


    fun getSponsorFromServer(bearerToken: String) {


        subscription = gamePointApi.getsponsor("Bearer $bearerToken")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onGetDataStart() }
            .doOnTerminate { onGetDataFinish() }
            .subscribe({ t ->
                Log.e(TAG, "getsponsor result: " + t.toString())
                SponsorsFromServer.value = t
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

    fun getFeedsGlobFromServer(bearerToken: String) {

        getFeedsFromServer(bearerToken)

    }


    fun getFeedsFriendsFromServer(bearerToken: String) {


        subscription = gamePointApi!!.getFriendsFeeds("Bearer $bearerToken")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onGetDataStart() }
            .doOnTerminate { onGetDataFinish() }
            .subscribe({ t ->
                Log.i(TAG, t.toString())
                onGetDataSuccess(t.posts)
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

    fun getFeedsPrivateFromServer(bearerToken: String, Myid: String) {


        subscription = gamePointApi!!.getPrivateFeeds("Bearer $bearerToken", Myid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onGetDataStart() }
            .doOnTerminate { onGetDataFinish() }
            .subscribe({ t ->
                Log.i(TAG, t.toString())
                onGetDataSuccess(t.posts)
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

    private fun onGetDataSuccess(response: List<PostsModel>) {
//        val gson = Gson()
        feedsFromServer.value = response
//        Log.e(TAG, "feedsFromServer.value ${feedsFromServer.value}")
        Log.e(TAG, "get feeds resultsFromServer ${response.size}")

//        val feedsString = gson.toJson(response)

    }



    fun postLike(bearerToken: String, iDPost: String) {
        Log.e("JMG", "iD: " + iDPost)
        subscription = gamePointApi.postLike("application/json", "Bearer $bearerToken", iDPost)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onPostDataStart() }
            .doOnTerminate { onPostDataFinish() }
            .subscribe({ t ->
            }, { error ->

                var message = "Something went wrong, please try again"
                if (error is HttpException) {
                    val errorJsonString = error.response()?.errorBody()?.string()
                    val arr = JsonParser().parse(errorJsonString)
                        .asJsonObject["errors"]
                        .asJsonArray
                    if (arr.size() > 0) {
                        message = arr.get(0).toString()
                        onPostDataError(message)
                    }
                } else {
                    error.message?.let {
                        onPostDataError(it)
                    }
                }
            })
    }

    fun postDislike(bearerToken: String, iDPost: String) {
        Log.e("JMG", "iD: " + iDPost)
        subscription = gamePointApi.postDislike("application/json", "Bearer $bearerToken", iDPost)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onPostDataStart() }
            .doOnTerminate { onPostDataFinish() }
            .subscribe({ t ->
            }, { error ->

                var message = "Something went wrong, please try again"
                if (error is HttpException) {
                    val errorJsonString = error.response()?.errorBody()?.string()
                    val arr = JsonParser().parse(errorJsonString)
                        .asJsonObject["errors"]
                        .asJsonArray
                    if (arr.size() > 0) {
                        message = arr.get(0).toString()
                        onPostDataError(message)
                    }
                } else {
                    error.message?.let {
                        onPostDataError(it)
                    }
                }
            })
    }

    fun reportPost(bearerToken: String, iDPost: String) {

        Log.e("JMG", "iD: " + iDPost)
        subscription = gamePointApi.postReport("application/json", "Bearer $bearerToken", iDPost)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onPostDataStart() }
            .doOnTerminate { onPostDataFinish() }
            .subscribe({ t ->
            }, { error ->

                var message = "Something went wrong, please try again"
                if (error is HttpException) {
                    val errorJsonString = error.response()?.errorBody()?.string()
                    val arr = JsonParser().parse(errorJsonString)
                        .asJsonObject["errors"]
                        .asJsonArray
                    if (arr.size() > 0) {
                        message = arr.get(0).toString()
                        onPostDataError(message)
                    }
                } else {
                    error.message?.let {
                        onPostDataError(it)
                    }
                }
            })

    }

    fun reportUser(bearerToken:String ,iD: String , report: reportModel) {

        subscription = gamePointApi.ReportUser( "Bearer $bearerToken", iD ,report)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onPostDataStart() }
            .doOnTerminate { onPostDataFinish() }
            .subscribe({ t ->
            }, { error ->

                var message = "Something went wrong, please try again"
                if (error is HttpException) {
                    val errorJsonString = error.response()?.errorBody()?.string()
                    val arr = JsonParser().parse(errorJsonString)
                        .asJsonObject["errors"]
                        .asJsonArray
                    if (arr.size() > 0) {
                        message = arr.get(0).toString()
                        onPostDataError(message)
                    }
                } else {
                    error.message?.let {
                        onPostDataError(it)
                    }
                }
            })

    }


    fun deletePost(bearerToken: String, iDPost: String, typeOfFeeds: String, iD: String) {

        Log.e("JMG", "iD: " + iDPost)
        subscription = gamePointApi.deletePost("application/json", "Bearer $bearerToken", iDPost)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onPostDataStart() }
            .doOnTerminate { onPostDataFinish() }
            .subscribe({ t ->
                changefeedstype(bearerToken!!, typeOfFeeds, iD!!)

            }, { error ->

                var message = "Something went wrong, please try again"
                if (error is HttpException) {
                    val errorJsonString = error.response()?.errorBody()?.string()
                    val arr = JsonParser().parse(errorJsonString)
                        .asJsonObject["errors"]
                        .asJsonArray
                    if (arr.size() > 0) {
                        message = arr.get(0).toString()
                        onPostDataError(message)
                    }
                } else {
                    error.message?.let {
                        onPostDataError(it)
                    }
                }
            })

    }


    fun sendRequest(bearerToken: String, sendId: FriendSentRequestModel) {

        subscription = gamePointApi.sendRequest("application/json", "Bearer $bearerToken", sendId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onPostDataStart() }
            .doOnTerminate { onPostDataFinish() }
            .subscribe({ t ->
                onPostDataSuccess(t.toString())
            }, { error ->

                var message = "Something went wrong, please try again"
                if (error is HttpException) {
                    val errorJsonString = error.response()?.errorBody()?.string()
                    val arr = JsonParser().parse(errorJsonString)
                        .asJsonObject["errors"]
                        .asJsonArray
                    if (arr.size() > 0) {
                        message = arr.get(0).toString()
                        onPostDataError(message)
                    }
                } else {
                    error.message?.let {
                        onPostDataError(it)
                    }
                }
            })
    }

    fun deletefriend(bearerToken: String, typeOfFeeds: String, User: User?) {

        subscription = gamePointApi.deleteFriend("application/json", "Bearer $bearerToken", User!!.friend_request!!.id.toString())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onPostDataStart() }
            .doOnTerminate { onPostDataFinish() }
            .subscribe({ t ->
                onPostDataSuccess(t.toString())
                changefeedstype(bearerToken!!, typeOfFeeds, User.id.toString()!!)

            }, { error ->

                var message = "Something went wrong, please try again"
                if (error is HttpException) {
                    val errorJsonString = error.response()?.errorBody()?.string()
                    val arr = JsonParser().parse(errorJsonString)
                        .asJsonObject["errors"]
                        .asJsonArray
                    if (arr.size() > 0) {
                        message = arr.get(0).toString()
                        onPostDataError(message)
                    }
                } else {
                    error.message?.let {
                        onPostDataError(it)
                    }
                }
            })

    }


    fun blockUser(bearerToken: String, sendId: UserBlockModel) {

        subscription = gamePointApi.blockUser("application/json", "Bearer $bearerToken", sendId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onPostDataStart() }
            .doOnTerminate { onPostDataFinish() }
            .subscribe({ t ->

            }, { error ->

                var message = "Something went wrong, please try again"
                if (error is HttpException) {
                    val errorJsonString = error.response()?.errorBody()?.string()
                    val arr = JsonParser().parse(errorJsonString)
                        .asJsonObject["errors"]
                        .asJsonArray
                    if (arr.size() > 0) {
                        message = arr.get(0).toString()
                        onPostDataError(message)
                    }
                } else {
                    error.message?.let {
                        onPostDataError(it)
                    }
                }
            })
    }




    private fun onPostDataStart() {
        Log.e("$TAG", "onPostDataStart")
    }

    private fun onPostDataFinish() {
        Log.e("$TAG", "onPostDataFinish")
    }

    private fun onPostDataSuccess(P: String) {
        Log.e("$TAG onPostDataSuccess", P)
        postSuccess.value = P
    }



    private fun onPostDataError(it: String) {
        Log.e("$TAG onPostDataError", it)
    }


    private fun onGetDataFinish() {
        Log.e("$TAG", "onGetDataFinish")
    }

    private fun onGetDataStart() {
        Log.e("$TAG", "onGetDataStart")

    }

    fun getMyFeedsFromServer(bearerToken: String) {
        getFeedsFromServer(bearerToken)
    }


    fun getFeedsDb() {
        //getFeedsFromDatabase()
    }


    fun changefeedstype(TOKEN: String, typeOfFeeds: String, id: String) {

        if (typeOfFeeds == "G") {

            getFeedsGlobFromServer(TOKEN)


        } else if (typeOfFeeds == "F") {

            getFeedsFriendsFromServer(TOKEN)


        } else if (typeOfFeeds == "P") {
            getFeedsPrivateFromServer(TOKEN, id)

        }
    }
}