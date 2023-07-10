package com.jedmahonisgroup.gamepoint.ui.createpost

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.lifecycle.MutableLiveData
import com.github.drjacky.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.model.Post
import com.jedmahonisgroup.gamepoint.model.events.EventsModel
import com.jedmahonisgroup.gamepoint.model.feeds.PostsModel
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import com.jedmahonisgroup.gamepoint.utils.setLocalImage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_profile.*
import retrofit2.HttpException
import javax.inject.Inject

class FeedsCreatPostViewModel : BaseViewModel() {
    private var TAG: String = FeedsCreatPostViewModel::class.java.simpleName

    private lateinit var subscription: Disposable

    //get feeds
    val eventsFromServer: MutableLiveData<List<EventsModel>> = MutableLiveData()



    val serverDataFailed: MutableLiveData<String> = MutableLiveData()

    val feedsFromDb: MutableLiveData<List<PostsModel>> = MutableLiveData()
    val failedFeedsFromDb: MutableLiveData<String> = MutableLiveData()

    val errorSavingFeeds: MutableLiveData<String> = MutableLiveData()

    //post deals



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

    private fun getEventsFromServer(bearerToken: String) {


        subscription = gamePointApi.getEvents("Bearer $bearerToken")
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

    private fun postPost(bearerToken: String, post: Post) {
//        val gson: Gson = Gson()
//        val userString = gson.toJson(post)
        Log.e("JMG", "post: $post")
        subscription = gamePointApi.postUserPost("application/json", "Bearer $bearerToken", post)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onPostDataStart() }
                .doOnTerminate { onPostDataFinish() }
                .subscribe({ t ->
                    onPostDataSuccess(t)
                }, { error ->

                 Log.e(TAG , error.toString())

                })
    }

    private fun onPostDataStart() {
        Log.e("JMG", "onPostDataStart")
    }

    private fun onPostDataFinish() {
        Log.e("JMG", "onPostDataFinish")
    }

    private fun onPostDataSuccess(t: PostsModel) {
        Log.e("JMG onPostDataSuccess", t.toString())
//        getPicksFromDatabase();
        postSuccess.value = t
    }

    private fun onPostDataError(it: String) {
        Log.e("JMG onPostDataError", it)
        postFail.value = it
    }

    fun postUserPost(bearerToken: String, post: Post) {
        postPost(bearerToken, post)
    }





    private fun onGetDataError(error: String) {
        Log.e("$TAG feeds model error", error)
        serverDataFailed.value = error
    }

    private fun onGetDataSuccess(response: List<EventsModel>) {
        val gson = Gson()
        eventsFromServer.value = response

        Log.i(TAG, "get Event resultsFromServer $response")

//        val feedsString = gson.toJson(response)


    }

    private fun onGetDataFinish() {
        Log.i("JMG", "onGetDataFinish")
    }

    private fun onGetDataStart() {
        Log.i("JMG", "onGetDataStart")
    }

    fun geteventsFromServer(token: String) {
        getEventsFromServer(token)

    }


}
