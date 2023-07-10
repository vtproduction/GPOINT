package com.jedmahonisgroup.gamepoint

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModelProviders
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.injection.component.DaggerViewModelInjector
import com.jedmahonisgroup.gamepoint.injection.component.ViewModelInjector
import com.jedmahonisgroup.gamepoint.injection.module.AppModule
import com.jedmahonisgroup.gamepoint.injection.module.NetworkModule
import com.jedmahonisgroup.gamepoint.injection.module.SharedPreferencesModule
import com.jedmahonisgroup.gamepoint.model.PointsPrize
import com.jedmahonisgroup.gamepoint.model.StreakPrize
import com.jedmahonisgroup.gamepoint.model.User
import com.jedmahonisgroup.gamepoint.model.UserResponse
import com.jedmahonisgroup.gamepoint.ui.MainViewModel
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.Exception


class GamePointApplication : Application() {
    private lateinit var repository: EventRepository

    private var viewModelInjector: ViewModelInjector? = null
    private var user: UserResponse? = null
    var shouldRefreshToken = true

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
        Log.e("JMG", "GamPointApplication: " + "onCreate")
        shouldRefreshToken = true
        repository = EventRepository(this)
        shared = this
        val builder = Picasso.Builder(this)
        builder.downloader(OkHttp3Downloader(this, Integer.MAX_VALUE.toLong()))
        val built = builder.build()
        built.setIndicatorsEnabled(false)
        built.isLoggingEnabled = true
        Picasso.setSingletonInstance(built)



        viewModelInjector = DaggerViewModelInjector.builder()
            .networkModule(NetworkModule)
            .appModule(AppModule(this))
            .sharedPrefModule(SharedPreferencesModule())
            .build()




    }



    companion object{
        val CHANNEL_ID = "test_channel"
        var shared: GamePointApplication? = null
        fun createNotificationChannel(context: Context){
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Test Channel"
                val descriptionText = "Channel for testing"
                val importance = NotificationManager.IMPORTANCE_DEFAULT

                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                // Register the channel with the system
                val notificationManager: NotificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    fun setCurrentUser(currentUser: UserResponse, ctx: Context) {
        val fileName = "currentUser"
        try {
            val fos = ctx.openFileOutput(fileName, MODE_PRIVATE)
            val os = ObjectOutputStream(fos)
            os.writeObject(currentUser)
            os.close()
            fos.close()
        } catch (e: Exception) {
            Log.e("exception", e.localizedMessage)
            e.printStackTrace()
        }
        user = currentUser
    }

    fun getCurrentUser(ctx: Context): UserResponse? {
        val fileName = "currentUser"
        try {
            val fis = ctx.openFileInput(fileName)
            val inputStream = ObjectInputStream(fis)
            val currentUser: UserResponse = inputStream.readObject() as UserResponse
            inputStream.close()
            fis.close()

            currentUser.points_prize =null
            currentUser.streaks_prize = null
            /*currentUser.streaks_prize = StreakPrize(id = 1, title = "The prize", prize_url = "https://www.jmg.rocks", description = "This is the prize for you",
                image = "https://picsum.photos/200/300", url = "https://sandbox.gamepoint.fans/prizes/1.json")*/

            user = currentUser
            return currentUser
        } catch (e: Exception) {
            Log.e("exception", e.localizedMessage)
            e.printStackTrace()
        }
        return null
    }



    fun getRepository() = repository
    fun getViewModelInjector(): ViewModelInjector {
        return viewModelInjector!!
    }}