package com.jedmahonisgroup.gamepoint.helpers

import android.content.SharedPreferences
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.model.gameshow.JoinGameShowResponse
import javax.inject.Inject

/**
 * Created by nienle on 12,January,2023
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */

class SharedPreferencesHelper(private val preferences: SharedPreferences, private val gson: Gson) {

    private val KEY_JOINED_GAME_ID = "KEY_JOINED_GAME_ID"
    private val KEY_JOINED_GAME_DATA = "KEY_JOINED_GAME_DATA"
    private val KEY_CURRENT_TOKEN = "access_token"




    fun getJoinedGame() : Int = preferences.getInt(KEY_JOINED_GAME_ID, -1000)

    fun saveJoinedGame(joinedGameId: Int) {
        preferences.edit().putInt(KEY_JOINED_GAME_ID, joinedGameId).apply()
    }

    fun clearJoinedGame(){
        preferences.edit().remove(KEY_JOINED_GAME_ID).apply()
    }


    fun getAccountToken() = preferences.getString(KEY_CURRENT_TOKEN, "")

    fun getJoinedGameData() : JoinGameShowResponse?{
        val data = preferences.getString(KEY_JOINED_GAME_DATA, "") ?: return null
        if (data.isEmpty()) return null

        return gson.fromJson(data, JoinGameShowResponse::class.java)
    }

    fun saveJoinedGameData(data: JoinGameShowResponse){
        preferences.edit().putString(KEY_JOINED_GAME_DATA, gson.toJson(data)).apply()
    }

    fun clearJoinedGameData(){
        preferences.edit().remove(KEY_JOINED_GAME_DATA).apply()
    }
}