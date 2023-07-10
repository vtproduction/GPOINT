package com.jedmahonisgroup.gamepoint.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.ui.auth.AuthActivity
import com.jedmahonisgroup.gamepoint.ui.auth.AuthViewModel
import com.jedmahonisgroup.gamepoint.utils.Constants.IsLogedIn


class SplashScreenActivity : AppCompatActivity() {

    private var TAG: String = SplashScreenActivity::class.java.simpleName

    private lateinit var authViewModel: AuthViewModel

    private val PREFS_FILENAME = "com.jedmahonisgroup.gamepoint"
    private lateinit var sharedPreferences: SharedPreferences
    private var imgLoading: ImageView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        // Make sure this is before calling super.onCreate
        //setTheme(R.style.AppTheme)=
        super.onCreate(savedInstanceState)
        initCrashAnalytics()
        setContentView(R.layout.activity_splashscreen)
        imgLoading = findViewById(R.id.imgLoading)
        authViewModel = ViewModelProviders.of(this, ViewModelFactory(this)).get(AuthViewModel::class.java)
        sharedPreferences = applicationContext.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

        doesUserExist()
    }



    private fun initCrashAnalytics(){
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
    }
    /**
     * we are checking no longer for api data in the db,
     * instead we should check for a boolean in shared prefs
    *  @param isLogedIn
    * */
    private fun doesUserExist() {
        animateImage()
        Handler(Looper.getMainLooper()).postDelayed({
            val islogedIn = sharedPreferences.getBoolean(IsLogedIn, false)
            if (islogedIn){
                //its true so the user is loogged in, take them to the main activity
                startMainActivity()
            }else{
                //user is not loged in, show them the auth activity
                startAuthActivity()
            }
        },500)




//        authViewModel.checkIfUserExists()
//
//        authViewModel.successMessage.observe(this, Observer {
//            if (it != null && it.isNotEmpty()) {
//                //the user exists, launch main activity
//                startMainActivity()
//                finish()
//            } else {
//                startAuthActivity()
//                finish()
//            }
//        })
//
//        authViewModel.errorMessage.observe(this, Observer {
//            Log.e(TAG, "==============> Error reading user from db")
//            startAuthActivity()
//            finish()
//        })

    }

    private fun animateImage(){
        try{

            val set = AnimationUtils.loadAnimation(this, R.anim.loading_anim)


            imgLoading?.startAnimation(set)
        }catch(t: Throwable){
            Log.e("SplashScreenActivity"," > animateImage > 94: ${t.localizedMessage}")
        }
    }

    private fun startMainActivity() {
        Log.i(TAG, "User exists, starting main activity")
        val myIntent = Intent(this@SplashScreenActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        //myIntent.putExtra("key", value); //Optional parameters
        this@SplashScreenActivity.startActivity(myIntent)
        finish()
    }

    private fun startAuthActivity() {
        Log.i(TAG, "User exists, starting main activity")
        val myIntent = Intent(this@SplashScreenActivity, AuthActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        //myIntent.putExtra("key", value); //Optional parameters
        this@SplashScreenActivity.startActivity(myIntent)
        finish()
    }


}
