package com.jedmahonisgroup.gamepoint.ui.auth.settings

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.ui.BaseActivity
import com.jedmahonisgroup.gamepoint.ui.WebViewActivity
import com.jedmahonisgroup.gamepoint.ui.auth.AuthActivity
import kotlinx.android.synthetic.main.activity_user_profile.*


class UserProfile : BaseActivity() {
    private var userProfileViewModel: UserProfileViewModel? = null
    private var mLogoutButton: Button? = null
    private var mPrivacy: Button? = null
    private var mTerms: Button? = null
    private var mContestButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        userProfileViewModel = ViewModelProviders.of(this, ViewModelFactory(this)).get(UserProfileViewModel::class.java)


        mLogoutButton = findViewById(R.id.logoutButton)
        mPrivacy = findViewById(R.id.privacyPolicy)
        mTerms = findViewById(R.id.termsAndConditions)
        mContestButton = findViewById(R.id.contestTerms)

        versionBuildNumber.text = getVersionBuildNumber()

    }

    override fun onResume() {
        super.onResume()
        setOnClickListeners()
        getSchoolColor()?.let { mLogoutButton!!.setTextColor(it) }
    }

    private fun logout(token: String) {
        mLogoutButton!!.setOnClickListener {
            userProfileViewModel!!.logout(token)
            logoutResult()
        }
    }

    private fun setOnClickListeners () {
        val intent = intent
        val token = intent.getStringExtra("token")!!
        logout(token)
        privacy()
        terms()
        contest()

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


    private fun startWebViewWithInt(urlType:Int) {
        val intent = Intent(applicationContext, WebViewActivity::class.java)
        intent.putExtra("urlType", urlType)
        startActivity(intent)
    }

    private fun logoutResult() {
        userProfileViewModel!!.logoutSuccess.observe(this, Observer {
            closeAllActivities()
        })
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
