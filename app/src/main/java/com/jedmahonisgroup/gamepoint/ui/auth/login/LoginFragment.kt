package com.jedmahonisgroup.gamepoint.ui.auth.login

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.BuildConfig
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.database.model.UserDatabaseModel
import com.jedmahonisgroup.gamepoint.databinding.FragmentLoginBinding
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.LoginModel
import com.jedmahonisgroup.gamepoint.model.UserResponse
import com.jedmahonisgroup.gamepoint.model.UserResponseModel
import com.jedmahonisgroup.gamepoint.model.school.School
import com.jedmahonisgroup.gamepoint.ui.MainActivity
import com.jedmahonisgroup.gamepoint.ui.auth.ForgotPasswordActivity
import com.jedmahonisgroup.gamepoint.utils.Constants
import kotlinx.android.synthetic.main.above_18_alert.view.*
import kotlinx.android.synthetic.main.error_login.view.*
import kotlinx.android.synthetic.main.event_expired_error.view.*
import kotlinx.android.synthetic.main.event_expired_error.view.close
import kotlinx.android.synthetic.main.fragment_login.*
import java.util.regex.Pattern


class LoginFragment : androidx.fragment.app.Fragment() {
    private var TAG = LoginFragment::class.java.simpleName

    private var mListener: OnLogInListener? = null
    private var mSignUpListener: OnSignUpSwitchListener? = null
    private var loginViewModel: LoginViewModel? = null
    private lateinit var binding: FragmentLoginBinding

    private var mProgress: ProgressBar? = null
    private var mLoginForm: RelativeLayout? = null
    private var mLoginButton: Button? = null
    private var mToolbar: Toolbar? = null
    private var mForgotPassword: TextView? = null
    private var mSignUp: TextView? = null
    private val PREFS_FILENAME = "com.jedmahonisgroup.gamepoint"
    private lateinit var sharedPreferences: SharedPreferences
    private var mEmailField: EditText? = null
    private var mPasswordField: EditText? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        val rootView = binding.root
        loginViewModel = ViewModelProviders.of(this, ViewModelFactory(this.activity as AppCompatActivity)).get(LoginViewModel::class.java)
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        loginViewModel!!.context = context
        setupUi(rootView)
        //binding.loginViewModel = loginViewModel
        return rootView
    }

    private fun setupUi(rootView: View) {
        mLoginButton = rootView.findViewById(R.id.loginFinishButton)
        mProgress = rootView.findViewById(R.id.loginPending)
        mLoginForm = rootView.findViewById(R.id.loginForm)
        mToolbar = rootView.findViewById(R.id.loginUpToolbar)
        mForgotPassword = rootView.findViewById(R.id.forgotPassword)
        mSignUp = rootView.findViewById(R.id.signup)


        //toolbar
        ((activity as AppCompatActivity)).setSupportActionBar(mToolbar)
        (activity as AppCompatActivity).supportActionBar?.title = ""
        mEmailField = rootView.findViewById(R.id.emailField)
        mPasswordField = rootView.findViewById(R.id.loginPassField)
        if (BuildConfig.DEBUG) {

            mEmailField?.setText("nienbkict@gmail.com")
            mPasswordField?.setText("12345678")
        }
        //status bar
        val window = this.activity?.window
        window?.statusBarColor = ContextCompat.getColor(this.requireActivity().baseContext, android.R.color.black)

        mToolbar!!.setNavigationOnClickListener {
            //on click
            activity?.onBackPressed();

        }

        mForgotPassword?.setOnClickListener {
            val intent = Intent(this.activity, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
        mSignUp?.setOnClickListener {
            mSignUpListener?.onSwitchToSignUp()
        }
        mPasswordField!!.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                //Perform Code
                    loginPressed()
                return@OnKeyListener true
            }
            false
        })



        login()

    }


     override fun onAttach(context: Context) {
        super.onAttach(context)
         if (context is OnLogInListener) {
             mListener = context
         } else {
             throw RuntimeException(requireContext().toString() + " must implement OnSignUpListener")
         }

         if (context is OnSignUpSwitchListener) {
             mSignUpListener = context
         } else {
             throw RuntimeException(requireContext().toString() + " must implement OnSignUpListener")
         }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
        mSignUpListener = null
    }


    private fun login() {
//        if (BuildConfig.DEBUG) {
//            emailField?.setText("rob+produser3@jmg.rocks")
//            loginPassField?.setText("Test1234")
//        }
        mLoginButton?.setOnClickListener {
            //check to see if txt fields are empty
            loginPressed()

        }
        loginResult()
    }

    private fun loginPressed() {
        val email = mEmailField?.text?.toString()
        val password = mPasswordField?.text?.toString()

        if (email.isNullOrEmpty() || password.isNullOrEmpty()){
            //display empty alert
            emptyFieldAlert(email, password)
        }else{
            //check to see if email was valid.
            val pattern = Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+")
            val matcher = pattern.matcher(email)
            val isValidEmail = matcher.matches()
            if (isValidEmail){
                //do the request
                val loginModel = LoginModel(
                    email = email,
                    password = password
                )
                loginViewModel?.onLoginClicked(loginModel)

            }else{
                //email was not a vaid one
                val title = "Oops!"
                val info = "Please enter a valid email"

                val posBtnTxt = "Ok"
                val negBtnTxt = "Cancel"

                val it = ""
                genericAlert(it, title, info, posBtnTxt, negBtnTxt)                }

        }
    }

    private fun emptyFieldAlert(email: String?, password: String?) {
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.above_18_alert, null)
        //AlertDialogBuilder
        val mBuilder = this.let { AlertDialog.Builder(requireContext()).setView(mDialogView) }
        //show dialog
        val mAlertDialog = mBuilder?.show()
        mAlertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val mTitle: TextView = mAlertDialog?.findViewById(R.id.required)!!
        val mInfo: TextView = mAlertDialog.findViewById(R.id.aboveEighteenText)!!
        val mPostBtn: TextView = mAlertDialog.findViewById(R.id.okBtn)!!
        val mNegBtn: TextView = mAlertDialog.findViewById(R.id.termsOfServiceBtn)!!

        mTitle.text = "Required"
        try {
            mInfo.text = when {
                email!!.isEmpty() -> "You need to enter your email."
                password!!.isEmpty() -> "You need to enter your password."
                else -> ""
            }
        } catch (e: Exception) {
            mInfo.text = "Please make sure the email and password fields are not empty and try again."
        }
        mPostBtn.text = "Ok"
        mNegBtn.visibility = View.GONE
        // mPostBtn.alig

        //login button cick of custom layout
        mDialogView.okBtn.setOnClickListener {
            mAlertDialog.dismiss()
        }
    }

    private fun genericAlert(it: String?, title: String, info: String, posBtnTxt: String, negBtnTxt: String) {
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.above_18_alert, null)
        //AlertDialogBuilder
        val mBuilder = this.let { AlertDialog.Builder(requireContext()).setView(mDialogView) }
        //show dialog
        val mAlertDialog = mBuilder.show()
        mAlertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val mTitle: TextView = mAlertDialog.findViewById(R.id.required)!!
        val mInfo: TextView = mAlertDialog.findViewById(R.id.aboveEighteenText)!!
        val mPostBtn: TextView = mAlertDialog.findViewById(R.id.okBtn)!!
        val mNegBtn: TextView = mAlertDialog.findViewById(R.id.termsOfServiceBtn)!!

        mTitle.text = title
        mInfo.text = info
        mPostBtn.text = posBtnTxt
        mNegBtn.visibility = View.GONE

        //login button cick of custom layout
        mDialogView.okBtn.setOnClickListener {
            mAlertDialog.dismiss()
        }
    }

    private fun loginResult() {
        loginViewModel?.loginStarting?.observe(viewLifecycleOwner, Observer {
            mProgress?.visibility = View.VISIBLE
            mLoginForm?.visibility = View.INVISIBLE
            Log.i(TAG, "Starting login")
        })
        //failed login
        loginViewModel?.errorMessage?.observe(viewLifecycleOwner, Observer { errorMessage ->
            mProgress?.visibility = View.GONE
            mLoginForm?.visibility = View.VISIBLE
            Log.e(TAG, "error loging in")

            displayErrorLogin(errorMessage)
           // if (errorMessage != null) showError(errorMessage) else hideError()
        })

        //successful login
        loginViewModel?.successMessage?.observe(viewLifecycleOwner, Observer { user ->
            Log.e(TAG, "success loging in")

            mProgress?.visibility = View.GONE
            mLoginForm?.visibility = View.VISIBLE
            if (user != null) {
                //regardless of what's going to happen, the user is loged in at this moment
                val editor = sharedPreferences.edit()
                editor.putBoolean(Constants.IsLogedIn, true)
                editor.apply()
                sharedPreferences.edit().putBoolean("pushTokenSent", false).apply()
                saveUserIdTosp(user.id.toString(), user.school)
                launchActivity()
            } else showError("Unable to logging in, please check your username and password and try again.")
        })
    }



    private fun saveUserIdTosp(id: String, school: School){
        val editor = sharedPreferences.edit()
        editor.putString(Constants.USER_ID,id)
        /*editor.putString(Constants.PRIMARY_COLOR, school.primary_color)
        editor.putString(Constants.SECONDARY_COLOR, school.secondary_color)
        editor.putString(Constants.PRIMARY_COLOR_DARK, school.dark_primary_color)
        editor.putString(Constants.SECONDARY_COLOR_DARK, school.dark_secondary_color)*/

        //TODO ASK TO CHANGE THE COLOR LATER, ATM USING BASED COLOR
        editor.putString(Constants.PRIMARY_COLOR, "#702DF5")
        editor.putString(Constants.SECONDARY_COLOR, "#702DF5")
        editor.putString(Constants.PRIMARY_COLOR_DARK, "#702DF5")
        editor.putString(Constants.SECONDARY_COLOR_DARK, "#702DF5")

        editor.apply()

    }

    private fun displayErrorLogin(errorMessage: String?) {
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.error_login, null)
        //AlertDialogBuilder
        if (errorMessage != null) {
            mDialogView.info.text = errorMessage
        }
        val mBuilder = this.let { it1 ->
            AlertDialog.Builder(requireContext())
                    .setView(mDialogView)
        }
        //show dialog
        val mAlertDialog = mBuilder?.show()
        mAlertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        //login button cick of custom layout

        mDialogView.close.setOnClickListener {
            mAlertDialog?.dismiss()


        }
    }

    private fun launchActivity() {
        val intent = Intent(this.activity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        activity?.finish()
    }


    private fun showError(errorMessage: String?) {
         Snackbar.make(binding.root, "Error, please try again $errorMessage", Snackbar.LENGTH_INDEFINITE).show()
    }

    interface OnSignUpSwitchListener{
        fun onSwitchToSignUp()
    }

    interface OnLogInListener {
        fun onLogin()
    }
}
