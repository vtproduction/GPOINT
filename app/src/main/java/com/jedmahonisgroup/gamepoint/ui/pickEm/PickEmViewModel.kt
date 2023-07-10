package com.jedmahonisgroup.gamepoint.ui.pickEm

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.jedmahonisgroup.gamepoint.BuildConfig
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.database.GameShowDao
import com.jedmahonisgroup.gamepoint.database.UserDao
import com.jedmahonisgroup.gamepoint.helpers.SharedPreferencesHelper
import com.jedmahonisgroup.gamepoint.model.StateNotifier
import com.jedmahonisgroup.gamepoint.model.gameshow.*
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import com.jedmahonisgroup.gamepoint.ui.MainViewModel
import com.jedmahonisgroup.gamepoint.utils.LogUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject


/**
 * Created by nienle on 29,December,2022
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class PickEmViewModel(private var userDao: UserDao, private var gameShowDao: GameShowDao) : BaseViewModel() {

    private lateinit var subscription: Disposable
    private var TAG: String = PickEmViewModel::class.java.simpleName

    @Inject
    lateinit var gamePointApi: GamePointApi

    @Inject lateinit var sharedPreferencesHelper: SharedPreferencesHelper



    var url: String = BuildConfig.BASE_URL_2

    companion object {

    }


    val gameShowFromServer: MutableLiveData<StateNotifier<Pair<Int, GameShow>>> = MutableLiveData()
    val gameShowListFromServer: MutableLiveData<StateNotifier<List<GameShow>>> = MutableLiveData()
    val joinGameShow: MutableLiveData<StateNotifier<Int>> = MutableLiveData()
    val joinGameResponseMessage: MutableLiveData<Pair<Boolean, String?>> = MutableLiveData()
    val joinGameShowResponse: MutableLiveData<JoinGameShowResponse?> = MutableLiveData()
    val gameShowResult: MutableLiveData<StateNotifier<GameShowResult?>> = MutableLiveData()

    private var breakerValue: Int = -1
    private var wager = -1
    private var answers: MutableList<PostQuestion> = arrayListOf()


    init {
        //gameShowFromServer.value = StateNotifier.loading()
        gameShowListFromServer.value = StateNotifier.loading()
    }

    fun onSetBreakerValue(value: Int) { breakerValue = value }

    fun updateAnswers(position: Int, answersList: List<Boolean>){
        val question = answers.elementAt(position)

        if (question.answers.size != answersList.size) return
        question.answers.forEachIndexed { index, postAnswer -> postAnswer.isPlayerSelected = answersList[index] }

        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()
        LogUtil.d("PickEmViewModel > updateAnswers > 78: \n${gson.toJson(answers)}")
    }


    fun onSubmitAnswers() {

        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()
        LogUtil.d("PickEmViewModel > onSubmitAnswers > 86: \n${gson.toJson(answers)}")

        val game = gameShowFromServer.value?.data?.second
        val joinedGameData = joinGameShowResponse.value  //sharedPreferencesHelper.getJoinedGameData()
        val token = sharedPreferencesHelper.getAccountToken()
        if (token == null){
            joinGameResponseMessage.value = Pair(false, "Invalid user token")
            return
        }

        if (game == null || joinedGameData == null){
            joinGameResponseMessage.value = Pair(false, "Invalid game data")
            return
        }

        if (breakerValue < game.gameBreakerFrom || breakerValue > game.gameBreakerTo){
            joinGameResponseMessage.value = Pair(false, "Invalid game breaker value, must be between ${game.gameBreakerFrom} and ${game.gameBreakerTo}")
            return
        }

        answers.forEachIndexed {index, postQuestion ->
            if (postQuestion.answers.none { a -> a.isPlayerSelected }) {
                joinGameResponseMessage.value = Pair(false, "Must select at least one answer at question number ${index + 1}")
                return
            }
        }

        subscription = gamePointApi.submitGameAnswers("$url/api/gameshow/submit", token, joinedGameData.joinedId, breakerValue, answers)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {  }
            .doOnTerminate {  }
            .subscribe({ data ->
                LogUtil.d("PickEmViewModel > onSubmitAnswers > 113: $data")
                if (data.status){
                    joinGameResponseMessage.value = Pair(true, data.data?.message ?: "")
                }else{
                    joinGameResponseMessage.value = Pair(false, data.message ?: "")
                }
            }, {error ->

                LogUtil.d("PickEmViewModel > onSubmitAnswers > 117: $error")
                joinGameResponseMessage.value = Pair(false, error.message)
            })
    }



    fun joinGameShowAction(bearerToken: String, game: GameShow, wager: Int){
        this.wager = wager
        LogUtil.d("PickEmViewModel > joinGameShow > 52: $bearerToken - ${game.id} - $wager")
        subscription = gamePointApi.joinGameShow("$url/api/gameshow/join", bearerToken, game.id, wager)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {  }
            .doOnTerminate {  }
            .subscribe({ data -> handleJoinGame(data.data) }, { error -> handleError(error, joinGameShow)})
    }


    fun getGameShowResult(bearerToken: String, gameShowId: Int){
        LogUtil.d("PickEmViewModel > getGameShowResult > 147: ")
        subscription = gamePointApi.getGameShowResult("$url/api/gameshow/result", bearerToken,gameShowId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { gameShowResult.value = StateNotifier.loading() }
            .doOnTerminate {  }
            .subscribe({ data ->
                if (data.status){
                    gameShowResult.value = StateNotifier.success(data.data)
                }else{
                    gameShowResult.value = StateNotifier.failed(Exception(data.message))
                }
            }, { error -> handleError(error, gameShowResult)})
    }

    fun reJoinGameShowAction(bearerToken: String, game: GameShow, wager: Int){
        this.wager = wager
        LogUtil.d("PickEmViewModel > joinGameShow > 52: $bearerToken - ${game.id} - $wager")
        subscription = gamePointApi.joinGameShow("$url/api/gameshow/join", bearerToken, game.id, wager)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {  }
            .doOnTerminate {  }
            .subscribe({ data -> handleReJoinGame(game,data.data)}, {error -> handleError(error, joinGameShow)})
    }

    fun getGameShowList(bearerToken: String){
        LogUtil.d("PickEmViewModel > getGameShowList > 144: ")
        subscription = gamePointApi.getGameShowList("$url/api/gameshows",bearerToken)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { gameShowListFromServer.value = StateNotifier.loading() }
            .doOnTerminate {  }
            .subscribe({ t ->
                LogUtil.d("PickEmViewModel > getGameShowList > 151: ${t.data.size}")

                onGetGameShowListSuccess(t.data)
            }, { error ->
                handleError(error, gameShowFromServer)
            })
    }

    fun getGameShow(bearerToken: String, checkJoinGame: Boolean = true){
        Log.d("DEBUG", "getGameShow: $bearerToken $url")
        subscription = gamePointApi.getGameShowList("$url/api/gameshows",bearerToken)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { gameShowFromServer.value = StateNotifier.loading() }
            .doOnTerminate {  }
            .subscribe({ t ->

                onGetGameShowSuccess(t.data)

            }, { error ->
                handleError(error, gameShowFromServer)
            })
    }

    fun getActiveGameShow(bearerToken: String, checkJoinGame: Boolean = true){
        Log.d("DEBUG", "getGameShow: $bearerToken $url")
        subscription = gamePointApi.getGameShowList("$url/api/gameshow",bearerToken)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { gameShowFromServer.value = StateNotifier.loading() }
            .doOnTerminate {  }
            .subscribe({ t ->

                onGetGameShowSuccess(t.data)

            }, { error ->
                handleError(error, gameShowFromServer)
            })
    }

    private fun<T> handleError(error: Throwable?, dispatcher: MutableLiveData<StateNotifier<T>>){
        var message = ""
        if (error is HttpException) {
            val errorJsonString = error.response()?.errorBody()?.string()
            try {
                val arr = JsonParser().parse(errorJsonString)
                    .asJsonObject["errors"]
                    .asJsonArray
                if (arr.size() > 0) {
                    message = arr.get(0).toString()
                    dispatcher.value = StateNotifier.failed(error)
                }
            } catch (e: Exception) {
                error.message?.let {
                    dispatcher.value = StateNotifier.failed(Throwable(it))
                }
            }
        } else {
            error?.message?.let {
                dispatcher.value = StateNotifier.failed(error)
            }
        }
    }

    /*
    {
      "status": true,
      "joinedId": 9,
      "gameShowId": 14,
      "player1": {
        "playerId": 6,
        "name": "Giang Nguyễn"
      },
      "player2": {
        "playerId": 7,
        "name": "NIEN2 LE2"
      }
    }
     */

    private fun handleReJoinGame(game: GameShow, response: JoinGameShowResponse) {

        try{
            answers.clear()
            game.questions.forEach { question ->
                answers.add(PostQuestion.build(question))
            }
            gameShowFromServer.value = StateNotifier.success(Pair(0, game))
            joinGameShowResponse.value = response
        }catch(t: Throwable){
            LogUtil.e("PickEmViewModel > handleJoinGame > 146: ${t.localizedMessage}")
        }
    }

    private fun handleJoinGame(response: JoinGameShowResponse) {
        LogUtil.d("PickEmViewModel > handleJoinGame > 241: $response")
        try{
            val game = gameShowFromServer.value?.data?.second ?: return
            if (response.status) { //first time join game
                response.wager = wager
                sharedPreferencesHelper.saveJoinedGameData(response)
                sharedPreferencesHelper.saveJoinedGame(game.id)
                gameShowFromServer.value = StateNotifier.success(Pair(0, game))

                joinGameShowResponse.value = response
            }else{
                if (response.message != null){
                    joinGameResponseMessage.value = Pair(true, response.message)
                }

                if (response.gameShowId + response.joinedId > 0){ //already joined
                    sharedPreferencesHelper.saveJoinedGame(game.id)
                    val d = sharedPreferencesHelper.getJoinedGameData()
                    if (d != null){
                        joinGameShowResponse.value = d
                    }

                    gameShowFromServer.value = StateNotifier.success(Pair(0, game))
                }else{
                    gameShowFromServer.value = StateNotifier.success(Pair(1, game))
                }
            }
        }catch(t: Throwable){
            LogUtil.e("PickEmViewModel > handleJoinGame > 146: ${t.localizedMessage}")
        }
    }

    private fun onGetGameShowListSuccess(response: List<GameShowResponse>){
        if (response.isEmpty()) return

        val list = response.map { i -> GameShow(i) }

        gameShowListFromServer.value = StateNotifier.success(list)
    }

    private fun onGetGameShowSuccess(response: List<GameShowResponse>) {

        if (response.isEmpty()) return
        val r = response.first()
        val g = GameShow(r)

        answers.clear()
        g.questions.forEach { question ->
            answers.add(PostQuestion.build(question))
        }

        if (isJoiningCurrentGame(g)){
            val d = sharedPreferencesHelper.getJoinedGameData()
            if (d != null && d.gameShowId == g.id){
                joinGameShowResponse.value = d
            }
            gameShowFromServer.value = StateNotifier.success(Pair(0, g))
        }else{
            sharedPreferencesHelper.clearJoinedGame()
            sharedPreferencesHelper.clearJoinedGameData()
            gameShowFromServer.value = StateNotifier.success(Pair(1, g))
        }


        //Log.i(TAG, "get game show resultsFromServer $response")

    }

    private fun isJoiningCurrentGame(gameShow: GameShow) = gameShow.gameShowStatus == GameShow.Status.JOINED






    override fun onCleared() {
        super.onCleared()
        try {
            subscription.dispose()
        } catch (e: Exception) {
            Log.e("JMG", "e onCleared MainViewModel: " + e.localizedMessage)
        }
    }

    fun goToGameShow(data: GameShow, status: GameShow.Status) {
        var stt = 0
        if (status == GameShow.Status.OPEN) {
            stt = 1
            gameShowFromServer.value = StateNotifier.success(Pair(stt, data))
            return
        }

        //gameShowFromServer.value = StateNotifier.success(Pair(stt, data))
    }
}