package com.jedmahonisgroup.gamepoint.ui.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.adapters.createpostgallery.CreatePostGalleryAdapter
import com.jedmahonisgroup.gamepoint.database.model.UserDatabaseModel
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.*
import com.jedmahonisgroup.gamepoint.model.school.School
import com.jedmahonisgroup.gamepoint.ui.BaseActivity
import com.jedmahonisgroup.gamepoint.ui.auth.school.SelectSchoolFragment
import com.jedmahonisgroup.gamepoint.ui.auth.signup.SignUpFragment
import com.jedmahonisgroup.gamepoint.ui.auth.signup.SignUpViewModel
import com.jedmahonisgroup.gamepoint.utils.Constants
import com.jedmahonisgroup.gamepoint.utils.Constants.PREFS_FILENAME
import com.jedmahonisgroup.gamepoint.utils.setLocalImage
import kotlinx.android.synthetic.main.above_18_alert.view.*
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


class EditProfile : BaseActivity(), SelectSchoolFragment.SendSchoolBack {
    private var TAG: String = EditProfile::class.java.simpleName
    //utils
    private var mEditProfileViewModel: EditProfileViewModel? = null
    private var mToken: String? = null
    private var mUserId: String? = null
    private var mUser: UserResponse? = null
    var date: DatePickerDialog.OnDateSetListener? = null
    private val myCalendar: Calendar = Calendar.getInstance()

    private var mAvatar: String? = null

    //UI
    private var mToolbar: MaterialToolbar? = null
    private var mBirthdayEditText: EditText? = null
    private var mProfilePicture: ImageView? = null
    private var mBottomHalf: RelativeLayout? = null
    private var mSchoolTv: TextView? = null
    //misc
    var selectedYear: Int = 1998
    var selectedMonth: Int = 5
    var selectedDay: Int = 1
    private lateinit var schoolList : List<School>

    private lateinit var userModel: UserModel
    private lateinit var selectedSchool: School
    private var fromSelectSchool = false
    private lateinit var signUpViewModel: SignUpViewModel
    private var containerId: Int = 0
    private lateinit var parentRelative: RelativeLayout

    private var listener: SignUpFragment.OnSignUpListener? = null

    companion object {
        private const val PROFILE_IMAGE_REQ_CODE = 101
    }

    val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val file = ImagePicker.getFile(it.data)

            Log.e("TAG", "Path:${file?.absolutePath}")
            doSomething(convertToBase64(file!!.path))
            mProfilePicture?.setLocalImage(file, true)


        } else if (it.resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(it.data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onSchoolSelected(school: School) {
        fromSelectSchool = true

        selectedSchool = school
//        userModel = userM
        schoolTv.setText(school.name)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        mEditProfileViewModel = ViewModelProviders.of(this, ViewModelFactory(this)).get(EditProfileViewModel::class.java)
        mEditProfileViewModel?.context = baseContext
        mEditProfileViewModel?.userFromDb()
        mBirthdayEditText = findViewById(R.id.birthDayField)
        parentRelative = findViewById(R.id.parentRelative)
        setToolbarColors(edit_profile_toolbar)
        mSchoolTv = findViewById(R.id.schoolTv)
        signUpViewModel = ViewModelProviders.of(this, ViewModelFactory(this as AppCompatActivity)).get(SignUpViewModel::class.java)
        doSchoolStuff()
        resultsFromServer()
        setupUi()

    }

    private fun doSchoolStuff() {
        getSchools()

        var bday = ""
        if (birthDayField.text.toString().isNotEmpty()){
            bday = birthDayField.text.toString()
        }
        var first = ""
        if (editUserName!!.text.toString().isNotEmpty()){
            first = editUserName!!.text.toString()
        }
        var last = ""
        if (editLastName!!.text.toString().isNotEmpty()){
            last = editLastName!!.text.toString()
        }
        var email = ""
        if (editEmailField!!.text.toString().isNotEmpty()){
            email = editEmailField!!.text.toString()
        }
        var signUpCode = ""
        if (editSignWithCodeField!!.text.toString().isNotEmpty()){
            signUpCode = editSignWithCodeField!!.text.toString()
        }
        var pass = ""

        var phone = ""
        if (editPhoneNumberField!!.text.toString().isNotEmpty()){
            phone = editPhoneNumberField!!.text.toString()
        }
//            var passCon = ""
//            if (mPasswordConfirmEditText!!.text.toString().isNotEmpty()){
//                passCon = mPasswordConfirmEditText!!.text.toString()
//            }
        var nickname = ""
        if (editnickNameField!!.text.toString().isNotEmpty()){
            nickname = editnickNameField!!.text.toString()
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
        mSchoolTv?.setOnClickListener {
            val frag = SelectSchoolFragment()
            frag.setSchools(schoolList, user, true)
            val transaction = this.supportFragmentManager!!.beginTransaction()
            Log.e("JMG", "174")
            transaction.add(R.id.parentRelative , frag)
            Log.e("JMG", "176")
            transaction.addToBackStack("hi")
//            transaction.commitAllowingStateLoss()
//            transaction.disallowAddToBackStack()
            transaction.commit()
        }
    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        return super.onCreateView(parent, name, context, attrs)
        containerId = parent!!.id
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
        signUpViewModel.errorGetSchools.observe(this, Observer { errorMessage ->
//            mSignUpParent?.visibility = View.VISIBLE
//            mSignUpPending?.visibility = View.GONE
            if (errorMessage != null) showError(errorMessage)
        })

        signUpViewModel.succesGetSchools.observe(this, Observer { successMessage ->
//            mSignUpParent?.visibility = View.VISIBLE
//            mSignUpPending?.visibility = View.GONE
            if (successMessage != null) {

                schoolList = successMessage

//                Log.e("school", successMessage.toString())

//                schools = successMessage
//                initializeRecyclerView()
            }
        })
    }


    private fun showError(errorMessage: String?) {
//        if (errorMessage.equals("HTTP 422 Unprocessable Entity")) {
//            Snackbar.make(binding.root, "Error, User already exits", Snackbar.LENGTH_INDEFINITE).show()
//        }
//        Snackbar.make(binding.root, errorMessage.toString(), Snackbar.LENGTH_INDEFINITE).show()
        val builder = AlertDialog.Builder(this)
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            // File object will not be null for RESULT_OK
            val file = ImagePicker.getFile(data)

            Log.e("TAG", "Path:${file?.absolutePath}")
            if (requestCode == PROFILE_IMAGE_REQ_CODE) {
                doSomething(convertToBase64(file!!.path))
                mProfilePicture?.setLocalImage(file, true)
            }

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun doSomething(image: String) {
        Log.i(TAG, "Base 64 String is: data:image/png;base64,$image")
        mAvatar = "data:image/png;base64,$image"
    }

    private fun convertToBase64(imagePath: String): String {

        val bm = BitmapFactory.decodeFile(imagePath)

        val baos = ByteArrayOutputStream()

        bm.compress(Bitmap.CompressFormat.JPEG, 50, baos)

        val byteArrayImage = baos.toByteArray()

        return Base64.encodeToString(byteArrayImage, Base64.DEFAULT)

    }

    private fun setupUi() {
        mProfilePicture = findViewById(R.id.overlapImage)
        mBottomHalf = findViewById(R.id.bottomhalf)
        datePickerAlert()

        toolbar()
        updateProfile.setOnClickListener {
            updateProfile()
        }

        mProfilePicture?.setOnClickListener {
            ImagePicker.with(this)
                    .cropSquare() // Crop Square image(Optional)
                    .maxResultSize(620, 620) // Final image resolution will be less than 620 x 620(Optional)
                .createIntentFromDialog { launcher.launch(it) }
        }



        editPhoneNumberField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                /* Let me prepare a StringBuilder to hold all digits of the edit text */
                val digits = StringBuilder()

                /* this is the phone StringBuilder that will hold the phone number */
                val phone = StringBuilder()

                /* let's take all characters from the edit text */
                val chars: CharArray = editPhoneNumberField.getText().toString().toCharArray()

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
                    editPhoneNumberField.removeTextChangedListener(this)
                    /** set the new text to the EditText  */
                    editPhoneNumberField.setText(phone.toString())
                    /** bring the cursor to the end of input  */
                    editPhoneNumberField.setSelection(editPhoneNumberField.getText().toString().length)
                    /* bring back the watcher and go on listening to change events */editPhoneNumberField.addTextChangedListener(this)
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

    @SuppressLint("LongLogTag")
    private fun updateProfile() {
        val firstName = editUserName.text?.toString()
        val lastName = editLastName.text?.toString()
        val email = editEmailField.text?.toString()
        val birthday = mBirthdayEditText!!.text?.toString()
        Log.e("JMG", "birthday: " + birthday)
        val pattern = Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+")
        val matcher = pattern.matcher(email)
        val isEmailValid = matcher.matches()


        var phone = editPhoneNumberField.text?.toString()

        if (phone != null) {
            phone = phone.replace("(", "")
        }
        if (phone != null) {
            phone = phone.replace(")", "")
        }
        if (phone != null) {
            phone = phone.replace("-", "")
        }


        val signWithCode = editSignWithCodeField.text?.toString()
        if (firstName.isNullOrEmpty() || lastName.isNullOrEmpty() || email.isNullOrEmpty() || birthday.isNullOrEmpty() || phone.isNullOrEmpty()) {
            //show user they must fill out everything
            emptyFieldAlert(firstName, lastName, email, birthday!!, phone)
        } else {
            //make sure its a valid email

            if (isEmailValid) {
                //yes its a valid email
                    var schlId = mUser!!.school.id
                    if (selectedSchool != null) {
                        schlId = selectedSchool.id
                    }
                    Log.e("JMG", "selectedSchool should save: " + selectedSchool.name + " selectedSchool: " + selectedSchool.id)
                val update = UpdateProfile(
                        user = UserUpdate(
                                first_name = if (editUserName.text.isNullOrEmpty()) mUser!!.first_name else editUserName.text.toString(),
                                last_name = if (editLastName.text.isNullOrEmpty()) mUser!!.last_name else editLastName.text.toString(),
                                email = if (editEmailField.text.isNullOrEmpty()) mUser!!.email else editEmailField.text.toString(),
                                birthday = birthday,
                                nickname = if (editnickNameField.text.isNullOrEmpty()) mUser!!.nickname else editnickNameField.text.toString(),
                                phone = if (editPhoneNumberField.text.isNullOrEmpty()) mUser!!.phone!! else editPhoneNumberField.text.toString(),
                                sign_with_code = if (editSignWithCodeField.text.isNullOrEmpty()) mUser!!.sign_with_code else editSignWithCodeField.text.toString(),
                                school_id = schlId.toString(),
                                avatar = if (mAvatar.isNullOrEmpty()) "" else mAvatar!!
                        )
                )
                if (mUser!!.school.id != schlId) {
                    val mDialogView = LayoutInflater.from(this).inflate(R.layout.above_18_alert, null)
                    //AlertDialogBuilder
                    val mBuilder = this.let { AlertDialog.Builder(this).setView(mDialogView) }
                    //show dialog
                    val mAlertDialog = mBuilder?.show()
                    mAlertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

                    val mTitle: TextView = mAlertDialog?.findViewById(R.id.required)!!
                    val mInfo: TextView = mAlertDialog.findViewById(R.id.aboveEighteenText)!!
                    val mPostBtn: TextView = mAlertDialog.findViewById(R.id.okBtn)!!
                    val mNegBtn: TextView = mAlertDialog.findViewById(R.id.termsOfServiceBtn)!!

                    mTitle.setTextColor(getSchoolColor()!!)

                    mPostBtn.setTextColor(getSchoolColor()!!)
                    mNegBtn.setTextColor(getSchoolColor()!!)

                    mTitle.text = getText(R.string.change_schools_title)
                    mInfo.text = getText(R.string.change_schools_desc)
                    mPostBtn.text = getText(R.string.change_schools)
                    mNegBtn.text = "Cancel"
                    mNegBtn.visibility = View.VISIBLE
                    // mPostBtn.alig

                    //login button cick of custom layout
                    mPostBtn.setOnClickListener {
                        mEditProfileViewModel?.updateUser(mToken.toString(), mUserId.toString(), update)
                        mAlertDialog.dismiss()
                    }
                    mNegBtn.setOnClickListener{
                        mAlertDialog.dismiss()
                    }
                } else {
                    mEditProfileViewModel?.updateUser(mToken.toString(), mUserId.toString(), update)
                }
            } else {
                //display email not valid alert.
                val info = "please enter a valid email"
                formValidatorAlert(info)
            }
        }
    }

    private fun datePickerAlert() {
        date = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
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
            val datePickerDialog = DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, date, selectedYear, selectedMonth, selectedDay)
            datePickerDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            datePickerDialog.show()
        }
    }

    private fun updateLabel() {
        val myFormat = "MM/dd/yyyy" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.US)

        mBirthdayEditText?.setText(sdf.format(myCalendar.time))
    }

    private fun formValidatorAlert(info: String) {
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.above_18_alert, null)
        //AlertDialogBuilder
        val mBuilder = this.let { AlertDialog.Builder(this).setView(mDialogView) }
        //show dialog
        val mAlertDialog = mBuilder?.show()
        mAlertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val mTitle: TextView = mAlertDialog?.findViewById(R.id.required)!!
        val mInfo: TextView = mAlertDialog.findViewById(R.id.aboveEighteenText)!!
        val mPostBtn: TextView = mAlertDialog.findViewById(R.id.okBtn)!!
        val mNegBtn: TextView = mAlertDialog.findViewById(R.id.termsOfServiceBtn)!!

        mTitle.setTextColor(getSchoolColor()!!)

        mPostBtn.setTextColor(getSchoolColor()!!)
        mNegBtn.setTextColor(getSchoolColor()!!)

        mTitle.text = "Oops!"
        mInfo.text = info
        mPostBtn.text = "Ok"
        mNegBtn.visibility = View.INVISIBLE

        //login button cick of custom layout
        mDialogView.okBtn.setOnClickListener {
            mAlertDialog.dismiss()

        }

    }

    private fun toolbar() {
        mToolbar = findViewById(R.id.edit_profile_toolbar)
        setSupportActionBar(mToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.icon_feather_chevron_left_orange)

        mToolbar!!.setNavigationOnClickListener {
            //on click
            this.onBackPressed()

        }
    }

    private fun resultsFromServer() {
        //get user from database
        mEditProfileViewModel?.userFromDbSuccess?.observe(this, Observer {

            if (it != null) {

                displayUser(it)
                mUser = it
            }

        })

        //could not load user from db, we need to get server data or try logout first
        mEditProfileViewModel?.userFromDbError?.observe(this, Observer {


            //something went wrong alert
            Log.e(TAG, "could not get user form db")
        })

        /**
         * Update profile response
         */

        mEditProfileViewModel?.startRequest?.observe(this, Observer {
//            mBottomHalf?.visibility = View.INVISIBLE
//            editProfilePending?.visibility = View.VISIBLE
        })

        mEditProfileViewModel?.successUpdateingProfile?.observe(this, Observer {
//            mBottomHalf?.visibility = View.VISIBLE
//            editProfilePending?.visibility = View.INVISIBLE
//            val title = "Success!"
//            val info = "Successfully update your profile"
//            val posBtn = "Thanks"
//
//            genericAlert(title, info, posBtn)
            Log.e("JMG", "it.school.id: " + it.school.id)

//            saveUser(it)
            Log.e("JMG", "should finish")
            val sharedPreferences = this.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(Constants.PRIMARY_COLOR, it.school.primary_color)
            editor.putString(Constants.SECONDARY_COLOR, it.school.secondary_color)
            editor.putString(Constants.PRIMARY_COLOR_DARK, it.school.dark_primary_color)
            editor.putString(Constants.SECONDARY_COLOR_DARK, it.school.dark_secondary_color)
            editor.putBoolean(Constants.IsLogedIn, true)
            editor.apply()
            finish()
            //save user model to db
        })

        mEditProfileViewModel?.errorUpdateingProfile?.observe(this, Observer {
            //display alert diag, toast or snack to let the user know something went wrong
            mBottomHalf?.visibility = View.VISIBLE
            editProfilePending?.visibility = View.INVISIBLE
            errorLoadingAlert()
            Log.e(TAG, "Failed to update profile")

            val title = "Oops!"
            val info = "Something went wrong while trying to update your profile, please try again later"
            val posBtn = "Ok"

            genericAlert(title, info, posBtn)
        })
    }

//    private fun saveUser(it: UserResponse?) {
//        val gson = Gson()
//        Log.e("JMG", "birthday in saveUser: " + it!!.birthday +" or from mUser: " + mUser!!.birthday)
//        val login = Login(
//                refresh_token = if (it!!.login?.refresh_token != null) it!!.login.refresh_token else mUser!!.login.refresh_token,
//                token = if (it!!.login?.token != null) it!!.login?.token else mUser!!.login.token,
//                expires = if (it!!.login?.expires != null) it!!.login?.expires else mUser!!.login.expires
//        )
//
//                val user = UserResponse(
//                        id = if (it!!.id != null) it!!.id else mUser!!.id,
//                        first_name = if (it!!.first_name != null) it!!.first_name else mUser!!.first_name,
//                        last_name = if (it!!.last_name != null) it!!.last_name else mUser!!.last_name,
//                        email = if (it!!.email != null) it!!.email else mUser!!.email,
//                        avatar = if (it!!.avatar != null) it!!.avatar else mUser!!.avatar,
//                        birthday = if (it!!.birthday != null) it!!.birthday else mUser!!.birthday,
//                        phone = if (it!!.phone != null) it!!.phone else mUser!!.phone,
//                        sign_with_code = if (it!!.sign_with_code != null) it!!.sign_with_code else mUser!!.sign_with_code,
//                        total_points = if (it!!.total_points != null) it!!.total_points else mUser!!.total_points,
//                        current_streak = if (it!!.current_streak != null) it!!.current_streak else mUser!!.current_streak,
//                        pick_rank = if (it!!.pick_rank != null) it!!.pick_rank else mUser!!.pick_rank,
//                        highest_streak = if (it!!.highest_streak != null) it!!.highest_streak else mUser!!.highest_streak,
//                        this_month_event_points = if (it!!.this_month_event_points != null) it!!.this_month_event_points else mUser!!.this_month_event_points,
//                        event_rank = if (it!!.event_rank != null) it!!.event_rank else mUser!!.event_rank,
//                        redeemable = if (it!!.redeemable != null) it!!.redeemable else mUser!!.redeemable,
//                        user_picks = if (it!!.user_picks != null) it!!.user_picks else mUser!!.user_picks,
//                        check_ins = if (it!!.check_ins != null)  it!!.check_ins else mUser!!.check_ins,
//                        nickname = if (it!!.nickname != null) it!!.nickname else mUser!!.nickname,
//                        url = if (it!!.url != null) it!!.url else mUser!!.url,
//                        sign_up_code = it!!.sign_up_code!!,
//                        login = login,
//                        private_posts = if (it!!.private_posts != null) it!!.private_posts else mUser!!.private_posts,
//                        friend_request = it!!.friend_request,
//                        streaks_prize =if (it!!.streaks_prize != null) it!!.streaks_prize else mUser!!.streaks_prize,
//                        points_prize = if (it!!.points_prize != null) it!!.points_prize else mUser!!.points_prize,
//                        school = if (it!!.school != null) it!!.school else mUser!!.school
//                )
//
//
//
//
//        mEditProfileViewModel?.saveUserToDb(user)

//    }

    private fun genericAlert(title: String, info: String, posBtn: String) {
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.above_18_alert, null)
        //AlertDialogBuilder
        val mBuilder = this.let { AlertDialog.Builder(this).setView(mDialogView) }
        //show dialog
        val mAlertDialog = mBuilder?.show()
        mAlertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val mTitle: TextView = mAlertDialog?.findViewById(R.id.required)!!
        val mInfo: TextView = mAlertDialog.findViewById(R.id.aboveEighteenText)!!
        val mPostBtn: TextView = mAlertDialog.findViewById(R.id.okBtn)!!
        val mNegBtn: TextView = mAlertDialog.findViewById(R.id.termsOfServiceBtn)!!

        mTitle.setTextColor(getSchoolColor()!!)

        mPostBtn.setTextColor(getSchoolColor()!!)
        mNegBtn.setTextColor(getSchoolColor()!!)

        mTitle.text = title
        mInfo.text = info
        mPostBtn.text = posBtn
        mNegBtn.visibility = View.GONE
        // mPostBtn.alig

        //login button cick of custom layout
        mDialogView.okBtn.setOnClickListener {
            mAlertDialog.dismiss()
        }
    }

    private fun emptyFieldAlert(firstName: String?, lastName: String?, email: String?, birthday: String, phone: String?) {
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.above_18_alert, null)
        //AlertDialogBuilder
        val mBuilder = this.let { AlertDialog.Builder(this).setView(mDialogView) }
        //show dialog
        val mAlertDialog = mBuilder?.show()
        mAlertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val mTitle: TextView = mAlertDialog?.findViewById(R.id.required)!!
        val mInfo: TextView = mAlertDialog.findViewById(R.id.aboveEighteenText)!!
        val mPostBtn: TextView = mAlertDialog.findViewById(R.id.okBtn)!!
        val mNegBtn: TextView = mAlertDialog.findViewById(R.id.termsOfServiceBtn)!!

        mTitle.setTextColor(getSchoolColor()!!)

        mPostBtn.setTextColor(getSchoolColor()!!)
        mNegBtn.setTextColor(getSchoolColor()!!)

        mTitle.text = "Required"
        mInfo.text = when {
            firstName!!.isEmpty() -> "You need to enter your first name"
            lastName!!.isEmpty() -> "You need to enter your last name name"
            email!!.isEmpty() -> "You need to enter your email"
            birthday!!.isEmpty() -> "You need to enter your birthday"
            phone!!.isEmpty() -> "You need to enter your phone number"
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

    private fun errorLoadingAlert() {
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.above_18_alert, null)
        //AlertDialogBuilder
        val mBuilder = this.let { AlertDialog.Builder(this).setView(mDialogView) }
        //show dialog
        val mAlertDialog = mBuilder?.show()
        mAlertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val mTitle: TextView = mAlertDialog?.findViewById(R.id.required)!!
        val mInfo: TextView = mAlertDialog.findViewById(R.id.aboveEighteenText)!!
        val mPostBtn: TextView = mAlertDialog.findViewById(R.id.okBtn)!!
        val mNegBtn: TextView = mAlertDialog.findViewById(R.id.termsOfServiceBtn)!!

        mTitle.setTextColor(getSchoolColor()!!)

        mPostBtn.setTextColor(getSchoolColor()!!)
        mNegBtn.setTextColor(getSchoolColor()!!)

        mTitle.text = "Oops!"
        mInfo.text = "Something went wrong and we could not update your profile, please try again later"
        mPostBtn.text = "Ok"
        mNegBtn.visibility = View.GONE
        mDialogView.okBtn.setOnClickListener {
            mAlertDialog.dismiss()
        }
    }

    private fun displayUser(it: UserResponse) {
        mToken = it.login.token
        mUserId = it.id.toString()
        mUser = it
        selectedSchool = mUser!!.school

        editSignWithCodeField.setText(it.sign_up_code)
        editSignWithCodeField.isEnabled = false


        editUserName.setText(it.first_name)
        editLastName.setText(it.last_name)
        editEmailField.setText(it.email)
        mSchoolTv!!.text = it.school.name

        var numStr = it.phone

        if (numStr != null) {
            numStr = "(" + numStr.substring(0, 3) + ") " + numStr.substring(3,6) + "-" + numStr.substring(6, 10)
        }

        editPhoneNumberField.setText(numStr)

        editnickNameField.setText(it.nickname)
        Glide.with(this).load(mUser!!.avatar).circleCrop().error(R.drawable.ic_user_account_white).into(mProfilePicture!!)
        Log.e("JMG", "it.user.birthday: " + it.birthday)
        var spf = SimpleDateFormat("dd/MM/yyyy")
        val newBirthDate = spf.parse(it.birthday)
        Log.e("JMG", "newBirthDate: " + newBirthDate)
        spf = SimpleDateFormat("MM/dd/yyyy")
        var bdayToUse = spf.format(newBirthDate)
        Log.e("JMG", "bdayToUse: " + bdayToUse)

        birthDayField.setText(bdayToUse)

    }

    override fun onBackPressed() {
        super.onBackPressed()
//        val myIntent = Intent(this@EditProfile, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        //myIntent.putExtra("key", value); //Optional parameters
//        this@EditProfile.startActivity(myIntent)
//        this.finish();
    }


}
