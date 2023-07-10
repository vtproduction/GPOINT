package com.jedmahonisgroup.gamepoint.ui.auth

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.ForgotPasswordModel
import kotlinx.android.synthetic.main.above_18_alert.view.*
import java.util.regex.Pattern


class ForgotPasswordActivity : AppCompatActivity() {

    private var TAG: String = ForgotPasswordActivity::class.java.simpleName
    private var forgotPasswordViewModel: ForgotPasswordViewModel? = null

    //UI
    private var mForgotPasswordEditText: EditText? = null
    private var mForgotPassBtn: Button? = null
    private var mToolbar: Toolbar? = null
    private var mPending: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        forgotPasswordViewModel = ViewModelProviders.of(this).get(ForgotPasswordViewModel::class.java)
        setUpUi()
        responseFromServer()
    }

    private fun setUpUi() {
        mForgotPasswordEditText = findViewById(R.id.forgotPassEmail)
        mForgotPassBtn = findViewById(R.id.forgotPassBtn)
        mToolbar = findViewById(R.id.forgotPassToolbar)
        mPending = findViewById(R.id.passResetPending)

        //status bar
        val window = this.window
        window.statusBarColor = ContextCompat.getColor(this.baseContext, android.R.color.black)


        //toolbar
        mToolbar!!.setNavigationOnClickListener {
            this.onBackPressed()

        }
        startProcess()

    }

    private fun startProcess() {
        val pattern = Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+")

        mForgotPassBtn!!.setOnClickListener {
            if (!mForgotPasswordEditText!!.text.isNullOrEmpty()) {
                val email = mForgotPasswordEditText!!.text.toString()
                val matcher = pattern.matcher(email)
                val emailMatches = matcher.matches()
                if (emailMatches){
                    //its a valid email, make the request
                    val resetRequest = ForgotPasswordModel(
                            email = email
                    )
                    forgotPasswordViewModel!!.resetPassword(resetRequest)
                }else{
                    //email did not match, ask user to enter valid email
                    val title = "Oops!"
                    val info = "Please enter a valid email"

                    val posBtnTxt = "Ok"
                    val negBtnTxt = "Cancel"

                    val it = ""
                    genericAlert(it, title, info, posBtnTxt, negBtnTxt)
                }

            }  else {
                //user left text field null
                val title = "Oops!"
                val info = "Please enter an email in the email field"

                val posBtnTxt = "Ok"
                val negBtnTxt = "Cancel"

                val it = ""
                genericAlert(it, title, info, posBtnTxt, negBtnTxt)
            }
        }
    }

    private fun responseFromServer() {

        forgotPasswordViewModel!!.startPassResetRequest.observe(this, Observer {
            mPending?.visibility = View.VISIBLE
            mForgotPasswordEditText?.visibility = View.INVISIBLE
            mForgotPassBtn?.visibility = View.INVISIBLE

        })

        forgotPasswordViewModel!!.sucessfullyResetPass.observe(this, Observer {
            mPending?.visibility = View.INVISIBLE
            mForgotPasswordEditText?.visibility = View.VISIBLE
            mForgotPassBtn?.visibility = View.VISIBLE
            //response from server
            Log.e(TAG, " reset password password success: $it")
            if (!it?.mssg.isNullOrEmpty()) {
                val title = "Success!"
                val info = "You should receive an email soon with more instructions for resetting your password."

                val posBtnTxt = "Ok"
                val negBtnTxt = ""
                genericAlert(it?.mssg, title, info, posBtnTxt, negBtnTxt)

            } else if (!it?.errors.isNullOrEmpty()) {
                val title = "Oops!"
                val info = "Email not found"

                val posBtnTxt = "Ok"
                val negBtnTxt = ""
                genericAlert(it?.errors, title, info, posBtnTxt, negBtnTxt)

            }

        })

        forgotPasswordViewModel!!.errorResetUserPass.observe(this, Observer {
            //display alert dialog failed
            mPending?.visibility = View.INVISIBLE
            mForgotPasswordEditText?.visibility = View.VISIBLE
            mForgotPassBtn?.visibility = View.VISIBLE

            Log.e(TAG, " failed to reset password $it")
            val title = "Oops!"
            val info = "Something went wrong, please try again later"

            val posBtnTxt = "Ok"
            val negBtnTxt = ""
            val isUserUnder18 = false

            genericAlert(it, title, info, posBtnTxt, negBtnTxt)
        })
    }

    private fun genericAlert(it: String?, title: String, info: String, posBtnTxt: String, negBtnTxt: String) {
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.above_18_alert, null)
        //AlertDialogBuilder
        val mBuilder = this.let { AlertDialog.Builder(this).setView(mDialogView) }
        //show dialog
        val mAlertDialog = mBuilder.show()
        mAlertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

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

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }
}
