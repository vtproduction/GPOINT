package com.jedmahonisgroup.gamepoint.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable
import java.util.*


class UserResponseConverter : Serializable {
    companion object {

        @TypeConverter
        fun fromTimestamp(value: String): ArrayList<String>? {
            val listType = object : TypeToken<ArrayList<String>>() {

            }.type
            return Gson().fromJson<ArrayList<String>>(value, listType)
            // return value == null ? null : new Date(value);
        }

        @TypeConverter
        fun arraylistToString(list: ArrayList<String>): String {
            val gson = Gson()

            return gson.toJson(list)
            // return mDate == null ? null : mDate.getTime();
        }
    }

}