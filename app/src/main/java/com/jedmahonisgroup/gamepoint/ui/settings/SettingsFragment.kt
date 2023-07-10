package com.jedmahonisgroup.gamepoint.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.ui.BaseFragment
import com.jedmahonisgroup.gamepoint.ui.auth.AuthActivity
import com.jedmahonisgroup.gamepoint.utils.Constants
import com.jedmahonisgroup.gamepoint.utils.LogUtil
import kotlinx.android.synthetic.main.activity_user_profile.*


class SettingsFragment : BaseFragment() {
    private var settingsViewModel: SettingsViewModel? = null
    private var TAG: String? = SettingsActivity::class.java.simpleName

    private var mLogoutButton: Button? = null
    private var mPrivacy: Button? = null
    private var mTerms: Button? = null
    private var mContestButton: Button? = null
    private var mEditProfile: Button? = null
    private var mfaqButton: Button? = null
    private var mBuildNumber: TextView? = null
    private var mBackButton: ImageButton? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences(Constants.PREFS_FILENAME, Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainView = inflater.inflate(R.layout.fragment_settings, container, false)
        settingsViewModel = ViewModelProviders
            .of(this, ViewModelFactory(requireActivity() as AppCompatActivity))
            .get(SettingsViewModel::class.java)


        mLogoutButton = mainView.findViewById(R.id.btnAction6)
        mPrivacy = mainView.findViewById(R.id.btnAction2)
        mTerms = mainView.findViewById(R.id.btnAction3)
        mContestButton = mainView.findViewById(R.id.btnAction4)
        mfaqButton = mainView.findViewById(R.id.btnAction5)
        mEditProfile = mainView.findViewById(R.id.btnAction1)
        mBuildNumber = mainView.findViewById(R.id.versionBuildNumber)
        mBackButton = mainView.findViewById(R.id.back_arrow)
        mBuildNumber?.text = getVersionBuildNumber()
        return mainView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
    }

    private fun setOnClickListeners () {
        logout()
        privacy()
        terms()
        contest()
        faq()
        editProfile()
        mBackButton?.setOnClickListener {
            requireActivity().onBackPressed()
        }
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
            val intent = Intent(requireContext(), EditProfile::class.java)
            startActivity(intent)

        }
    }

    private fun startWebViewWithInt(urlType:Int) {
        val intent = Intent(requireContext(), WebViewActivity::class.java)
        intent.putExtra("urlType", urlType)
        startActivity(intent)
    }


    private fun getVersionBuildNumber(): String {
        val manager = requireActivity().packageManager
        val info = manager.getPackageInfo(requireActivity().packageName, PackageManager.GET_ACTIVITIES)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            "Version ${info.longVersionCode} Build ${info.versionName}"
        } else {
            "Version ${info.versionCode} Build ${info.versionName}"
        }
    }


    private fun logout() {
        mLogoutButton!!.setOnClickListener {
            val keys: Map<String, *> = sharedPreferences.all

            keys.entries.forEach {
                Log.d("NIENTAG", "key: [${it.key}] - val: [${it.value.toString()}]")
            }

            //change the sp file and then call the logout api call
            unregisterPushToken()
        }
    }

    private fun unregisterPushToken() {
        val token = sharedPreferences.getString("pushTokenString","")  //requireActivity().intent.getStringExtra("token")
        settingsViewModel!!.unregisterPushTokenSuccess.observe(viewLifecycleOwner, Observer {
            sharedPreferences.edit().putString("pushTokenString", "").apply()
            sharedPreferences.edit().putBoolean("pushTokenSent", false).apply()
            val editor = sharedPreferences.edit()
            editor.putBoolean(Constants.IsLogedIn, false)
            editor.apply()

            //make the api call
            token?.let { it1 -> settingsViewModel!!.logout(it1) }
            logoutResult()
        })
        settingsViewModel!!.unregisterPushTokenFail.observe(viewLifecycleOwner, Observer {
            Log.e(TAG, "something went wrong while trying to unregister push token $it we will log user out anyways")
            val editor = sharedPreferences.edit()
            editor.putBoolean(Constants.IsLogedIn, false)
            editor.apply()

            //make the api call
            token?.let { it1 -> settingsViewModel!!.logout(it1) }
            logoutResult()
        })

        Log.e("JMG", "sharedPreferences.getString(\"pushTokenString\", \"\")!!: " + sharedPreferences.getString("pushTokenString", "")!!)
        token?.let { settingsViewModel!!.unregisterPushToken(it, sharedPreferences.getString("pushTokenString", "")!!) }
    }

    private fun logoutResult() {
        settingsViewModel!!.logoutSuccess.observe(viewLifecycleOwner, Observer {
            closeAllActivities()
        })

        settingsViewModel!!.logoutFail.observe(viewLifecycleOwner, Observer {
            Log.e(TAG, "something went wrong while trying to log out $it we will log user out anyways")
            closeAllActivities()
        })


    }

    private fun closeAllActivities() {
        val intent = Intent(requireContext(), AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        intent.putExtra("EXIT", true)
        startActivity(intent)
    }


    companion object {

        @JvmStatic
        fun newInstance() =
            SettingsFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}