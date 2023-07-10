package com.jedmahonisgroup.gamepoint.ui.comments

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.model.CommentsRModel
import com.jedmahonisgroup.gamepoint.model.PostCommentModel
import com.jedmahonisgroup.gamepoint.model.feeds.PostsModel
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class CommentsViewModel : BaseViewModel() {
    private var TAG: String = CommentsViewModel::class.java.simpleName

    private lateinit var subscription: Disposable

    //get friends
    val commentsFromServer: MutableLiveData<List<CommentsRModel>> = MutableLiveData()



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

    private fun getCommentsFromServer(bearerToken: String, postId: String) {

        subscription = gamePointApi.getComments("Bearer $bearerToken" , postId)
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

    private fun postComment(bearerToken: String,postId:String ,post: PostCommentModel) {

        subscription = gamePointApi.postComment("Application/JSON","Bearer $bearerToken",postId , post)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onGetDataStart() }
                .doOnTerminate { onGetDataFinish() }
                .subscribe({ t ->
                    Log.i(TAG, "comment Post $t")
                    getCommentsFromServer(bearerToken,postId)
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

    fun deletComment(bearerToken: String,postId:String ,commentId: String) {

        subscription = gamePointApi.deleteComment("Application/JSON","Bearer $bearerToken",postId , commentId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onGetDataStart() }
                .doOnTerminate { onGetDataFinish() }
                .subscribe({ t ->
                    Log.i(TAG, "Comment has been Deleted $t")
                    getCommentsFromServer(bearerToken,postId)
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


    fun reportComment(bearerToken: String,postId:String ,commentId: String) {

        subscription = gamePointApi.commentReport("Application/JSON","Bearer $bearerToken",postId , commentId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onGetDataStart() }
                .doOnTerminate { onGetDataFinish() }
                .subscribe({ t ->
                    Log.i(TAG, "Comment has been Deleted $t")
                    getCommentsFromServer(bearerToken,postId)
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









    private fun onGetDataError(error: String) {
        Log.e("$TAG feeds model error", error)
        serverDataFailed.value = error
    }

    private fun onGetDataSuccess(response: List<CommentsRModel>) {
        val gson = Gson()

        commentsFromServer.value = response


        Log.i(TAG, "get Event resultsFromServer $response")

        val feedsString = gson.toJson(response)


    }

    private fun onGetDataFinish() {
        Log.i("$TAG", "onGetDataFinish")
    }

    private fun onGetDataStart() {
        Log.i("$TAG", "onGetDataStart")
    }

    fun getComments(bearerToken: String , postId : String){
        getCommentsFromServer(bearerToken , postId)
    }

    fun postCmmnt(token : String, postId: String , comment : PostCommentModel) {
        postComment(token , postId , comment)
    }


}
