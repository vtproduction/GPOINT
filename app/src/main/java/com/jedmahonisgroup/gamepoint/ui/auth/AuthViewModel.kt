package com.jedmahonisgroup.gamepoint.ui.auth

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.database.UserDao
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class AuthViewModel(private var userDao: UserDao) : BaseViewModel() {

    private val TAG: String = AuthViewModel::class.java.simpleName
    @Inject
    lateinit var gamePointApi: GamePointApi

    val successMessage: MutableLiveData<String> = MutableLiveData()
    val errorMessage: MutableLiveData<String> = MutableLiveData()


    private var subscription: Disposable? = null

    init {

    }

    @SuppressLint("CheckResult")
    private fun getUserDB() {
        subscription = Observable.fromCallable { userDao.getUserObjectFromDb.user }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onGetUserFromDbSuccess(it) },
                        { onGetUserToDbError(it) }
                )
    }

    private fun onGetUserToDbError(it: Throwable?) {
        Log.e(TAG, "onGetUserToDbError -------->  $it")
        errorMessage.value = it.toString()
    }

    private fun onGetUserFromDbSuccess(it: String?) {
        Log.e(TAG, "onGetUserFromDbSuccess -------->  $it")
        successMessage.value = it
    }

    fun checkIfUserExists() {
        getUserDB()
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }

}