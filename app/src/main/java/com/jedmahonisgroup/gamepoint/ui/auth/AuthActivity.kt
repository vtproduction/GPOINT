package com.jedmahonisgroup.gamepoint.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.UserResponse
import com.jedmahonisgroup.gamepoint.model.UserResponseModel
import com.jedmahonisgroup.gamepoint.ui.UploadPictureActivity
import com.jedmahonisgroup.gamepoint.ui.auth.login.LoginFragment
import com.jedmahonisgroup.gamepoint.ui.auth.signup.SignUpFragment


class AuthActivity : AppCompatActivity(),
        AuthOptionsFragment.OnFragmentInteractionListener,
        SignUpFragment.OnSignUpListener,
        LoginFragment.OnLogInListener,
        LoginFragment.OnSignUpSwitchListener{

    private var homeFragmentIdentifier: Int? = null

    private var TAG = AuthActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        //status bar
        val window = this.window
        window.statusBarColor = ContextCompat.getColor(this.baseContext, android.R.color.black)

    }
//
//    private fun changeFragments(fragment: Fragment) {
//        val ft = supportFragmentManager.beginTransaction()
//        ft.replace(R.id.placeholder, fragment)
//        ft.commit()
//    }

    override fun onResume() {
        super.onResume()
        changeFragments(AuthOptionsFragment())
    }

    private fun changeFragments(fragment: Fragment) {
        val xfragmentTransaction = supportFragmentManager.beginTransaction()

        // val xfragmentTransaction = fragmentManager.beingTransaction()
        xfragmentTransaction.replace(R.id.placeholder, fragment)
        xfragmentTransaction.addToBackStack(fragment::class.java.simpleName)
        homeFragmentIdentifier = xfragmentTransaction.commit()
        Log.i(TAG, "changeFragments ================>fragment id $homeFragmentIdentifier ")
        Log.i(TAG, "changeFragments ================>fragment points is ${fragment::class.java.simpleName} ")
    }


    override fun onBackPressed() {
        //super.onBackPressed()
        Log.i(TAG, "onBackPressed ================>fragment id $homeFragmentIdentifier ")
        val fm = supportFragmentManager
        fm.popBackStack("AuthOptionsFragment", 0)

    }

    override fun onChangeFragment(fragment: Fragment) {
        Log.i(TAG, "onChangeFragment ==========> changing fragments to $fragment")
        changeFragments(fragment)
    }

    override fun onSignUp(user: UserResponse?) {
        Log.i(TAG, "onSignUp ==========> SignUp clicked")
        launchActivity(user!!)
    }

    override fun onSwitchToLogin() {
        changeFragments(LoginFragment())
    }


    override fun onLogin() {
        Log.i(TAG, "onLogin ==========> Login clicked")
    }

    private fun launchActivity(user: UserResponse?) {
        val gSon = Gson()
        val userString = gSon.toJson(user)

        if (!userString.isNullOrEmpty()) {
            val intent = Intent(this, UploadPictureActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            startActivity(intent)
            finish()
        } else {
            // Something went Wrong
            Toast.makeText(applicationContext, "Something Went Wrong, Please try again.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSwitchToSignUp() {
        changeFragments(SignUpFragment())
    }

}
