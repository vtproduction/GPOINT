package com.jedmahonisgroup.gamepoint.ui.leaderboard

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.database.PointsDao
import com.jedmahonisgroup.gamepoint.database.model.DatabasePointsModel
import com.jedmahonisgroup.gamepoint.model.leaderboard.EventLeaderboardModel
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class PointsLeaderBoardViewModel(private val pointsDao: PointsDao) : BaseViewModel() {
    private var TAG: String = PointsLeaderBoardViewModel::class.java.simpleName

    private lateinit var subscription: Disposable

    val leaderboardData: MutableLiveData<List<EventLeaderboardModel>> = MutableLiveData()
    val errorFetchingLeaderboardData: MutableLiveData<String> = MutableLiveData()

    val pointFromDb: MutableLiveData<List<EventLeaderboardModel>> = MutableLiveData()
    val failedPointsFromDb: MutableLiveData<String> = MutableLiveData()

    val errorSavingPointsToDb: MutableLiveData<String> = MutableLiveData()

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
                    onGetLeaderboardSuccess(t.event_leaderboard)
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

    private fun onGetLeaderboardSuccess(response: List<EventLeaderboardModel>) {
        val gson = Gson()

        Log.e("$TAG GetLeaderboardSuccess:", response.toString())
        leaderboardData.value = response

        val pointsString = gson.toJson(response)
        val points= DatabasePointsModel(
                id = 1,
                points = pointsString
        )
        saveStreaksToDb(points)

    }

    private fun onGetLeaderboardFinish() {
        Log.e("$TAG GetLeaderboardFinish: ", "Finished")
    }

    private fun onGetLeaderboardStart() {
        Log.e("$TAG GetLeaderboardStart: ", "Started")
    }

    fun getEventLeaderboardData(bearerToken: String) {
        getLeaderboardFromServer(bearerToken)
    }

    private fun getStreakFromDatabase() {
        subscription = Observable.fromCallable {pointsDao.getPoints.points }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onGetPointsFromDb(it)
                },
                        {
                            onGetPointsFromDbError(it)
                        }
                )
    }

    private fun onGetPointsFromDbError(it: Any) {
        Log.e(TAG, "onGetStreakFromDbError =====> error reading deals from database: $it")
        failedPointsFromDb.value = it.toString()
    }

    private fun onGetPointsFromDb(it: String) {
        Log.i(TAG, "onGetStreakFromDb =====> resultsFromServer reading streak from database: $it")
        val gson = Gson()
        val type = object : TypeToken<ArrayList<EventLeaderboardModel>>() {}.type
        val points = gson.fromJson<ArrayList<EventLeaderboardModel>>(it, type)

        pointFromDb.value = points
    }

    @SuppressLint("CheckResult")
    private fun saveStreaksToDb(streaks: DatabasePointsModel) {
        Observable.fromCallable {pointsDao.insertPoints(streaks) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onSavePointsToDbSuccess() },
                        { onSavePointsToDbError(it) }
                )
    }

    private fun onSavePointsToDbSuccess() {
        Log.i(TAG, "onSavePointsToDbSuccess =====> saved points to database")

    }

    private fun onSavePointsToDbError(it: Throwable) {
        Log.e(TAG, "onSavePointsToDbError =======> error saving points to database: ${it.toString()}")
        errorSavingPointsToDb.value = it.toString()

    }

    fun getPointsFromDb() {
        getStreakFromDatabase()
    }

}