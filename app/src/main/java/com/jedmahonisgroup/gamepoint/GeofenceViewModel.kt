package com.jedmahonisgroup.gamepoint

import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.JsonParser
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.model.CheckinsResponseModel
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class GeofenceViewModel : BaseViewModel() {

    private lateinit var subscription: Disposable
    private var TAG: String = GeofenceViewModel::class.java.simpleName

    val checkoutSuccess: MutableLiveData<CheckinsResponseModel> = MutableLiveData()
    val errorCheckout: MutableLiveData<String> = MutableLiveData()

    init {
    }

    @Inject
    lateinit var gamePointApi: GamePointApi

    override fun onCleared() {
        super.onCleared()
        subscription.dispose()
    }


    private fun checkOut(bearerToken: String?, checkOutId: String) {
        subscription = gamePointApi.postCheckOut("Bearer $bearerToken", checkOutId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onGetDataStart() }
                .doOnTerminate { onGetDataFinish() }
                .subscribe({ t ->
                    onCheckoutSuccess(t)
                }, { error ->
                    var message = "Something went wrong, please try again"
                    if (error is HttpException) {
                        val errorJsonString = error.response()?.errorBody()?.string()
                        val arr  = JsonParser().parse(errorJsonString)
                                .asJsonObject["errors"]
                                .asJsonArray
                        if (arr.size() > 0) {
                            message = arr.get(0).toString()
                            onCheckOutError(message)
                        }
                    } else {
                        error.message?.let {
                            onCheckOutError(it)
                        }
                    }
                })
    }


    private fun onCheckOutError(it: String) {
        errorCheckout.value = it
        Log.e(TAG, "bg onCheckOutError: ==========> $it")
    }

    private fun onCheckoutSuccess(t: CheckinsResponseModel) {
        checkoutSuccess.value = t
        //update the user object
        Log.i(TAG, " bg onCheckoutSuccess: ==========> $t")
    }

    private fun onGetDataFinish() {
        Log.i(TAG, "bg onGetDataFinish")
    }

    private fun onGetDataStart() {
        Log.i(TAG, " bg onGetDataStart")
    }


}