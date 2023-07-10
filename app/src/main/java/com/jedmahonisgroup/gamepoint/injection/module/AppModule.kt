package com.jedmahonisgroup.gamepoint.injection.module

import android.content.Context
import androidx.annotation.NonNull
import com.google.gson.Gson
import dagger.Module
import dagger.Provides

import javax.inject.Singleton




/**
 * Created by nienle on 11,January,2023
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
@Module
class AppModule(@NonNull context: Context) {
    private val context: Context

    init {
        this.context = context
    }

    @Singleton
    @Provides
    @NonNull
    fun provideContext(): Context {
        return context
    }

    @Singleton
    @Provides
    fun provideGson() : Gson = Gson()
}