package com.jedmahonisgroup.gamepoint.injection.module

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.helpers.SharedPreferencesHelper
import dagger.Module

import dagger.Provides
import dagger.Reusable
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Created by nienle on 11,January,2023
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
@Module
class SharedPreferencesModule {

    @Provides
    @Reusable
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("com.jedmahonisgroup.gamepoint", Context.MODE_PRIVATE)
    }


    @Provides
    @Reusable
    fun provideSharedPreferencesHelper(pref: SharedPreferences, gson: Gson): SharedPreferencesHelper {
        return SharedPreferencesHelper(pref, gson)
    }

}