package com.jedmahonisgroup.gamepoint.ui.picks

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.database.ResultsDao
import com.jedmahonisgroup.gamepoint.database.model.DatabaseResultsModel
import com.jedmahonisgroup.gamepoint.model.picks.MyPicksModel
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class ResultsViewModel(private val resultsDao: ResultsDao) : BaseViewModel() {
    private var TAG: String = ResultsViewModel::class.java.simpleName

    private  var subscription: Disposable? = null

    val resultsFromServer: MutableLiveData<List<MyPicksModel>> = MutableLiveData()
    val resultsFromServerFailed: MutableLiveData<String> = MutableLiveData()

    val resultsFromDb: MutableLiveData<List<MyPicksModel>> = MutableLiveData()
    val failedResultsFromDb: MutableLiveData<String> = MutableLiveData()

    val errorSavingResults: MutableLiveData<String> = MutableLiveData()

    init {

    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }

    @Inject
    lateinit var gamePointApi: GamePointApi

    /**
     * get results from server
     */
    private fun getPicks(bearerToken: String) {
        subscription = gamePointApi.getPicks("Bearer $bearerToken")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onGetDataStart() }
                .doOnTerminate { onGetDataFinish() }
                .subscribe({ t ->
                    onGetDataSuccess(t.my_picks)
                }, { error ->
                    var message = "Something went wrong, please try again"
                    if (error is HttpException) {
                        val errorJsonString = error.response()?.errorBody()?.string()
                        val arr  = JsonParser().parse(errorJsonString)
                                .asJsonObject["errors"]
                                .asJsonArray
                        if (arr.size() > 0) {
                            message = arr.get(0).toString()
                            onGetDataError(message)
                        }
                    } else {
                        error.message?.let {
                            onGetDataError(it)
                        }
                    }
                })
    }

    private fun onGetDataError(error: String) {
        Log.e(TAG, "error loading results from server $error")
        resultsFromServerFailed.value = error
    }

    private fun onGetDataSuccess(response: List<MyPicksModel>) {
        val gson = Gson()
        resultsFromServer.value = response
        Log.i(TAG, "results from server$response")

        val resultsString = gson.toJson(response)

        val results = DatabaseResultsModel(
                id = 1,
                results = resultsString
        )
        saveResultsToDb(results)
    }

    private fun onGetDataFinish() {
        Log.i(TAG, "onGetResultsFinish")
    }

    private fun onGetDataStart() {
        Log.i(TAG, "onGetResultsStart")
    }

    /**
     * get results from db
     */

    private fun getResultsFromDb() {
        subscription = Observable.fromCallable { resultsDao.getResults.results }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onGetResultsFromDb(it)
                },
                        {
                            onGetResultsFromDbError(it)
                        }
                )
    }

    private fun onGetResultsFromDb(it: String?) {
        Log.i(TAG, "onGetPicksFromDb =====> resultsFromServer reading picks from database: $it")
        val gson = Gson()
        val type = object : TypeToken<ArrayList<MyPicksModel>>() {}.type
        val results = gson.fromJson<ArrayList<MyPicksModel>>(it, type)
        resultsFromDb.value = results
    }

    private fun onGetResultsFromDbError(it: Throwable?) {
        Log.e(TAG, "onGetResultsFromDbError =====> error reading results from database: $it")
        failedResultsFromDb.value = it.toString()
    }

    /**
     * save to db
     */
    @SuppressLint("CheckResult")
    private fun saveResultsToDb(results: DatabaseResultsModel) {
        Observable.fromCallable {resultsDao.insertResults(results) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onSaveResultsToDbSuccess() },
                        { onSaveResultsToDbError(it) }
                )
    }

    private fun onSaveResultsToDbSuccess() {
        Log.i(TAG, "onSaveResultsToDbSuccess =====> saved results to database")
    }

    private fun onSaveResultsToDbError(it: Throwable?) {
        Log.e(TAG, "onSaveResultsToDbError =======> error saving results to database: ${it.toString()}")
        errorSavingResults.value = it.toString()
    }

    fun getPicksFromServer(bearerToken: String) {
        getPicks(bearerToken)
    }

    fun getDbResults() {
        getResultsFromDb()
    }
}