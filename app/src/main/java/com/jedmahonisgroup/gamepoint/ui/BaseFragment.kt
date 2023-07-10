package com.jedmahonisgroup.gamepoint.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.utils.Constants


abstract class BaseFragment: androidx.fragment.app.Fragment() {
    lateinit var sharedPreferences: SharedPreferences
    var color: Int? = null
    var secondaryColor: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPreferences = requireContext().getSharedPreferences(Constants.PREFS_FILENAME, Context.MODE_PRIVATE)
        /*Color.parseColor(
            sharedPreferences.getString(
                Constants.PRIMARY_COLOR,
                R.color.colorPrimary.toString()
            )
        ).also { color = it }*/
        /*Color.parseColor(
                sharedPreferences.getString(
                        Constants.SECONDARY_COLOR,
                        R.color.colorYellow.toString()
                )
        ).also { secondaryColor = it }*/
        color = Color.parseColor(getString(R.color.colorPrimary))
        secondaryColor = Color.parseColor(getString(R.color.colorYellow))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    fun setToolbarColors(tbar: Toolbar) {
        try {
            tbar.background = color?.let { ColorDrawable(it) }
            Log.e("JMG" , "color should have set successfully on tbar: " + tbar)
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
    fun getSchoolSecondaryColor(): Int? {
        return secondaryColor?.let { it }
    }

    fun getSchoolSecondaryColorAsString() : String? {
        return sharedPreferences.getString(Constants.SECONDARY_COLOR, R.color.colorYellow.toString())
    }

}