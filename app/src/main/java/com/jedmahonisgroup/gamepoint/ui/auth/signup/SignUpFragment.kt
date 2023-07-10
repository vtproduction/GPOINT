package com.jedmahonisgroup.gamepoint.ui.auth.signup

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.TextView.BufferType
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.databinding.FragmentSignUpBinding
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.User
import com.jedmahonisgroup.gamepoint.model.UserModel
import com.jedmahonisgroup.gamepoint.model.UserResponse
import com.jedmahonisgroup.gamepoint.model.UserResponseModel
import com.jedmahonisgroup.gamepoint.model.school.School
import com.jedmahonisgroup.gamepoint.ui.auth.AuthActivity
import com.jedmahonisgroup.gamepoint.ui.auth.school.SelectSchoolFragment
import com.jedmahonisgroup.gamepoint.ui.settings.WebViewActivity
import com.jedmahonisgroup.gamepoint.utils.Constants
import com.jedmahonisgroup.gamepoint.utils.StringFormatter
import kotlinx.android.synthetic.main.above_18_alert.view.*
import kotlinx.android.synthetic.main.activity_edit_profile.*
import org.joda.time.LocalDateTime
import org.joda.time.Period
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder
import java.lang.reflect.InvocationTargetException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.math.sign


class SignUpFragment : androidx.fragment.app.Fragment(), SelectSchoolFragment.SendSchoolBack {
    private var TAG = SignUpFragment::class.java.simpleName

    private var listener: OnSignUpListener? = null


    private lateinit var binding: FragmentSignUpBinding
    private lateinit var signUpViewModel: SignUpViewModel

    private var errorSnackbar: Snackbar? = null

    private var mSignUpButton: Button? = null
    private var mLoginButton: Button? = null

    private var mTermsText: TextView? = null

    private var mBirthdayParentView: LinearLayout? = null

    private var mUserNameEditText: EditText? = null

    private var mLastNameEditText: EditText? = null

    private var mEmailEditText: EditText? = null

    private var mBirthdayEditText: EditText? = null

    private var mPhoneNumberEditText: EditText? = null

    private var mSignWithCodeEditText: EditText? = null

    private var mPasswordEditText: EditText? = null

    private var mPasswordConfirmEditText: EditText? = null

    private var mNickNameEditText: EditText? = null
    private var mSelectedSchoolTextView: TextView? = null
    private lateinit var userModel: UserModel
    private lateinit var selectedSchool: School

    private var mToolbar: Toolbar? = null
    private var mSignUpParent: LinearLayout? = null
    private var mSignUpPending: ProgressBar? = null

    private val PREFS_FILENAME = "com.jedmahonisgroup.gamepoint"
    private lateinit var sharedPreferences: SharedPreferences


    private val myCalendar: Calendar = Calendar.getInstance()

    var mDate: DatePickerDialog.OnDateSetListener? = null

    var selectedYear: Int = 1998
    var selectedMonth: Int = 5
    var selectedDay: Int = 1

    private var containerId: Int = 0
    private var fromSelectSchool = false
    private lateinit var schoolList : List<School>


    fun setUserModel(userM: UserModel, school: School){
        fromSelectSchool = true

        selectedSchool = school
        userModel = userM

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up, container, false)
        val rootView = binding.root
        signUpViewModel = ViewModelProviders.of(this, ViewModelFactory(this.activity as AppCompatActivity)).get(SignUpViewModel::class.java)
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        signUpViewModel.context = context
        containerId = container!!.id

        binding.viewModel = signUpViewModel

        setUpUi(rootView)
        termsText()

        getSchools()

        return rootView
    }

    private fun getSchools(){
        signUpViewModel.getSchools()
        getSchoolsResult()
    }

    @SuppressLint("ResourceType")
    private fun getSchoolsResult() {
//        signUpViewModel.loadingVisibility.observe(this, Observer { type ->
//                 mSignUpParent?.visibility = View.GONE
//                 mSignUpPending?.visibility = View.VISIBLE
//        })
        signUpViewModel.errorGetSchools.observe(viewLifecycleOwner, Observer { errorMessage ->
            mSignUpParent?.visibility = View.VISIBLE
            mSignUpPending?.visibility = View.GONE
            if (errorMessage != null) showError(errorMessage)
        })

        signUpViewModel.succesGetSchools.observe(viewLifecycleOwner, Observer { successMessage ->
            mSignUpParent?.visibility = View.VISIBLE
            mSignUpPending?.visibility = View.GONE
            if (successMessage != null) {

                schoolList = successMessage
//                Log.e("school", successMessage.toString())

//                schools = successMessage
//                initializeRecyclerView()
            }
        })
    }


    @SuppressLint("ResourceType")
    private fun setUpUi(rootView: View) {

        mSelectedSchoolTextView = rootView.findViewById(R.id.schoolTv)
        mSignUpButton = rootView.findViewById(R.id.signup)
        mLoginButton = rootView.findViewById(R.id.signin)
        mTermsText = rootView.findViewById(R.id.termsText)
        mBirthdayParentView = rootView.findViewById(R.id.birthdayFieldParent)
        mUserNameEditText = rootView.findViewById(R.id.userName)
        mLastNameEditText = rootView.findViewById(R.id.lastName)
        mEmailEditText = rootView.findViewById(R.id.emailField)
        mPhoneNumberEditText = rootView.findViewById(R.id.phoneNumberField)
        formatPhoneEditText()
        mSignWithCodeEditText = rootView.findViewById(R.id.signWithCodeField)
        mBirthdayEditText = rootView.findViewById(R.id.birthDayField)
        mPasswordEditText = rootView.findViewById(R.id.signUpPassword)
//        mPasswordConfirmEditText = rootView.findViewById(R.id.signUpConfirmPassword)
        mToolbar = rootView.findViewById(R.id.signUpToolbar)
        mSignUpParent = rootView.findViewById(R.id.signUpParent)
        mSignUpPending = rootView.findViewById(R.id.signUpPending)
        mNickNameEditText = rootView.findViewById(R.id.usernameField)

        mSelectedSchoolTextView!!.setOnClickListener {
            var bday = ""
            if (dateAsStringThatServerNeeds().isNotEmpty()){
                bday = dateAsStringThatServerNeeds()
            }
            var first = ""
            if (mUserNameEditText!!.text.toString().isNotEmpty()){
                first = mUserNameEditText!!.text.toString()
            }
            var last = ""
            if (mLastNameEditText!!.text.toString().isNotEmpty()){
                last = mLastNameEditText!!.text.toString()
            }
            var email = ""
            if (mEmailEditText!!.text.toString().isNotEmpty()){
                email = mEmailEditText!!.text.toString()
            }
            var signUpCode = ""
            if (mSignWithCodeEditText!!.text.toString().isNotEmpty()){
                signUpCode = mSignWithCodeEditText!!.text.toString()
            }
            var pass = ""
            if (mPasswordEditText!!.text.toString().isNotEmpty()){
                pass = mPasswordEditText!!.text.toString()
            }
            var phone = ""
            if (mPhoneNumberEditText!!.text.toString().isNotEmpty()){
                phone = mPhoneNumberEditText!!.text.toString().filter { it.isDigit() }
            }
//            var passCon = ""
//            if (mPasswordConfirmEditText!!.text.toString().isNotEmpty()){
//                passCon = mPasswordConfirmEditText!!.text.toString()
//            }
            var nickname = ""
            if (mNickNameEditText!!.text.toString().isNotEmpty()){
                nickname = mNickNameEditText!!.text.toString()
            }

            val user = UserModel(User(
                    first_name =first,
                    last_name =last,
                    email =email,
                    birthday = bday,
                    phone = phone,
                    nickname = nickname,
                    sign_with_code = signUpCode,
                    password =pass,
//                    password_confirmation =passCon,
                    school_id = 0
            ))

            val frag = SelectSchoolFragment()
            frag.setSchools(schoolList, user, false)
            val transaction = activity?.supportFragmentManager!!.beginTransaction()
            transaction.replace(containerId, frag)
//            transaction.disallowAddToBackStack()
            transaction.commit()
        }

//        mUserNameEditText!!.inputType = InputType.TYPE_CLASS_TEXT;
//        mLastNameEditText!!.inputType = InputType.TYPE_CLASS_TEXT;
        //change statusbar color
        val window = requireActivity().window
        window.statusBarColor = ContextCompat.getColor(this.requireActivity().baseContext, R.color.black_transparent)

        //toolbar
        ((activity as AppCompatActivity)).setSupportActionBar(mToolbar)
        (activity as AppCompatActivity).supportActionBar?.title = ""

        if (fromSelectSchool){
            mUserNameEditText?.setText(userModel.user.first_name)
            mLastNameEditText?.setText(userModel.user.last_name)
            mEmailEditText?.setText(userModel.user.email)
            mBirthdayEditText?.setText(userModel.user.birthday)
            mPhoneNumberEditText?.setText(userModel.user.phone)
            mNickNameEditText?.setText(userModel.user.nickname)
            mSignWithCodeEditText?.setText(userModel.user.sign_with_code)
            mPasswordEditText?.setText(userModel.user.password)
//            mPasswordConfirmEditText?.setText(userModel.user.password_confirmation)
            mSelectedSchoolTextView?.setText(selectedSchool.name)

        }
        /*
        val numberPicker = rootView.findViewById<NumberPicker>(R.id.phoneNumberField)

        if (numberPicker != null){
            numberPicker.minValue = 1000000000
            numberPicker.maxValue = 9999999999.toInt()

            numberPicker.setFormatter { i ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    NumberFormat.getCurrencyInstance(
                            Locale.US).format(i.toLong())
                } else {
                    //TODO("VERSION.SDK_INT < N")
                }
            }
            numberPicker.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {

                }
            }
        }
        */
        mToolbar!!.setNavigationOnClickListener {
            //on click
            activity?.onBackPressed()

        }

        datePickerAlert()
        createUser()

    }

    private fun formatPhoneEditText() {
        mPhoneNumberEditText!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                /* Let me prepare a StringBuilder to hold all digits of the edit text */
                val digits = StringBuilder()

                /* this is the phone StringBuilder that will hold the phone number */
                val phone = StringBuilder()

                /* let's take all characters from the edit text */
                val chars: CharArray = mPhoneNumberEditText!!.getText().toString().toCharArray()

                /* a loop to extract all digits */for (x in chars.indices) {
                    if (Character.isDigit(chars[x])) {
                        /* if its a digit append to digits string builder */
                        digits.append(chars[x])
                    }
                }
                if (digits.toString().length >= 3) {
                    /* our phone formatting starts at the third character  and starts with the country code*/
                    var countryCode = String()

                    /* we build the country code */countryCode += "(" + digits.toString().substring(0, 3) + ") "
                    /** and we append it to phone string builder  */
                    phone.append(countryCode)
                    /** if digits are more than or just 6, that means we already have our state code/region code  */
                    if (digits.toString().length >= 6) {
                        var regionCode = String()
                        /** we build the state/region code  */
                        regionCode += digits.toString().substring(3, 6) + "-"
                        /** we append the region code to phone  */
                        phone.append(regionCode)
                        /** the phone number will not go over 12 digits  if ten, set the limit to ten digits */
                        if (digits.toString().length >= 10) {
                            phone.append(digits.toString().substring(6, 10))
                        } else {
                            phone.append(digits.toString().substring(6))
                        }
                    } else {
                        phone.append(digits.toString().substring(3))
                    }
                    /** remove the watcher  so you can not capture the affectation you are going to make, to avoid infinite loop on text change  */
                    mPhoneNumberEditText!!.removeTextChangedListener(this)
                    /** set the new text to the EditText  */
                    mPhoneNumberEditText!!.setText(phone.toString())
                    /** bring the cursor to the end of input  */
                    mPhoneNumberEditText!!.setSelection(mPhoneNumberEditText!!.getText().toString().length)
                    /* bring back the watcher and go on listening to change events */mPhoneNumberEditText!!.addTextChangedListener(this)
                } else {
                    return
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun termsText() {
        val spanTxt = SpannableStringBuilder("By tapping Sign Up, you agree that you understand \nthe")
        spanTxt.append(" Term of Services")
        //spanTxt.setSpan( ForegroundColorSpan(Color.WHITE), 60, 70, 0);
        spanTxt.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(activity?.applicationContext, WebViewActivity::class.java)
                intent.putExtra("urlType", 1)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }, spanTxt.length - "Term of services".length, spanTxt.length, 0)
        spanTxt.setSpan(StyleSpan(Typeface.BOLD) , spanTxt.length - "Term of services".length, spanTxt.length, 0)
        spanTxt.setSpan(ForegroundColorSpan(Color.WHITE), spanTxt.length - "Term of services".length, spanTxt.length, 0)


        //spanTxt.setSpan( ForegroundColorSpan(Color.WHITE), 10,15 , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanTxt.append(" and agree to the")
        spanTxt.append(" Privacy \nPolicy")
        spanTxt.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(activity?.applicationContext, WebViewActivity::class.java)
                intent.putExtra("urlType", 0)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }

        }, spanTxt.length - " Privacy Policy".length, spanTxt.length, 0)
        spanTxt.setSpan(StyleSpan(Typeface.BOLD) , spanTxt.length - " Privacy Policy".length, spanTxt.length, 0)
        spanTxt.setSpan(ForegroundColorSpan(Color.WHITE), spanTxt.length - " Privacy Policy".length, spanTxt.length, 0)


        spanTxt.append(" and agree to the")
        spanTxt.append(" Terms of Contest")

        // spanTxt.setSpan(ForegroundColorSpan(Color.BLUE), spanTxt.length - " Terms of Contest".length, spanTxt.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanTxt.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(activity?.applicationContext, WebViewActivity::class.java)
                intent.putExtra("urlType", 2)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }

        }, spanTxt.length - "Terms of Contest".length, spanTxt.length, 0)

        spanTxt.setSpan(StyleSpan(Typeface.BOLD) , spanTxt.length - "Terms of Contest".length, spanTxt.length, 0)
        spanTxt.setSpan(ForegroundColorSpan(Color.WHITE), spanTxt.length - "Terms of Contest".length, spanTxt.length, 0)
        mTermsText?.movementMethod = LinkMovementMethod.getInstance()
        mTermsText?.setText(spanTxt, BufferType.SPANNABLE)

    }

    private fun createUser() {
        mSignUpButton?.setOnClickListener {
            areFieldsEmpty()
        }
        mLoginButton?.setOnClickListener {
            listener?.onSwitchToLogin()
        }
        //maybe change location of this thing
        signUpResult()
    }

    private fun areFieldsEmpty() {
        val firstName = mUserNameEditText?.text?.toString()
        val lastName = mLastNameEditText?.text?.toString()
        val email = mEmailEditText?.text?.toString()
        val birthday = mBirthdayEditText?.text
        val password = mPasswordEditText?.text?.toString()
//        val passwordConfirmation = mPasswordConfirmEditText?.text?.toString()
        val pattern = Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+")
        val matcher = pattern.matcher(email)
        val isValidEmail = matcher.matches()
        val phone = mPhoneNumberEditText?.text?.toString()

        if (firstName.isNullOrEmpty() || lastName.isNullOrEmpty() ||
                email.isNullOrEmpty() || birthday.isNullOrEmpty() || phone.isNullOrEmpty() || password.isNullOrEmpty()) {
            //display alert dialog

            emptyFieldAlert(firstName, lastName, email, birthday, password, phone)

        } else {
            //is email valid
            if (isValidEmail) {
                val isValidPhone = StringFormatter.isValidPhoneNumber(phone)
                //val isValidPhone = phone.toInt() in 1000000000..9999999999
                //email is valid but do passwords match || phone not valid
                if (isValidPhone && password.length >= 8) {
                    //this is our happy place, everything is right
                    confirmUserAge()
                } else {
                    if (!isValidPhone){
                        //phone is not valid
                        val info = "please enter a valid phone number"
                        formValidatorAlert(info)
                    }else if (password.length < 8){
                        //passwords must be minimum 8 character
                        val info = "please enter a password with minimum 8 characters"
                        formValidatorAlert(info)
                    }else{
                        //passwords do not match
                        val info = "passwords don't match"
                        formValidatorAlert(info)
                    }
                }

            } else {
                //email is not valid
                val info = "please enter a valid email"
                formValidatorAlert(info)
            }
        }
    }

    private fun requestSignUp() {
        val user = UserModel(User(

                first_name = mUserNameEditText?.text?.toString()!!,
                last_name = mLastNameEditText?.text?.toString()!!,
                email = mEmailEditText?.text?.toString()!!,
                birthday = dateAsStringThatServerNeeds(),
                phone = mPhoneNumberEditText?.text?.toString()!!,
                nickname = mNickNameEditText?.text?.toString()!!,
                sign_with_code = mSignWithCodeEditText?.text?.toString()!!,
                password = mPasswordEditText?.text?.toString()!!,
//                password_confirmation = mPasswordConfirmEditText?.text?.toString()!!,
                school_id = selectedSchool.id
        ))


        signUpViewModel.onSignUpClicked(user)
    }

    private fun confirmUserAge() {
        val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

        val todayDateTime = formatter.parseDateTime(StringFormatter.getDate().toString())
        val dateToday = LocalDateTime(todayDateTime)

        val fmtr = DateTimeFormat.forPattern("dd/mm/yyyy")
        val birthdayDateTime = fmtr.parseDateTime(dateAsStringThatServerNeeds())
        val birthday = LocalDateTime(birthdayDateTime)

        val period = Period(birthday, dateToday)
        val periodFormatter: PeriodFormatter = PeriodFormatterBuilder()
                .minimumPrintedDigits(2)
                .appendYears()
                .toFormatter()
        Log.i(TAG, "how many years are past ${periodFormatter.print(period)}")

        //find user's real age.
        when {
            birthday.isAfter(dateToday) -> {
                //birthday is after today
                val age = periodFormatter.print(period).toInt()
                displayAppropriateAlert(age)
                Log.i(TAG, "user is over 18: ${periodFormatter.print(period)}")

            }
            birthday.isEqual(dateToday) -> {
                //user turned 18 today
                val age = periodFormatter.print(period).toInt()
                displayAppropriateAlert(age)
                Log.i(TAG, "user turned 18 today: ${periodFormatter.print(period)}")


            }
            birthday.isBefore(dateToday) -> {
                // user's birthday is before today
                val age = periodFormatter.print(period).toInt() - 1
                displayAppropriateAlert(age)

                Log.i(TAG, "user is under 18 ${periodFormatter.print(period).toInt() - 1}")


            }
        }
    }

    private fun displayAppropriateAlert(age: Int) {
        if (age > 18 || age == 18) {
            //user is above 18, show them over 18 alert
            val title = "Confirm Age"
            val info = "I confirm that I am at least 18 years of age"

            val posBtnTxt = "Confirm"
            val negBtnTxt = "Cancel"
            val isUserUnder18 = false
            genericAlert(title, info, posBtnTxt, negBtnTxt, isUserUnder18)

        } else {
            //user is under 18 don't let them in
            val title = "Required"
            val info = "You must be 18 years or older to use this app. Please refer to the terms of service"

            val posBtnTxt = "Ok"
            val negBtnTxt = "Terms of Service"
            val isUserUnder18 = true
            genericAlert(title, info, posBtnTxt, negBtnTxt, isUserUnder18)

        }

    }

    private fun genericAlert(title: String, info: String, posBtnTxt: String, negBtnTxt: String, userUnder18: Boolean) {
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

        mTitle.text = title
        mInfo.text = info
        mPostBtn.text = posBtnTxt
        mNegBtn.text = negBtnTxt

        //login button cick of custom layout
        mDialogView.okBtn.setOnClickListener {
            if (userUnder18) {
                //warn them
                mAlertDialog.dismiss()
            } else {
                //make the request
                requestSignUp()
                mAlertDialog.dismiss()

            }

        }
        mDialogView.termsOfServiceBtn.setOnClickListener {
            //user wants to learn more, take them to terms and service
            if (userUnder18) {
                val intent = Intent(this.context?.applicationContext, WebViewActivity::class.java)
                intent.putExtra("urlType", 0)
                startActivity(intent)
                mAlertDialog.dismiss()

            } else {
                //make the request
                mAlertDialog.dismiss()
            }
        }
    }

    private fun emptyFieldAlert(firstName: String?, lastName: String?, email: String?, birthday: Editable?, password: String?, phone: String?) {
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
        mInfo.text = when {
            firstName!!.isEmpty() -> "You need to enter your first name"
            lastName!!.isEmpty() -> "You need to enter your last name name"
            email!!.isEmpty() -> "You need to enter your email"
            birthday!!.isEmpty() -> "You need to enter your birthday"
            phone!!.isEmpty() -> "You need to enter your phone number"
            password!!.isEmpty() -> "You need to enter your password"
            else -> ""
        }
        mPostBtn.text = "Ok"
        mNegBtn.visibility = View.GONE
        // mPostBtn.alig

        //login button cick of custom layout
        mDialogView.okBtn.setOnClickListener {
            mAlertDialog.dismiss()
        }
    }

    private fun formValidatorAlert(info: String) {
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

        mTitle.text = "Oops!"
        mInfo.text = info
        mPostBtn.text = "Ok"
        mNegBtn.visibility = View.INVISIBLE

        //login button click of custom layout
        mDialogView.okBtn.setOnClickListener {
            mAlertDialog.dismiss()
        }

    }

    private fun datePickerAlert() {
        mDate = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            // TODO Auto-generated method stub
            selectedDay = dayOfMonth
            selectedMonth = monthOfYear
            selectedYear = year
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel()
        }

        mBirthdayEditText?.setOnClickListener {
            Log.e(TAG, "birthdyFieldClickRecognized")
            if (mBirthdayEditText?.text.toString().isNotEmpty()) {

            }
            val datePickerDialog = DatePickerDialog(requireContext(), android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDate, selectedYear, selectedMonth, selectedDay)
            datePickerDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            datePickerDialog.show()
        }
    }

    private fun updateLabel() {
        val myFormat = "MM/dd/yy" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.US)

        mBirthdayEditText?.setText(sdf.format(myCalendar.time))
    }

    private fun dateAsStringThatServerNeeds(): String {
        try {

            var spf = SimpleDateFormat("MM/dd/yyyy")
            var bdayToUse = ""
            if (!mBirthdayEditText?.text.isNullOrEmpty()) {
                val newBirthDate = spf.parse(mBirthdayEditText?.text?.toString())
                spf = SimpleDateFormat("dd/MM/yyyy")
                bdayToUse = spf.format(newBirthDate)
            }
            Log.e("JMG", "formattedDate: " + bdayToUse)
            return bdayToUse
        } catch (e: InvocationTargetException) {
            Log.e(TAG, "user did not select birthday $e")
        }
        return null.toString()

    }

    private fun signUpResult() {
        signUpViewModel.loadingVisibility.observe(viewLifecycleOwner, Observer { type ->
            mSignUpParent?.visibility = View.GONE
            mSignUpPending?.visibility = View.VISIBLE
        })
        signUpViewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            mSignUpParent?.visibility = View.VISIBLE
            mSignUpPending?.visibility = View.GONE
            if (errorMessage != null) showError(errorMessage)
        })

        signUpViewModel.userSignUpSuccess.observe(viewLifecycleOwner, Observer { successMessage ->
            mSignUpParent?.visibility = View.VISIBLE
            mSignUpPending?.visibility = View.GONE
            if (successMessage != null) {
                //launchActivity(successMessage)
                //save user id to sp
                val editor = sharedPreferences.edit()
                editor.putString(Constants.USER_ID, successMessage.id.toString())
                editor.putString(Constants.PRIMARY_COLOR, successMessage.school.primary_color)
                editor.putString(Constants.SECONDARY_COLOR, successMessage.school.secondary_color)
                editor.putString(Constants.PRIMARY_COLOR_DARK, successMessage.school.dark_primary_color)
                editor.putString(Constants.SECONDARY_COLOR_DARK, successMessage.school.dark_secondary_color)
                editor.putBoolean(Constants.IsLogedIn, true)
                editor.apply()
                sharedPreferences.edit().putBoolean("pushTokenSent", false).apply()
                listener?.onSignUp(successMessage)
            }

        })
    }

    private fun showError(errorMessage: String?) {
//        if (errorMessage.equals("HTTP 422 Unprocessable Entity")) {
//            Snackbar.make(binding.root, "Error, User already exits", Snackbar.LENGTH_INDEFINITE).show()
//        }
//        Snackbar.make(binding.root, errorMessage.toString(), Snackbar.LENGTH_INDEFINITE).show()
        val builder = AlertDialog.Builder(requireContext())
        //set title for alert dialog
        builder.setTitle("Oops!")
        //set message for alert dialog
        builder.setMessage(errorMessage)
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("Ok"){ dialogInterface, which ->
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()

    }

//    private fun hideError() {
//        errorSnackbar?.dismiss()
//    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSignUpListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnSignUpListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnSignUpListener {
        fun onSignUp(user: UserResponse?)
        fun onSwitchToLogin()
    }

    override fun onSchoolSelected(school: School) {
        fromSelectSchool = true

        selectedSchool = school
//        userModel = userM
        schoolTv.setText(school.name)
    }

}
