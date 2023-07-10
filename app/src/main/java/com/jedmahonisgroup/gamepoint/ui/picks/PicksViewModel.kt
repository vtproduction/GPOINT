package com.jedmahonisgroup.gamepoint.ui.picks

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.database.PicksDao
import com.jedmahonisgroup.gamepoint.database.model.DatabasePicksModel
import com.jedmahonisgroup.gamepoint.model.picks.OpenPicksModel
import com.jedmahonisgroup.gamepoint.model.picks.Picks
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class PicksViewModel(private val picksDao: PicksDao) : BaseViewModel() {
    private var TAG: String = PicksViewModel::class.java.simpleName

    private lateinit var subscription: Disposable

    //get deals
    val picksFromServer: MutableLiveData<List<OpenPicksModel>> = MutableLiveData()
    val picksTriviaFromServer: MutableLiveData<List<OpenPicksModel>> = MutableLiveData()
    val serverDataFailed: MutableLiveData<String> = MutableLiveData()
    val serverTriviaDataFailed: MutableLiveData<String> = MutableLiveData()

    val picksFromDb: MutableLiveData<List<OpenPicksModel>> = MutableLiveData()
    val failedPicksFromDb: MutableLiveData<String> = MutableLiveData()

    val errorSavingPicks: MutableLiveData<String> = MutableLiveData()

    //post deals
    val postSuccess: MutableLiveData<OpenPicksModel> = MutableLiveData()
    val postFail: MutableLiveData<String> = MutableLiveData()


    init {

    }

    override fun onCleared() {
        super.onCleared()
        subscription.dispose()
    }

    @Inject
    lateinit var gamePointApi: GamePointApi

    private fun getPicksFromServer(bearerToken: String) {

        subscription = gamePointApi.getPicks("Bearer $bearerToken")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onGetDataStart() }
                .doOnTerminate { onGetDataFinish() }
                .subscribe({ t ->
                    Log.e(TAG, "here ${t.open_picks}")
                    onGetDataSuccess(t.open_picks)
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
        Log.e("$TAG picks model error", error)
        serverDataFailed.value = error
    }

    private fun onGetDataSuccess(response: List<OpenPicksModel>) {
        val gson = Gson()
        picksFromServer.value = response

        Log.i(TAG, "get picks resultsFromServer $response")

        val picksString = gson.toJson(response)


        val picks = DatabasePicksModel(
                id = 1,
                picks = picksString
        )
        saveStreaksToDb(picks)
    }

    private fun onGetDataFinish() {
        Log.i("$TAG", "onGetDataFinish")
    }

    private fun onGetDataStart() {
        Log.i("$TAG", "onGetDataStart")
    }

    private fun getTriviaPicksFromServer(bearerToken: String) {

        subscription = gamePointApi.getTriviaPicks("Bearer $bearerToken")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onGetTriviaDataStart() }
            .doOnTerminate { onGetTriviaDataFinish() }
            .subscribe({ t ->
                Log.e(TAG, "here ${t.open_picks}")
                onGetTriviaDataSuccess(t.open_picks)
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
                            onGetTriviaDataError(message)
                        }
                    } catch (e: Exception) {
                        error.message?.let {
                            onGetTriviaDataError(it)
                        }
                    }
                } else {
                    error.message?.let {
                        onGetDataError(it)
                    }
                }
            })
    }


    private fun onGetTriviaDataError(error: String) {
        Log.e("$TAG picks model error", error)
        serverTriviaDataFailed.value = error
    }

    private fun onGetTriviaDataSuccess(response: List<OpenPicksModel>) {
        val gson = Gson()
        picksTriviaFromServer.value = response

        Log.i(TAG, "get picks resultsFromServer $response")

        val picksString = gson.toJson(response)


        val picks = DatabasePicksModel(
            id = 1,
            picks = picksString
        )
        saveStreaksToDb(picks)
    }

    private fun onGetTriviaDataFinish() {
        Log.i("$TAG", "onGetDataFinish")
    }

    private fun onGetTriviaDataStart() {
        Log.i("$TAG", "onGetDataStart")
    }

    /**
     * get data from database
     */

    private fun getPicksFromDatabase() {
        subscription = Observable.fromCallable { picksDao.getPicks.picks }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onGetPicksFromDb(it)
                },
                        {
                            onGetPicksFromDbError(it)
                        }
                )
    }

    private fun onGetPicksFromDb(it: String?) {
        Log.i(TAG, "onGetPicksFromDb =====> resultsFromServer reading picks from database: $it")
        val gson = Gson()
        val type = object : TypeToken<ArrayList<OpenPicksModel>>() {}.type
        val picks = gson.fromJson<ArrayList<OpenPicksModel>>(it, type)
        picksFromDb.value = picks
    }

    private fun onGetPicksFromDbError(it: Throwable?) {
        Log.e(TAG, "onGetStreakFromDbError =====> error reading deals from database: $it")
        failedPicksFromDb.value = it.toString()
    }

    /**
     * Save Picks to DB
     */
    @SuppressLint("CheckResult")
    private fun saveStreaksToDb(picks: DatabasePicksModel) {
        Observable.fromCallable {picksDao.inserPicks(picks) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onSavePicksToDbSuccess() },
                        { onSavePicksToDbError(it) }
                )
    }

    private fun onSavePicksToDbSuccess() {
        Log.i(TAG, "onSavePicksToDbSuccess =====> saved picks to database")

    }

    private fun onSavePicksToDbError(it: Throwable?) {
        Log.e(TAG, "onSavePicksToDbError =======> error saving Picks to database: ${it.toString()}")
        errorSavingPicks.value = it.toString()    }

    /**
     * Posting the user pick
     */
    private fun postPicks(bearerToken: String, userPick: Picks) {
        val gson: Gson = Gson()
        val userString = gson.toJson(userPick)
        Log.e("JMG", "userPick: " + userPick)
        subscription = gamePointApi.postUserPicks("application/json", "Bearer $bearerToken", userPick)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onPostDataStart() }
                .doOnTerminate { onPostDataFinish() }
                .subscribe({ t ->
                    onPostDataSuccess(t)
                }, { error ->

                    var message = "Something went wrong, please try again"
                    if (error is HttpException) {
                        val errorJsonString = error.response()?.errorBody()?.string()
                        val arr  = JsonParser().parse(errorJsonString)
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

    private fun onPostDataSuccess(t: OpenPicksModel) {
        Log.e("$TAG onPostDataSuccess", t.toString())
//        getPicksFromDatabase();
    }

    private fun onPostDataError(it: String) {
        Log.e("$TAG onPostDataError", it)
    }


    fun getMyPicksFromServer(bearerToken: String) {
        getPicksFromServer(bearerToken)
    }

    fun getMyTriviaPicksFromServer(bearerToken: String) {
        getTriviaPicksFromServer(bearerToken)
    }

    fun postUserPicks(bearerToken: String, userPick: Picks) {
        postPicks(bearerToken, userPick)
    }

    fun getPicksDb() {
        getPicksFromDatabase()
    }

     fun updatePickInDB(t: OpenPicksModel) {
         val gson = Gson()
         val picksString = gson.toJson(t)


         val picks = DatabasePicksModel(
                 id = 1,
                 picks = picksString
         )
         Observable.fromCallable {picksDao.updatePicks(picks) }
                 .subscribeOn(Schedulers.computation())
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe({ onSavePicksToDbSuccess() },
                         { onSavePicksToDbError(it) }
                 )
    }
}