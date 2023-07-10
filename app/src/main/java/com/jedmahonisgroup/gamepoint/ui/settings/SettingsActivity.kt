package com.jedmahonisgroup.gamepoint.ui.settings

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.ui.BaseActivity
import com.jedmahonisgroup.gamepoint.ui.auth.AuthActivity
import com.jedmahonisgroup.gamepoint.utils.Constants
import com.jedmahonisgroup.gamepoint.utils.Constants.IsLogedIn
import kotlinx.android.synthetic.main.activity_user_profile.*


class SettingsActivity : BaseActivity() {
    private var settingsViewModel: SettingsViewModel? = null
    private var TAG: String? = SettingsActivity::class.java.simpleName

    private var mLogoutButton: Button? = null
    private var mPrivacy: Button? = null
    private var mTerms: Button? = null
    private var mContestButton: Button? = null
    private var mEditProfile: Button? = null
    private var mfaqButton: Button? = null
    private var mPrivacySetting: Button? = null


    private val PREFS_FILENAME = "com.jedmahonisgroup.gamepoint"
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        settingsViewModel = ViewModelProviders.of(this, ViewModelFactory(this)).get(SettingsViewModel::class.java)



        mLogoutButton = findViewById(R.id.logoutButton)
        mPrivacy = findViewById(R.id.privacyPolicy)
        mTerms = findViewById(R.id.termsAndConditions)
        mContestButton = findViewById(R.id.contestTerms)
        mfaqButton = findViewById(R.id.faq)
        mEditProfile = findViewById(R.id.editProfile)
        mPrivacySetting = findViewById(R.id.privacySetting)


        versionBuildNumber.text = getVersionBuildNumber()

        sharedPreferences = applicationContext.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)



    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish();
    }

    override fun onResume() {
        super.onResume()
        setOnClickListeners()
        setToolbarColors(settings_toolbar)
        getSchoolColor()?.let { mLogoutButton!!.setTextColor(it) }

    }

    private fun logout(token: String) {
        mLogoutButton!!.setOnClickListener {
                //change the sp file and then call the logout api call
            unregisterPushToken()
        }
    }

    private fun setOnClickListeners () {
        val intent = intent
        val token = intent.getStringExtra("token")!!
        logout(token)
        privacy()
        terms()
        contest()
        faq()
        editProfile()
        privacysetting()


    }

    private fun privacy() {
        mPrivacy!!.setOnClickListener {
            startWebViewWithInt(0)
        }
    }

    private fun terms() {
        mTerms!!.setOnClickListener {
            startWebViewWithInt(1)
        }
    }

    private fun contest() {
        mContestButton!!.setOnClickListener {
            startWebViewWithInt(2)
        }
    }

    private fun faq() {
        mfaqButton!!.setOnClickListener {
            startWebViewWithInt(4)
        }
    }

    private fun editProfile(){
        mEditProfile?.setOnClickListener {
            val intent = Intent(applicationContext, EditProfile::class.java)
            startActivity(intent)

        }
    }

    private fun privacysetting(){
        mPrivacySetting?.setOnClickListener {
            val intent = Intent(applicationContext, PrivacySetting::class.java)
            startActivity(intent)

        }
    }



    private fun startWebViewWithInt(urlType:Int) {
        val intent = Intent(applicationContext, WebViewActivity::class.java)
        intent.putExtra("urlType", urlType)
        startActivity(intent)
    }

    private fun logoutResult() {
        settingsViewModel!!.logoutSuccess.observe(this, Observer {
            closeAllActivities()
        })

        settingsViewModel!!.logoutFail.observe(this, Observer {
            Log.e(TAG, "something went wrong while trying to log out $it we will log user out anyways")
            closeAllActivities()
        })


    }

    fun unregisterPushToken() {
        val token = intent.getStringExtra("token")
        settingsViewModel!!.unregisterPushTokenSuccess.observe(this, Observer {
            sharedPreferences.edit().putString("pushTokenString", "").apply()
            sharedPreferences.edit().putBoolean("pushTokenSent", false).apply()
            val editor = sharedPreferences.edit()
            editor.putBoolean(IsLogedIn, false)
            editor.apply()

            //make the api call
            token?.let { it1 -> settingsViewModel!!.logout(it1) }
            logoutResult()
        })
        settingsViewModel!!.unregisterPushTokenFail.observe(this, Observer {
            Log.e(TAG, "something went wrong while trying to unregister push token $it we will log user out anyways")
            val editor = sharedPreferences.edit()
            editor.putBoolean(IsLogedIn, false)
            editor.apply()

            //make the api call
            token?.let { it1 -> settingsViewModel!!.logout(it1) }
            logoutResult()
        })

        Log.e("JMG", "sharedPreferences.getString(\"pushTokenString\", \"\")!!: " + sharedPreferences.getString("pushTokenString", "")!!)
        token?.let { settingsViewModel!!.unregisterPushToken(it, sharedPreferences.getString("pushTokenString", "")!!) }
    }

    private fun closeAllActivities() {
        val intent = Intent(applicationContext, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        intent.putExtra("EXIT", true)
        startActivity(intent)
    }


    private fun getVersionBuildNumber(): String {
        val manager = this.packageManager
        val info = manager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            "Version ${info.longVersionCode} Build ${info.versionName}"
        } else {
            "Version ${info.versionCode} Build ${info.versionName}"
        }
    }
}
