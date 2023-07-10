package com.jedmahonisgroup.gamepoint.utils

import android.content.Context
import android.content.SharedPreferences
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject

class SharedPreferencesGamePointSharedPrefsRepo(preferences: SharedPreferences) : gamePointSharedPrefsRepo {
    private val prefSubject = BehaviorSubject.createDefault(preferences)

    private val prefChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, _ ->
        prefSubject.onNext(sharedPreferences)
    }

    companion object {

        @JvmStatic
        fun create(context: Context): SharedPreferencesGamePointSharedPrefsRepo {
            val preferences = context.getSharedPreferences("com.jedmahonisgroup.gamepoint", Context.MODE_PRIVATE)
            return SharedPreferencesGamePointSharedPrefsRepo(preferences)
        }

     //   private const val KEY_NAME = "key_name"

    }

    init {
        preferences.registerOnSharedPreferenceChangeListener(prefChangeListener)
    }
    /**
    * save points
    **/
    override fun savePoints(key: String, points: String): Completable = prefSubject
            .firstOrError()
            .editSharedPreferences {
                putString(key, points)
            }

    override fun points(key: String): Observable<String> = prefSubject
            .map { it.getString(key, "") }

    /**
     * save points this check in
     **/
    override fun savePointsThisCheckIn(key: String, points: String): Completable = prefSubject
            .firstOrError()
            .editSharedPreferences {
                putString(key, points)
            }

    override fun pointsThisCheckIn(key: String): Observable<String> = prefSubject
            .map { it.getString(key, "") }
    /**
    * save percent
    **/
    override fun savePercent(key: String, percent: String): Completable = prefSubject
            .firstOrError()
            .editSharedPreferences {
                putString(key, percent)
            }

    override fun percent(key: String): Observable<String> = prefSubject
            .map { it.getString(key, "") }

   /**
    * save previous minuets
    **/
    override fun savePreviousMinuets(key: String, percent: String): Completable = prefSubject
            .firstOrError()
            .editSharedPreferences {
                putString(key, percent)
            }

    override fun previousMinuets(key: String): Observable<String> = prefSubject
            .map { it.getString(key, "") }

   /**
    * save total points
    **/
    override fun saveTotalPoints(key: String, percent: String): Completable = prefSubject
            .firstOrError()
            .editSharedPreferences {
                putString(key, percent)
            }

    override fun totalPoints(key: String): Observable<String> = prefSubject
            .map { it.getString(key, "") }

    /**
    * save elapsedTime
    **/
    override fun saveElapsedTime(key: String, time: String): Completable = prefSubject
            .firstOrError()
            .editSharedPreferences {
                putString(key, time)
            }

    override fun elapsedTime(key: String): Observable<String> = prefSubject
            .map { it.getString(key, "") }

    /**
     * save RedeemTimestamp
     **/
    override fun saveRedeemTimestamp(key: String, percent: String): Completable = prefSubject
            .firstOrError()
            .editSharedPreferences {
                putString(key, percent)
            }


    override fun redeemTimestamp(key: String): Observable<String> = prefSubject
            .map{it.getString(key, "")
    }

    /**
     * save Deal Expire Timestamp
     **/
    override fun saveDealExpireTimestamp(key: String, percent: String): Completable = prefSubject
            .firstOrError()
            .editSharedPreferences {
                putString(key, percent)
            }


    override fun dealExpireTimeStamp(key: String): Observable<String> = prefSubject
            .map{it.getString(key, "")
    }

   /**
     * save Deal Expire Timestamp
     **/
    override fun saveCountDown(key: String, countDownTime: String): Completable = prefSubject
            .firstOrError()
            .editSharedPreferences {
                putString(key, countDownTime)
            }


    override fun countDown(key: String): Observable<String> = prefSubject
            .map{it.getString(key, "")
    }

   /**
     * save picks Timestamp
     **/
    override fun savePicksTimestamp(key: String, timestamp: String): Completable = prefSubject
            .firstOrError()
            .editSharedPreferences {
                putString(key, timestamp)
            }


    override fun getPicksTimestamp(key: String): Observable<String> = prefSubject
            .map{it.getString(key, "")
    }

    /**
     * save Deal Expire Timestamp
     **/
    override fun saveIsTimerActive(key: String, timerActive: Boolean): Completable = prefSubject
            .firstOrError()
            .editSharedPreferences {
                putBoolean(key, timerActive)
            }


    override fun isTimerActive(key: String): Observable<Boolean> = prefSubject
            .map{it.getBoolean(key, false)

    }


    /**
     * Clear from sp
     * */
    override fun clear(key: String): Completable {
        return prefSubject.firstOrError()
                .clearSharedPreferences {
                    remove(key)
                }
    }

    fun Single<SharedPreferences>.editSharedPreferences(batch: SharedPreferences.Editor.() -> Unit): Completable =
            flatMapCompletable {
                Completable.fromAction {
                    it.edit().also(batch).apply()
                }
            }

    fun Single<SharedPreferences>.clearSharedPreferences(batch: SharedPreferences.Editor.() -> Unit): Completable =
            flatMapCompletable {
                Completable.fromAction {
                    it.edit().also(batch).apply()
                }
            }

}