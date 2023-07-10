package com.jedmahonisgroup.gamepoint.utils

import io.reactivex.Completable
import io.reactivex.Observable

interface gamePointSharedPrefsRepo {

    //save points
    fun savePoints(key: String, points: String): Completable

    fun points(key: String): Observable<String>

    //save points
    fun savePointsThisCheckIn(key: String, points: String): Completable

    fun pointsThisCheckIn(key: String): Observable<String>

    //save percent
    fun savePercent(key: String, percent: String): Completable

    fun percent(key: String): Observable<String>

    //save previousMinutesCheckedIn
    fun savePreviousMinuets(key: String, percent: String): Completable

    fun previousMinuets(key: String): Observable<String>

    //save previousMinutesCheckedIn
    fun saveTotalPoints(key: String, percent: String): Completable

    fun totalPoints(key: String): Observable<String>

    //save previousMinutesCheckedIn
    fun saveElapsedTime(key: String, time: String): Completable

    fun elapsedTime(key: String): Observable<String>


    //save redeemTimeStamp
    fun saveRedeemTimestamp(key: String, percent: String): Completable

    fun redeemTimestamp(key: String): Observable<String>

    //save redeemTimeStamp
    fun saveDealExpireTimestamp(key: String, percent: String): Completable

    fun dealExpireTimeStamp(key: String): Observable<String>

    //save redeemTimeStamp
    fun saveIsTimerActive(key: String, timerActive: Boolean): Completable

    fun isTimerActive(key: String): Observable<Boolean>

    //save redeemTimeStamp
    fun savePicksTimestamp(key: String, timestamp: String): Completable

    fun getPicksTimestamp(key: String): Observable<String>

    //this is the count down time once the user is redeeming a deal
    fun saveCountDown(key: String, countDownTime: String): Completable

    fun countDown(key: String): Observable<String>


    fun clear(key: String): Completable
}