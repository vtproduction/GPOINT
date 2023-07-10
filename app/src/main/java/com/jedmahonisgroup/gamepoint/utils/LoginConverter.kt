package com.jedmahonisgroup.gamepoint.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jedmahonisgroup.gamepoint.model.Login

class LoginConverter {
    private val gson = Gson()
    private val type = object : TypeToken<Login>() {

    }.type

    @TypeConverter
    fun longinFromToJson(json: String): Login {
        return gson.fromJson(json,type)
    }

    fun loginJsonToString(nestedData: Login): String {
        return gson.toJson(nestedData, type)
    }
}
