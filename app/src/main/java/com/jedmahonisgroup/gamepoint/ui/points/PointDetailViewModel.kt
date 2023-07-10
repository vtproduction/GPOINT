package com.jedmahonisgroup.gamepoint.ui.points

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.database.EventsDao
import com.jedmahonisgroup.gamepoint.database.UserDao
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import com.jedmahonisgroup.gamepoint.ui.events.EventViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class PointDetailViewModel(private val userDao: UserDao, private val context: Context) : BaseViewModel() {

    private var subscription: Disposable? = null
    private var TAG: String = PointDetailViewModel::class.java.simpleName
    val getUserPointSuccess: MutableLiveData<String> = MutableLiveData()
    val getUserPointFailed: MutableLiveData<String> = MutableLiveData()

    init {
        getPoints()
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }

    @Inject
    lateinit var gamePointApi: GamePointApi


    private fun getPoints(){
        try{
            val user = GamePointApplication.shared!!.getCurrentUser(context)
            if (user != null){
                getUserPointSuccess.value = user.redeemable
            }else{
                getUserPointFailed.value = "No user found! Please log out and log in again!"
            }
        }catch(t: Throwable){
            getUserPointFailed.value = t.localizedMessage
        }
    }

    /*private fun getPointFromDb(){
        subscription = Observable.fromCallable {userDao.getUserObjectFromDb.user}
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onGetEventsFromDb(it)
            },
                {
                    onGetEventsFromDbError(it)
                }
            )
    }*/
}