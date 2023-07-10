package com.jedmahonisgroup.gamepoint.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.utils.Constants
import com.jedmahonisgroup.gamepoint.utils.Constants.PREFS_FILENAME
import android.graphics.drawable.ColorDrawable
import android.util.Log
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import kotlinx.android.synthetic.main.fragment_feeds.*


lateinit var sharedPreferences: SharedPreferences
var color: Int? = null

abstract class BaseActivity : AppCompatActivity() {
    fun getRepository() = (application as GamePointApplication).getRepository()

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = applicationContext.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        color = Color.parseColor(getString(R.color.colorPrimary))
        /*Color.parseColor(
            sharedPreferences.getString(
                Constants.PRIMARY_COLOR,
                R.color.colorPrimary.toString()
            )
        ).also { color = it }

        try {
            appBarLayout.background = ColorDrawable(color!!)
        } catch (e: Exception) {
            Log.e("JMG", "exception doing app bar layout color: " + e.localizedMessage)
        }*/

    }

    @SuppressLint("ResourceType")
    override fun onResume() {
        super.onResume()
        Log.e("JMG", "base onResume: ")
//        Log.e("JMG", "supportActionBar: " + supportActionBar)
        sharedPreferences = applicationContext.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        color = Color.parseColor(getString(R.color.colorPrimary))
        /*Color.parseColor(
            sharedPreferences.getString(
                Constants.PRIMARY_COLOR,
                R.color.colorPrimary.toString()
            )
        ).also { color = it }*/
        val window: Window = this.getWindow()
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.setStatusBarColor(color!!)
        window.navigationBarColor = color!!
        actionBar?.setBackgroundDrawable(ColorDrawable(color!!))
        supportActionBar?.setBackgroundDrawable(ColorDrawable(color!!))
    }

    fun setToolbarColors(tbar: Toolbar) {
        try {
            tbar.background = color?.let { ColorDrawable(it) }
//            Log.e("JMG" , "color should have set successfully on tbar: " + tbar)
        } catch (e: Exception) {
            Log.e("JMG", "exception set toolbar color: " + e.localizedMessage)
        }
    }
    fun getSchoolColor(): Int? {
        return color?.let { it }
    }

    fun getSchoolColorAsString() : String? {
        return sharedPreferences.getString(Constants.PRIMARY_COLOR, R.color.colorPrimary.toString())
    }
}