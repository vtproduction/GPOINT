package com.jedmahonisgroup.gamepoint.ui.auth

import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.JsonParser
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.model.ForgotPasswordModel
import com.jedmahonisgroup.gamepoint.model.ForgotPasswordResponse
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class ForgotPasswordViewModel : BaseViewModel() {
    private var subscription: Disposable? = null

    private val TAG: String = ForgotPasswordViewModel::class.java.simpleName

    val sucessfullyResetPass: MutableLiveData<ForgotPasswordResponse> = MutableLiveData()
    val errorResetUserPass: MutableLiveData<String> = MutableLiveData()

    val startPassResetRequest: MutableLiveData<String> = MutableLiveData()


    @Inject
    lateinit var gamePointApi: GamePointApi

    init {

    }

    private fun forgotPasswordRequest(forgotPasswordModel: ForgotPasswordModel) {
        subscription = gamePointApi.forgotPass("application/json", forgotPasswordModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onForgotPassStart() }
                .doOnTerminate { onForgotPassFinish() }
                .subscribe({ it ->
                    onForgotPassSuccess(it)
                }, { error ->
                    var message = "Something went wrong, please try again"
                    if (error is HttpException) {
                        val errorJsonString = error.response()?.errorBody()?.string()
                        val arr  = JsonParser().parse(errorJsonString)
                                .asJsonObject["errors"]
                                .asJsonArray
                        if (arr.size() > 0) {
                            message = arr.get(0).toString()
                            onForgotPassFail(message)
                        }
                    } else {
                        error.message?.let {
                            onForgotPassFail(it)
                        }
                    }
                })
    }

    private fun onForgotPassStart() {
        Log.i(TAG, "starting pass reset request")
        startPassResetRequest.value = ""
    }

    private fun onForgotPassFinish() {
        Log.i(TAG, "finished  pass reset request")
    }

    private fun onForgotPassSuccess(it: ForgotPasswordResponse?) {
        Log.i(TAG, "Successfully reset password $it")
        sucessfullyResetPass.value = it
    }

    private fun onForgotPassFail(it: String) {
        Log.e(TAG, "failed to reset password $it")
        errorResetUserPass.value = it
    }

    fun resetPassword(request: ForgotPasswordModel){
        forgotPasswordRequest(request)
    }
    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }
}