package com.jedmahonisgroup.gamepoint.ui.events

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.database.EventsDao
import com.jedmahonisgroup.gamepoint.database.model.DatabaseEventModel
import com.jedmahonisgroup.gamepoint.database.model.UserDatabaseModel
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.CheckinsResponseModel
import com.jedmahonisgroup.gamepoint.model.RefreshTokenModel
import com.jedmahonisgroup.gamepoint.model.UserResponseModel
import com.jedmahonisgroup.gamepoint.model.events.EventsModel
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import com.jedmahonisgroup.gamepoint.ui.MainViewModel
import com.jedmahonisgroup.gamepoint.utils.Constants
import com.jedmahonisgroup.gamepoint.utils.Constants.PREFS_FILENAME
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

class EventViewModel(private val eventsDao: EventsDao) : BaseViewModel() {

    private lateinit var subscription: Disposable
    private var TAG: String = EventViewModel::class.java.simpleName

    val eventsFromServer: MutableLiveData<List<EventsModel>> = MutableLiveData()
    val errorEventsFromServer: MutableLiveData<String> = MutableLiveData()

    //checkout
    val checkoutSuccess: MutableLiveData<CheckinsResponseModel> = MutableLiveData()
    val errorCheckout: MutableLiveData<String> = MutableLiveData()

    val eventsFromDb: MutableLiveData<List<EventsModel>> = MutableLiveData()
    val failedEventsFromDb: MutableLiveData<String> = MutableLiveData()

    val errorSavingEvents: MutableLiveData<String> = MutableLiveData()

    var makingRequest = false


    init {

    }

    override fun onCleared() {
        super.onCleared()
        try{
            subscription.dispose()
        }catch(t: Throwable){
            
        }
    }

    @Inject
    lateinit var gamePointApi: GamePointApi

    private fun getEventsFromServer(bearerToken: String) {

        if (makingRequest == false) {
            makingRequest = true
            subscription = gamePointApi.getEvents("Bearer $bearerToken")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { onGetEventsStart() }
                    .doOnTerminate { onGetEventsFinish() }
                    .subscribe({ t ->
                        onGetEventsSuccess(t)
                    }, { error ->
                        var message = "Something went wrong, please try again"
                        if (error is HttpException) {
                            val errorJsonString = error.response()?.errorBody()?.string()
                            Log.d("NIEN-TAG", errorJsonString ?: ">>>>")
                            try{
                                val arr  = JsonParser().parse(errorJsonString)
                                    .asJsonObject["errors"]
                                    .asJsonArray
                                if (arr.size() > 0) {
                                    message = arr.get(0).toString()
                                    onGetEventsError(message)
                                }
                            }catch(t: Throwable){
                                onGetEventsError(message)
                            }
                        } else {
                            error.message?.let {
                                onGetEventsError(it)
                            }
                        }

                    })
        }

    }

    private fun     onGetEventsError(it: String) {
        Log.e(TAG,"GetEventsError $it")
//        if (it.contains("HTTP 401")) {
//            val prefs = applicationContext?.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
//            refreshToken(RefreshTokenModel(prefs!!.getString("refresh_token", "")), prefs!!.getString("logged_in_user_id", ""))
//        }
        makingRequest = false
        errorEventsFromServer.value = it
    }

   /* private fun refreshToken(refreshToken: RefreshTokenModel, userId: String) {
        Log.e("JMG", "refreshToken: " + refreshToken + " userId: " + userId)
        subscription = gamePointApi.refreshToken(userId, refreshToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onRefreshTokenStart() }
                .doOnTerminate { onRefreshTokenFinish() }
                .subscribe({
                    onTokenRefreshedSuccess(it)
                }, { error ->
                    error.message?.let {
                        onTokenRefreshError(it)
                    }
                })

    }

    private fun onRefreshTokenStart() {
        Log.i(TAG,"onRefreshTokenStart =========> refreshing token started")
    }

    private fun onRefreshTokenFinish() {
        Log.i(TAG,"onRefreshTokenFinish =========> finished refreshing token")
    }

    private fun onTokenRefreshedSuccess(it: UserResponseModel) {
        Log.e(TAG,"onTokenRefreshedSuccess =========> $it")
        val gSon = Gson()
        val userString = gSon.toJson(response)


        Log.e(TAG, "Token is not expired, carry on.")
        mToken = response.user.login.token
        mLoginModel = response.user.login
        saveLoginModelSp(response.user.login)
        mUser = userString
        val editor = sharedPreferences.edit()
        editor.putString("refresh_token", response.user.login.refresh_token)
        editor.putString("logged_in_user_id", response.user.id.toString())
        editor.apply()
        disposables.add(repository.savePoints(Constants.TOTAL_POINTS_KEY, response.user.redeemable).subscribe())
        updateDatabaseWithUser(it)

    }

    private fun onTokenRefreshError(it: String) {
        Log.e(TAG,"onTokenRefreshError =========> $it")
        tokenRefresherror.value = it
    }

    fun updateDatabaseWithUser(user: UserResponseModel) {
        val gson: Gson = Gson()
        val userString = gson.toJson(user)

        val dbUser = UserDatabaseModel(
                id = 1, response = userString
        )
        UpdateuserToDb(dbUser)
    }

    @SuppressLint("CheckResult")
    private fun UpdateuserToDb(dbUser: UserDatabaseModel) {

        Observable.fromCallable { userDao.updateUser(dbUser) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onWriteUserToDbSuccess(it) },
                        { onWriteUserToDbError(it) }
                )
    }*/

    private fun onGetEventsSuccess(response: List<EventsModel>) {
        makingRequest = false
        val gson = Gson()
        Log.i(TAG, "GetEventsSuccess $response")

        eventsFromServer.value = response


        val eventStr = gson.toJson(response)


        val events = DatabaseEventModel(
                id = 1,
                events = eventStr
        )
        saveEventsToDb(events)
    }

    private fun onGetEventsFinish() {
        Log.i("$TAG onGetEventsFinish: ", "Finished")
    }

    private fun onGetEventsStart() {
        Log.i("$TAG onGetEventsStart: ", "Started")
    }

    /**
     * Get events from db
     */
    private fun getDbEvents(){
        subscription = Observable.fromCallable {eventsDao.getEvents.events}
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onGetEventsFromDb(it)
                },
                        {
                            onGetEventsFromDbError(it)
                        }
                )
    }

    private fun onGetEventsFromDb(it: String?) {
        Log.i(TAG, "onGetEventsFromDb =====>  reading events from database: $it")
        val gson = Gson()
        val type = object : TypeToken<ArrayList<EventsModel>>() {}.type
        val events = gson.fromJson<ArrayList<EventsModel>>(it, type)
        eventsFromDb.value = events
    }

    private fun onGetEventsFromDbError(it: Throwable?) {
        Log.e(TAG, "onGetEventsFromDbError =====> error reading events from database: $it")
        failedEventsFromDb.value = it.toString()    }

    /**
     * Save events to db
     */
    @SuppressLint("CheckResult")
    private fun saveEventsToDb(events: DatabaseEventModel){
        Observable.fromCallable {eventsDao.insertEvents(events) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({onSaveEventsToDbSuccess() },
                        {onSaveEventsToDbError(it) }
                )
    }

    private fun onSaveEventsToDbSuccess() {
        Log.i(TAG, "onSaveEventsToDbSuccess =====> saved events to database")

    }

    private fun onSaveEventsToDbError(it: Throwable?) {
        Log.e(TAG, "onSavePicksToDbError =======> error saving Picks to database: ${it.toString()}")
        errorSavingEvents.value = it.toString()
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
                        Log.e("error response", error.response().toString())
                        if (error.response()!!.code() == 500){
                            onCheckOutError(message)
                        }else {
                            val jObjError = JSONObject(error.response()!!.errorBody()!!.string())
                            var errorString = "An error occurred"
                            try {
                                errorString = jObjError.getJSONArray("errors").get(0).toString()
                            } catch (e: Exception) {
                                try {
                                    errorString = jObjError.getJSONArray("base").get(0).toString()
                                } catch (e: Exception) {
                                    errorString = jObjError.toString()
                                }
                            }
                            onCheckOutError(errorString)
                        }
                    } else {
                        error.message?.let {
                            onCheckOutError(it)
                        }
                    }
                })

//
//                    if (error is HttpException) {
//                        val errorJsonString = error.response()?.errorBody()?.string()
//                        Log.e("Error json", errorJsonString.toString())
////                        val arr  = JsonParser().parse(errorJsonString)
////                                .asJsonObject["errors"]
////                                .asJsonArray
////                        if (arr.size() > 0) {
////                            message = arr.get(0).toString()
//                            onCheckOutError(message)
////                        }
//                    } else {
//                        error.message?.let {
//                            onCheckOutError(it)
//                        }
//                    }
//                })
    }



    private fun onCheckOutError(it: String) {
        errorCheckout.value = it
        Log.e(TAG, "onCheckOutError: ==========> $it")
    }

    private fun onCheckoutSuccess(t: CheckinsResponseModel) {
        checkoutSuccess.value = t
        //update the user object
        Log.i(TAG, "onCheckoutSuccess: ==========> $t")
    }

    private fun onGetDataFinish() {
        Log.i(TAG, "onGetDataFinish")
    }

    private fun onGetDataStart() {
        Log.i(TAG, "onGetDataStart")
    }


    private fun onTokenRefreshError(it: String) {
        Log.e("$TAG Token Refresh: ", it)
        //internet timeout? logout user?
    }

    private fun onTokenRefreshed(it: UserResponseModel) {
        Log.e("$TAG TokenRefreshed: ", it.toString())
        //eventsFromServer.value = it

    }

    fun getEventsData(bearerToken: String) {
        getEventsFromServer(bearerToken)
    }

    fun postCheckOut(token: String?, checkOutId: String){
        checkOut(token,checkOutId)
    }

    fun getEventsFromDb(){
        getDbEvents()
    }



}