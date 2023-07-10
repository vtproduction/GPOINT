package com.jedmahonisgroup.gamepoint.ui.leaderboard

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.database.StreakDao
import com.jedmahonisgroup.gamepoint.database.model.DatabaseStreakModel
import com.jedmahonisgroup.gamepoint.model.leaderboard.StreakModel
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class StreakViewModel(private val streakDao: StreakDao) : BaseViewModel() {
    private lateinit var subscription: Disposable
    private var TAG: String = StreakViewModel::class.java.simpleName


    val streaksFromServer: MutableLiveData<List<StreakModel>> = MutableLiveData()
    val errorFetchingLeaderboardData: MutableLiveData<String> = MutableLiveData()

    val streaksFromDb: MutableLiveData<List<StreakModel>> = MutableLiveData()
    val failedStreaksFromDb: MutableLiveData<String> = MutableLiveData()

    val errorSavingStreaks: MutableLiveData<String> = MutableLiveData()


    init {

    }

    override fun onCleared() {
        super.onCleared()
        subscription.dispose()
    }

    @Inject
    lateinit var gamePointApi: GamePointApi

    private fun getLeaderboardFromServer(bearerToken: String) {
        subscription = gamePointApi.getLeaderboard("Bearer $bearerToken")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onGetLeaderboardStart() }
                .doOnTerminate { onGetLeaderboardFinish() }
                .subscribe({ t ->
                    onGetLeaderboardSuccess(t.pick_leaderboard)
                }, { error ->
                    var message = "Something went wrong, please try again"
                    if (error is HttpException) {
                        val errorJsonString = error.response()?.errorBody()?.string()
                        val arr  = JsonParser().parse(errorJsonString)
                                .asJsonObject["errors"]
                                .asJsonArray
                        if (arr.size() > 0) {
                            message = arr.get(0).toString()
                            onGetLeaderboardError(message)
                        }
                    } else {
                        error.message?.let {
                            onGetLeaderboardError(it)
                        }
                    }

                })
    }

    private fun onGetLeaderboardError(it: String) {
        Log.e("$TAG GetLeaderboardError: ", it)
        errorFetchingLeaderboardData.value = it
    }

    private fun onGetLeaderboardSuccess(response: List<StreakModel>) {
        val gson = Gson()
        Log.e(TAG, "GetLeaderboardSuccess $response")
        streaksFromServer.value = response

        val streakString = gson.toJson(response)

        val streaks = DatabaseStreakModel(
                id = 1,
                streak = streakString
        )
        saveStreaksToDb(streaks)
    }

    private fun onGetLeaderboardFinish() {
        Log.e("$TAG GetLeaderboardFinish: ", "Finished")
    }

    private fun onGetLeaderboardStart() {
        Log.e("$TAG GetLeaderboardStart: ", "Started")
    }

    private fun getStreakFromDatabase() {
        subscription = Observable.fromCallable { streakDao.getStreak.streak }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onGetStreakFromDb(it)
                },
                        {
                            onGetStreakFromDbError(it)
                        }
                )
    }

    private fun onGetStreakFromDbError(it: Throwable?) {
        Log.e(TAG, "onGetStreakFromDbError =====> error reading deals from database: $it")
        failedStreaksFromDb.value = it.toString()
    }

    private fun onGetStreakFromDb(it: String) {
        Log.i(TAG, "onGetStreakFromDb =====> resultsFromServer reading streak from database: $it")
        val gson = Gson()
        val type = object : TypeToken<ArrayList<StreakModel>>() {}.type
        val streaks = gson.fromJson<ArrayList<StreakModel>>(it, type)

        streaksFromDb.value = streaks
    }

    @SuppressLint("CheckResult")
    private fun saveStreaksToDb(streaks: DatabaseStreakModel) {
        Observable.fromCallable { streakDao.insertStreak(streaks) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onSaveStreakToDbSuccess() },
                        { onSaveStreakToDbError(it) }
                )
    }

    private fun onSaveStreakToDbError(it: Throwable?) {
        Log.e(TAG, "onSaveStreakToDbError =======> error saving streaks to database: ${it.toString()}")
        errorSavingStreaks.value = it.toString()
    }

    private fun onSaveStreakToDbSuccess() {
        Log.i(TAG, "onSaveStreakToDbSuccess =====> saved deals to database")
    }

    fun getPickLeaderboardData(bearerToken: String) {
        getLeaderboardFromServer(bearerToken)
    }

    fun getDbStreak(){
        getStreakFromDatabase()
    }

}