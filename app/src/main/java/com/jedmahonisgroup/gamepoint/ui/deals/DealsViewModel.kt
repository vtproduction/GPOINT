package com.jedmahonisgroup.gamepoint.ui.deals

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.database.DealsDao
import com.jedmahonisgroup.gamepoint.database.model.DatabaseDealsModel
import com.jedmahonisgroup.gamepoint.model.DealModel
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class DealsViewModel(private val dealsDao: DealsDao) : BaseViewModel()  {


    @Inject
    lateinit var gamePointApi: GamePointApi

    private var TAG: String = DealsViewModel::class.java.simpleName

    val dealsData: MutableLiveData<List<DealModel>> = MutableLiveData()
    val errorFetchingDealsData: MutableLiveData<String> = MutableLiveData()

    val errorSavingDeals: MutableLiveData<String> = MutableLiveData()

    val successGettingDeals: MutableLiveData<ArrayList<DealModel>> = MutableLiveData()
    val errorGettinDealsFromDb: MutableLiveData<String> = MutableLiveData()

    private lateinit var subscription: Disposable

    init {

    }

    override fun onCleared() {
        super.onCleared()
        subscription.dispose()
    }

    private fun getDealsFromServer(bearerToken: String) {
        subscription = gamePointApi.getDeals("Bearer $bearerToken")
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
                        val arr  = JsonParser().parse(errorJsonString)
                                .asJsonObject["errors"]
                                .asJsonArray
                        if (arr.size() > 0) {
                            message = arr.get(0).toString()
                            onGetDealsError(message)
                        }
                    } else {
                        error.message?.let {
                            onGetDealsError(it)
                        }
                    }

                })
    }

    private fun onGetDealsError(it: String) {
        Log.e(TAG, " GetDealsError: $it")
        errorFetchingDealsData.value = it
    }

    private fun onGetDealsSuccess(deals: List<DealModel>) {
        val gson: Gson = Gson()

//        Log.e("$TAG GetDealsSuccess:", deals.toString())
        Log.e("JMG", "onGetDealsSuccess")
        dealsData.value = deals

        val  dealsString = gson.toJson(deals)

        val databaseModel = DatabaseDealsModel(
                id = 1,
                deals = dealsString
        )
        saveDealsToDataBase(databaseModel)
    }

    private fun onGetDealsFinish() {
        Log.i("$TAG GetDealsFinish: ", "Finished")
    }

    private fun onGetDealsStart() {
        Log.e("$TAG GetDealsStart: ", "Started")
    }

    private fun getDealsFromDataBase() {
        subscription = Observable.fromCallable {dealsDao.getDeals.deals }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onGetDealsFromDbSuccess(it) },
                        {
                            onGetDealsFromDbError(it) }
                )
    }

    private fun onGetDealsFromDbError(it: Throwable?) {
        Log.e(TAG, "onGetDealsFromDbError =====> error reading deals from database: $it")
        errorGettinDealsFromDb.value = it.toString()

    }

    private fun onGetDealsFromDbSuccess(deals: String?) {
        Log.i(TAG, "onGetDealsFromDbSuccess =====> resultsFromServer reading deals from database: $deals")
        val gson = Gson()
        val type = object : TypeToken<ArrayList<DealModel>>() {}.type
        val deals = gson.fromJson<ArrayList<DealModel>>(deals, type)

        successGettingDeals.value = deals
    }

    @SuppressLint("CheckResult")
    private fun saveDealsToDataBase(deals: DatabaseDealsModel) {
        Observable.fromCallable {dealsDao.insertUser(deals)}
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({onSaveDealsToDbSuccess()},
                        { onSaveDealsToDbError(it) }
                )
    }

    private fun onSaveDealsToDbError(it: Throwable?) {
        Log.e(TAG, "onSaveDealsToDbError =======> error saving deals to database: ${it.toString()}")
        errorSavingDeals.value = it.toString()
    }

    private fun onSaveDealsToDbSuccess() {
        Log.i(TAG, "onSaveDealsToDbSuccess =====> saved deals to database")
    }


    fun getDeals(bearerToken: String) {
        getDealsFromServer(bearerToken)
    }

    fun getDealsFromDb(){
        getDealsFromDataBase()
    }

}