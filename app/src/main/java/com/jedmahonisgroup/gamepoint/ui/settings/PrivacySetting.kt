package com.jedmahonisgroup.gamepoint.ui.settings

import android.app.Activity
import android.app.DatePickerDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.database.model.UserDatabaseModel
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.*
import com.jedmahonisgroup.gamepoint.ui.BaseActivity
import com.jedmahonisgroup.gamepoint.ui.MainActivity
import com.jedmahonisgroup.gamepoint.ui.blockeduser.blockuserfragment
import com.jedmahonisgroup.gamepoint.utils.setLocalImage
import kotlinx.android.synthetic.main.above_18_alert.view.*
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_privacy_setting.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


class PrivacySetting : BaseActivity() {

    private var Token: String? = null
    private var mUserId: String? = null
    private var mUser: UserResponseModel? = null

    private var TAG: String = EditProfile::class.java.simpleName

    private var mUserModel: UserResponseModel? = null


    private var mPrivateSettingViewModel: PrivacySettingViewModel? = null

    private var mPublicRadioButton: RadioButton? = null
    private var mPrivateRadioButton: RadioButton? = null

    private var mBlockedButton: Button? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_setting)
        setToolbarColors(privacy_settings_toolbar)


        mPrivateSettingViewModel = ViewModelProviders.of(this, ViewModelFactory(this)).get(PrivacySettingViewModel::class.java)
        mPrivateSettingViewModel?.userFromDb()

        resultsFromServer()
        setupUi()

    }





    private fun setupUi() {

        mPublicRadioButton = findViewById(R.id.radioButtonPublic)
        mPrivateRadioButton = findViewById(R.id.radioButtonPrivate)
        mBlockedButton = findViewById(R.id.blockeduser)
        view_profile_back?.setOnClickListener {
            onBackPressed()
        }

        mPublicRadioButton?.setOnClickListener {

            mPrivateSettingViewModel?.updateUser(Token!! ,mUserId!!, UpdateProfilePP(user = UserUpdatePP(private_posts = false))  )
            mPrivateRadioButton?.isChecked = false


        }

        mPrivateRadioButton?.setOnClickListener {

            mPrivateSettingViewModel?.updateUser(Token!! ,mUserId!!, UpdateProfilePP(user = UserUpdatePP(private_posts = true))  )

            mPublicRadioButton?.isChecked = false


        }

        mBlockedButton?.setOnClickListener {

            val user = mUser

            val gSon = Gson()
            val bundle = Bundle()
            bundle.putString("token", Token.toString())


            // set Fragmentclass Arguments
            val fragobj = blockuserfragment()
            fragobj.arguments = bundle

            supportFragmentManager.beginTransaction()
                    .replace(R.id.checkedinUi, fragobj, "1").addToBackStack("1").commitAllowingStateLoss()
        }

    }


    private fun resultsFromServer() {
        //get user from database
        mPrivateSettingViewModel?.userFromDbSuccess?.observe(this, Observer {

            if (it != null) {

                getuserfromserver(it)
                mUserModel = it





            }

        })

        //could not load user from db, we need to get server data or try logout first
        mPrivateSettingViewModel?.userFromDbError?.observe(this, Observer {


            Log.e(TAG, "could not get user form db")
        })


        mPrivateSettingViewModel?.userFromServerSuccess?.observe(this, Observer {

            if (it.private_posts){

                mPublicRadioButton?.isChecked = false
                mPrivateRadioButton?.isChecked = true

            }else{

                mPublicRadioButton?.isChecked = true
                mPrivateRadioButton?.isChecked = false


            }

        })

        /**
         * Update profile response
         */


        mPrivateSettingViewModel?.successUpdateingProfile?.observe(this, Observer {



        })

        mPrivateSettingViewModel?.errorUpdateingProfile?.observe(this, Observer {

        })


    }


    private fun getuserfromserver(it: UserResponseModel) {
        Token = it.user.login.token
        mUserId = it.user.id.toString()
        mUser = it


        mPrivateSettingViewModel?.userFromServer(Token!!,mUserId!!)



    }










}
