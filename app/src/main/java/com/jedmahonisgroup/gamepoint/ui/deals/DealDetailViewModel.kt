package com.jedmahonisgroup.gamepoint.ui.deals

import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.JsonParser
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.model.DealBody
import com.jedmahonisgroup.gamepoint.model.DealRedeemModel
import com.jedmahonisgroup.gamepoint.model.RedeemDealBody
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class DealDetailViewModel : BaseViewModel() {
    private var TAG: String = DealDetailViewModel::class.java.simpleName

    @Inject
    lateinit var gamePointApi: GamePointApi

    private  var subscription: Disposable? = null

    val postedDealSucessfully: MutableLiveData<DealRedeemModel> = MutableLiveData()
    val errorPostingDeal: MutableLiveData<String> = MutableLiveData()


    init {

    }

    private fun postDealRedeems(bearerToken: String, dealBody: DealBody) {
        Log.e("JMG", "dealBody: " + dealBody)
        val redeemDealBody = RedeemDealBody(
            deal_redeem = dealBody
        )
        Log.e("JMG", "redeemDealBody: " + redeemDealBody)
        subscription = gamePointApi.postDealRedeems("Bearer $bearerToken", redeemDealBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onGetDealsStart() }
                .doOnTerminate { onGetDealsFinish() }
                .subscribe({ t ->
                    onGetDealsSuccess(t)
                }, { error ->
                    var message = "Something went wrong, please try again"
                    if (error is HttpException) {
                        val errorJsonString = error.response()?.errorBody()?.string()
                        Log.e("JMG", "errorJsonString: " + errorJsonString)
                        try {
                            val arr  = JsonParser().parse(errorJsonString)
                                .asJsonObject["errors"]
                                .asJsonArray
                            if (arr.size() > 0) {
                                message = arr.get(0).toString()
                                Log.e("JMG", "errorBody: " + message)
                                onGetDealsError(message)
                            }
                        } catch (e: Exception) {
                            try {
                                val arr  = JsonParser().parse(errorJsonString)
                                Log.e("JMG", "arr: " + arr)
                                error.message?.let {
                                    onGetDealsError(arr.toString())
                                }
                            } catch (e2: Exception) {
                                error.message?.let {
                                    onGetDealsError(error.message())
                                }
                            }
                        }
                    } else {
                        error.message?.let {
                            onGetDealsError(error.message)
                        }
                    }

                })
    }

    private fun onGetDealsError(it: String?) {
        Log.e(TAG, "onPostDealsError =======> could not post deal $it")
        errorPostingDeal.value = it!!
    }

    private fun onGetDealsSuccess(t: DealRedeemModel?) {
        Log.i(TAG, "onGetDealsSuccess ========> ${t.toString()}")
        postedDealSucessfully.value = t!!
    }

    private fun onGetDealsFinish() {
        Log.i(TAG, "onGetDealsFinish =======> finished")
    }

    private fun onGetDealsStart() {
        Log.i(TAG, "onGetDealsStart =======> started")
    }

    fun postDeal(bearerToken: String, dealBody: DealBody) {
        postDealRedeems(bearerToken, dealBody)
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }


}