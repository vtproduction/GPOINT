package com.jedmahonisgroup.gamepoint.ui.auth.school

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonParser
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.model.school.School
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class SelectSchoolViewModel : BaseViewModel() {

    private val TAG = SelectSchoolViewModel::class.java.simpleName

    @set:Inject
    lateinit var gamePointApi: GamePointApi

    val loadingVisibility: MutableLiveData<Int> = MutableLiveData()

    val succesGetSchools: MutableLiveData<List<School>> = MutableLiveData()
    val errorGetSchools: MutableLiveData<String> = MutableLiveData()
    val errorMessage: MutableLiveData<String> = MutableLiveData()

    private var subscription: Disposable? = null


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

}