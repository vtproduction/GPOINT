package com.jedmahonisgroup.gamepoint.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.ui.UploadPictureActivity
import com.jedmahonisgroup.gamepoint.ui.auth.login.LoginFragment
import com.jedmahonisgroup.gamepoint.ui.auth.signup.SignUpFragment


class AuthOptionsFragment : Fragment() {
    private var TAG: String = AuthOptionsFragment::class.java.simpleName

    private var listener: OnFragmentInteractionListener? = null

    private var signup: Button? = null
    private var login: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_auth_options, container, false)
        loginOrSignUpClicked(rootView)
        return rootView
    }

    private fun loginOrSignUpClicked(rootView: View) {
        signup = rootView.findViewById(R.id.authSignUpButton)
        signup!!.setOnClickListener {
            listener?.onChangeFragment(SignUpFragment())
            //launchActivity()
        }

        login = rootView.findViewById(R.id.authLoginButton)
        login!!.setOnClickListener {
            listener?.onChangeFragment(LoginFragment())
        }

    }




    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnSignUpListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    interface OnFragmentInteractionListener {
        fun onChangeFragment(fragment: Fragment)
    }

}
