package com.jedmahonisgroup.gamepoint.ui.leaderboard

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.jedmahonisgroup.gamepoint.BuildConfig
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.database.PointsDao
import com.jedmahonisgroup.gamepoint.database.model.DatabasePointsModel
import com.jedmahonisgroup.gamepoint.helpers.SharedPreferencesHelper
import com.jedmahonisgroup.gamepoint.model.StateNotifier
import com.jedmahonisgroup.gamepoint.model.gameshowLeaderboard.GameShowLeaderboardResponse
import com.jedmahonisgroup.gamepoint.model.leaderboard.EventLeaderboardModel
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import com.jedmahonisgroup.gamepoint.utils.LogUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject


class GameShowLeaderBoardViewModel() : BaseViewModel() {
    private var TAG: String = GameShowLeaderBoardViewModel::class.java.simpleName
    var url: String = BuildConfig.BASE_URL_2
    private lateinit var subscription: Disposable

    val gameShowLeaderBoardData : MutableLiveData<StateNotifier<GameShowLeaderboardResponse>> = MutableLiveData()

    init {
    }

    override fun onCleared() {
        super.onCleared()
        subscription.dispose()
    }

    @Inject
    lateinit var gamePointApi: GamePointApi

    @Inject lateinit var helper: SharedPreferencesHelper

     fun getLeaderBoardData() {

        val token = helper.getAccountToken() ?: ""
         LogUtil.d("GameShowLeaderBoardViewModel > getLeaderBoardData > 49: $token")

        subscription = gamePointApi.getGameShowLeaderBoard("$url/api/gameshow/leaderboard",token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { gameShowLeaderBoardData.value = StateNotifier.loading() }

                .subscribe({ t ->
                    LogUtil.d("GameShowLeaderBoardViewModel > getLeaderBoardData > 57: $t")
                    if (t.status){
                        onGetDataSuccess(t)
                    }else{
                        onGetLeaderboardError(t.message ?: "Unknown Error!")
                    }
                }, { error ->
                    LogUtil.d("GameShowLeaderBoardViewModel > getLeaderBoardData > 57: $error")
                    gameShowLeaderBoardData.value = StateNotifier.failed(Exception("Error when fetching data: ${error.message}"))

                })
    }

    private fun onGetLeaderboardError(it: String) {
        Log.e("$TAG GetLeaderboardError: ", it)
        gameShowLeaderBoardData.value = StateNotifier.failed(Exception("Error when fetching data: $it"))
    }

    private fun onGetDataSuccess(data: GameShowLeaderboardResponse){
        LogUtil.d("GameShowLeaderBoardViewModel > onGetDataSuccess > 76: $data")
        gameShowLeaderBoardData.value = StateNotifier.success(data)
    }

}