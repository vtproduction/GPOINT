package com.jedmahonisgroup.gamepoint.ui

import android.annotation.SuppressLint
import android.app.Activity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import com.github.drjacky.imagepicker.ImagePicker
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.injection.ViewModelFactory
import com.jedmahonisgroup.gamepoint.model.UserImage
import com.jedmahonisgroup.gamepoint.model.UserPhoto
import com.jedmahonisgroup.gamepoint.model.UserResponse
import com.jedmahonisgroup.gamepoint.model.UserResponseModel
import com.jedmahonisgroup.gamepoint.utils.setLocalImage
import kotlinx.android.synthetic.main.content_profile.imgProfile
import kotlinx.android.synthetic.main.event_expired_error.view.*
import kotlinx.android.synthetic.main.upload_picture_activity.*
import java.io.ByteArrayOutputStream
import java.io.File


class UploadPictureActivity : AppCompatActivity() {
    private val TAG = UploadPictureActivity::class.java.simpleName
    private var uploadPictureViewModel: UploadPictureViewModel? = null
    private lateinit var binding: com.jedmahonisgroup.gamepoint.databinding.UploadPictureActivityBinding

    private var mUserResponseModel: UserResponseModel? = null
    private var mToken: String? = null
    private var mUserId: String? = null

    companion object {
        private const val PROFILE_IMAGE_REQ_CODE = 101
    }

    @SuppressLint("SuspiciousIndentation")
    val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val uri = it.data?.data!!
            // Use the uri to load the image
//            if (requestCo == PROFILE_IMAGE_REQ_CODE) {
                setUpUi(uri!!.toFile())
                Log.i(TAG, "display user image")
                imgProfile.setLocalImage(uri.toFile(), true)
//            }

        } else if (it.resultCode == ImagePicker.RESULT_ERROR) {

            Toast.makeText(this, ImagePicker.getError(it.data!!), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.upload_picture_activity)

        uploadPictureViewModel = ViewModelProviders.of(this, ViewModelFactory(this)).get(UploadPictureViewModel::class.java)

        val mUser = GamePointApplication.shared!!.getCurrentUser(baseContext)

        Log.i(TAG, "===========> user is: $mUser")
        mToken = mUser!!.login.token
        mUserId = mUser!!.id.toString()

        addProfilePics.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                    .cropSquare() // Crop Square image(Optional)

                    .maxResultSize(620, 620) // Final image resolution will be less than 620 x 620(Optional)
                    .createIntentFromDialog { launcher.launch(it) } //PROFILE_IMAGE_REQ_CODE
        }

        skip.setOnClickListener {
            launchActivity()
        }



        binding.uploadPictureViewModel = uploadPictureViewModel

        //   imgProfile.setRemoteImage(DEFAULT_IMAGE_URL, true)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            // File object will not be null for RESULT_OK
            val file = data!!.data!!.toFile()

            Log.e("TAG", "Path:${file?.absolutePath}")
            if (requestCode == PROFILE_IMAGE_REQ_CODE) {
                setUpUi(file!!)
                Log.i(TAG, "display user image")
                imgProfile.setLocalImage(file, true)


            }

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            imgProfile.visibility = View.GONE
            imgProfilePlaceHolder.visibility = View.VISIBLE
            addProfilePics.visibility = View.VISIBLE
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
            imgProfile.visibility = View.GONE
            imgProfilePlaceHolder.visibility = View.VISIBLE
            addProfilePics.visibility = View.VISIBLE
        }
    }

    private fun uploadImage(image: String) {
        Log.i(TAG, "Base 64 String is: data:image/png;base64,$image")


        val userImage = UserImage(
                user = UserPhoto(
                        avatar = "data:image/png;base64,$image"
                )
        )

        val gson = Gson()
        val jsonRequest = gson.toJson(userImage)
        Log.i(TAG, "Base 64 Request is: $jsonRequest")


        uploadPictureViewModel?.uploadPicture(mToken.toString(), mUserId.toString(), userImage)
        uploadImageResult()
    }

    private fun uploadImageResult() {
        uploadPictureViewModel?.uploadedImageSuccessfully?.observe(this, Observer {
            //save response to disk
            // mUserResponseModel = it
            Log.i(TAG, "uploadImageResult ============> resultsFromServer: $it")
            launchActivityWithExtra(it?.avatar.toString())
            // launchActivity()

            uploadPictureViewModel!!.getUserFromDatabase()
            databaseWork(it)
        })

        uploadPictureViewModel?.errorUploadingImage?.observe(this, Observer {
            Snackbar.make(binding.root, "Unable to upload image, try again later", Snackbar.LENGTH_LONG).show()
            Log.e(TAG, "uploadImageResult ============> error: $it")
            //alert
            var error = "We were unable to upload your picture, please try again later."
            var title = "Something went wrong"
            alertUser(error, title)

        })
    }

    private fun databaseWork(recentUser: UserResponse?) {
        uploadPictureViewModel!!.getUserFromDbSuccess.observe(this, Observer { dbUser ->
            //this is the database stuff
            val newUserResponse = UserResponseModel(
                    user = UserResponse(
                            id = recentUser!!.id,
                            first_name = recentUser.first_name,
                            last_name = recentUser.last_name,
                            email = recentUser.email,
                            avatar = recentUser.avatar,
                            birthday = recentUser.birthday,
                            phone = recentUser.phone,
                            sign_with_code = recentUser.sign_with_code,
                            total_points = recentUser.total_points,
                            current_streak = recentUser.current_streak,
                            pick_rank = recentUser.pick_rank,
                            highest_streak = recentUser.highest_streak,
                            this_month_event_points = recentUser.this_month_event_points,
                            event_rank = recentUser.event_rank,
                            redeemable = recentUser.redeemable,
                            user_picks = recentUser.user_picks,
                            check_ins = recentUser.check_ins,
                            url = recentUser.url,
                            nickname = recentUser.nickname,
                            login = dbUser!!.user.login,
                            private_posts = recentUser.private_posts,
                            sign_up_code = recentUser.sign_up_code,
                            friend_request = recentUser.friend_request,
                            streaks_prize = recentUser.streaks_prize,
                            points_prize = recentUser.points_prize,
                            school = recentUser.school
                    )

            )

            uploadPictureViewModel!!.saveUserTodabase(newUserResponse)
        })

        uploadPictureViewModel!!.getUserFromDbError.observe(this, Observer {
            Log.e(TAG, "there was a problem when getting user form database")
            Snackbar.make(this.binding.root,"error fetching user form db", Snackbar.LENGTH_SHORT).show()

        })

        uploadPictureViewModel!!.writeUserToDbSucess.observe(this, Observer {
            Log.i(TAG, "successfully saved new user to db")
        })

        uploadPictureViewModel!!.writeUserToDbError.observe(this, Observer {
            Log.e(TAG, "could not save user to db...something went wrong")
            Snackbar.make(this.binding.root,"error when saving profile pic", Snackbar.LENGTH_SHORT).show()
        })
    }

    private fun setUpUi(file: File) {
        userPicReason.text = ""
        // skip.visibility = View.V

        retry.visibility = View.VISIBLE
        looksGood.visibility = View.VISIBLE

        imgProfile.visibility = View.VISIBLE
        imgProfilePlaceHolder.visibility = View.GONE
        addProfilePics.visibility = View.GONE

        addProfilePics.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .cropSquare() // Crop Square image(Optional)

                .maxResultSize(620, 620) // Final image resolution will be less than 620 x 620(Optional)
                .createIntentFromDialog { launcher.launch(it) } //PROFI
        }
        looksGood.setOnClickListener {
            //upload data, on resultsFromServer, launch activity
            //toBase64(path!!)
            // uploadImage(encoder(saveBitmapToFile(file)?.path!!))
            uploadImage(convertToBase64(file.path))

        }

        //on retry, upload new picture
        retry.setOnClickListener {
            ImagePicker.with(this)
                    .cropSquare() // Crop Square image(Optional)
                    .maxResultSize(620, 620) // Final image resolution will be less than 620 x 620(Optional)
                    .createIntentFromDialog { launcher.launch(it) }
        }

        //start main activity on skip
        skip.setOnClickListener {
            launchActivity()
        }

    }

    private fun launchActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    private fun launchActivityWithExtra(url: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("profileUrl", url)
        startActivity(intent)
        finish()
    }

    private fun convertToBase64(imagePath: String): String {

        val bm = BitmapFactory.decodeFile(imagePath)

        val baos = ByteArrayOutputStream()

        bm.compress(Bitmap.CompressFormat.JPEG, 50, baos)

        val byteArrayImage = baos.toByteArray()

        return Base64.encodeToString(byteArrayImage, Base64.DEFAULT)

    }




    private fun alertUser(msg: String, title: String) {
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.event_expired_error, null)
        //AlertDialogBuilder
        val mBuilder = this.let { it1 ->
            AlertDialog.Builder(it1)
                    .setView(mDialogView)
        }
        val mInfo = mDialogView.findViewById<TextView>(R.id.eventDetailAlertInfo)
        val mTitle = mDialogView.findViewById<TextView>(R.id.checkedIn)
        mInfo.text = msg
        mTitle.text = title
        //show dialog
        val mAlertDialog = mBuilder?.show()
        mAlertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        //login button cick of custom layout
        mDialogView.close.setOnClickListener {
            mAlertDialog?.dismiss()

        }
    }

}
