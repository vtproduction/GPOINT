package com.jedmahonisgroup.gamepoint.ui.createpost

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker
import com.google.gson.Gson
import com.jedmahonisgroup.gamepoint.GamePointApplication
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.adapters.createpostgallery.CreatePostGalleryAdapter
import com.jedmahonisgroup.gamepoint.model.CreatPostUi
import com.jedmahonisgroup.gamepoint.model.UserResponse
import com.jedmahonisgroup.gamepoint.model.UserResponseModel
import com.jedmahonisgroup.gamepoint.utils.onItemClick


class FeedsCreatPostFragment : Fragment() {

    private var token: String? = null
    private var mFeedsCreatePostBackIV: Button? = null
    private var mFeedsCreatePostPhoto: Button? = null

    private var imageSelected: Boolean? = false

    var Bundlein: Bundle? = null


    private var mFeedsCreatePostNextIV: TextView? = null
    private var mFeedsCreatePostBigImage: ImageView? = null
    private var Lastpos = 0

    private var TOKEN: String? = null
    private var mUser: UserResponse? = null

    private var recyclerView: RecyclerView? = null
    private var gridLayout: GridLayoutManager? = null

    var imagePathList: ArrayList<CreatPostUi> = arrayListOf()

    var Path: String? = null

    var NoImage: RelativeLayout? = null


    private var binding: ViewDataBinding? = null


    companion object {
        private const val PROFILE_IMAGE_REQ_CODE = 101
    }

    val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            imagePathList = getImagePathFromStorage()

            recyclerView?.adapter = CreatePostGalleryAdapter(imagePathList, context)
            recyclerView?.adapter?.notifyDataSetChanged()

            val file = ImagePicker.getFilePath(it.data)
            Path = file
            Log.e("TAG", "Path:${file}")
            Glide.with(this)
                .load(file).placeholder(R.drawable.ic_photo_black_48dp).error(R.drawable.no_access_to_photos).into(mFeedsCreatePostBigImage!!)


            if (imagePathList.isNullOrEmpty()){
                imagePathList.add(CreatPostUi(path = Path , selected = true))
                recyclerView?.visibility = View.VISIBLE
                NoImage?.visibility = View.INVISIBLE
                recyclerView?.adapter?.notifyItemInserted(0)
                recyclerView?.adapter?.notifyDataSetChanged()
                Lastpos == imagePathList.indexOf(CreatPostUi(path = Path , selected = true))
                imageSelected = true

            }else{

                recyclerView?.visibility = View.VISIBLE
                NoImage?.visibility = View.INVISIBLE
                Lastpos = imagePathList.size


                for (i in 0 until imagePathList.size ){
                    if (imagePathList[i].selected){
                        imagePathList[i].selected = false
                    }
                }


                imagePathList.add(CreatPostUi(path = Path , selected = true))



                recyclerView?.adapter?.notifyItemInserted(imagePathList.indexOf(CreatPostUi(path = Path , selected = true)))
                recyclerView?.adapter?.notifyDataSetChanged()
                Lastpos == imagePathList.indexOf(CreatPostUi(path = Path , selected = true))
                imageSelected = true

            }


        } else if (it.resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(context, ImagePicker.getError(it.data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_feeds_creat_post_fragment, container, false)
        val rootView = binding?.root

        getToken()

        Bundlein = savedInstanceState

        NoImage = rootView?.findViewById(R.id.imageNoData)!!
        recyclerView = rootView?.findViewById(R.id.RCVGLIP)


        if (checkSelfPermission(rootView!!.context, Manifest.permission.READ_EXTERNAL_STORAGE ) ==
                PackageManager.PERMISSION_GRANTED) {
            imagePathList = getImagePathFromStorage()

            recyclerView?.visibility = View.VISIBLE
            NoImage?.visibility = View.INVISIBLE


        } else {

            requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    0)
            if (checkSelfPermission(rootView!!.context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {



                recyclerView?.visibility = View.VISIBLE
                NoImage?.visibility = View.INVISIBLE



            } else {



            }


        }



        setUpUi(rootView)

        return rootView

    }

    private fun setuprecycleview() {

        recyclerView?.visibility = View.VISIBLE
        NoImage?.visibility = View.INVISIBLE

        Log.e("TAHG", imagePathList.size.toString())

        recyclerView?.adapter = CreatePostGalleryAdapter(imagePathList, context)
        recyclerView?.adapter?.notifyDataSetChanged()


    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            0 -> {

                val granted = grantResults.isNotEmpty()
                        && permissions.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && !ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permissions[0])

                when (granted) {
                    true ->   setuprecycleview()
                    else -> Unit
                }
            }
        }
    }





    @SuppressLint("WrongConstant")
    private fun setUpUi(rootView: View?) {


        mFeedsCreatePostBackIV = rootView?.findViewById(R.id.feedsCreatPostBackBtn)
        mFeedsCreatePostNextIV = rootView?.findViewById(R.id.feedsCreatPostNextBtn)
        mFeedsCreatePostPhoto = rootView?.findViewById(R.id.feedsCreatPostPhotoBtn)


        mFeedsCreatePostPhoto?.setOnClickListener {

            ImagePicker.with(requireActivity())
                    .cameraOnly()
                    .cropSquare() // Crop Square image(Optional)
                    .maxResultSize(620, 620) // Final image resolution will be less than 620 x 620(Optional)
                    .createIntentFromDialog { launcher.launch(it) }

        }






        mFeedsCreatePostBackIV?.setOnClickListener {
            getFragmentManager()?.popBackStack("1", 1);
        }

        mFeedsCreatePostNextIV?.setOnClickListener {
            if (Path == null) {

                if (!imagePathList.isEmpty()) {

                    if ((Lastpos == 0)) {
                        Path = imagePathList[0].path
                    } else {
                        Path = imagePathList[Lastpos].path

                    }
                    if(imageSelected!!){
                        changeFragments(FeedsCreatPostFragment2(), Path!!)
                    }else{
                        Toast.makeText(rootView!!.context,"Choose Picture",3).show()

                    }
                } else {
                    recyclerView?.visibility = View.INVISIBLE
                    NoImage?.visibility = View.VISIBLE
                    Toast.makeText(rootView!!.context,"No Picture",3).show()

                }
            } else {

                if(imageSelected!!){
                    changeFragments(FeedsCreatPostFragment2(), Path!!)
                }else{
                    Toast.makeText(rootView!!.context,"Choose Picture",3).show()

                }




            }
        }



        mFeedsCreatePostBigImage = rootView?.findViewById(R.id.imageView)

        mFeedsCreatePostBigImage?.setOnClickListener {

        }

        gridLayout = GridLayoutManager(context, 4, LinearLayoutManager.VERTICAL, false)
        recyclerView?.layoutManager = gridLayout
        recyclerView?.setHasFixedSize(true)




        recyclerView?.adapter = CreatePostGalleryAdapter(imagePathList, context)
        recyclerView?.adapter?.notifyDataSetChanged()



        recyclerView?.onItemClick { recyclerView, position, v ->

            if (Lastpos == position){
                imageSelected = !imageSelected!!

            }else{
                imageSelected = true

            }


            imagePathList[Lastpos].selected = false
            Glide.with(requireContext())
                    .load(imagePathList[position].path)
                    .error(R.drawable.no_access_to_photos)
                    .into(mFeedsCreatePostBigImage!!)
            imagePathList[position].selected = true
            recyclerView?.adapter?.notifyDataSetChanged()
            Lastpos = position
            Log.e("chooosed" , Lastpos.toString())

            // do it

        }


    }

    private fun changeFragments(fragment: Fragment, path: String?) {
        Log.i(TAG, "Token ========>" + TOKEN.toString())




        val bundle = Bundle()
        bundle.putString("token", TOKEN.toString())
        bundle.putString("path", path)


        // set Fragmentclass Arguments
        val fragobj = fragment
        fragobj.arguments = bundle



        requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.feedsDrawerLayout, fragobj, "2").addToBackStack("2").commitAllowingStateLoss()


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK) {
            // File object will not be null for RESULT_OK

            if (requestCode == PROFILE_IMAGE_REQ_CODE) {
                imagePathList = getImagePathFromStorage()

                recyclerView?.adapter = CreatePostGalleryAdapter(imagePathList, context)
                recyclerView?.adapter?.notifyDataSetChanged()

                val file = ImagePicker.getFilePath(data)
                Path = file
                Log.e("TAG", "Path:${file}")
                Glide.with(this)
                        .load(file).placeholder(R.drawable.ic_photo_black_48dp).error(R.drawable.no_access_to_photos).into(mFeedsCreatePostBigImage!!)
            }

            if (imagePathList.isNullOrEmpty()){
                imagePathList.add(CreatPostUi(path = Path , selected = true))
                recyclerView?.visibility = View.VISIBLE
                NoImage?.visibility = View.INVISIBLE
                recyclerView?.adapter?.notifyItemInserted(0)
                recyclerView?.adapter?.notifyDataSetChanged()
                Lastpos == imagePathList.indexOf(CreatPostUi(path = Path , selected = true))
                imageSelected = true

            }else{

                recyclerView?.visibility = View.VISIBLE
                NoImage?.visibility = View.INVISIBLE
                Lastpos = imagePathList.size


                 for (i in 0 until imagePathList.size ){
                     if (imagePathList[i].selected){
                         imagePathList[i].selected = false
                     }
                 }


                imagePathList.add(CreatPostUi(path = Path , selected = true))



                recyclerView?.adapter?.notifyItemInserted(imagePathList.indexOf(CreatPostUi(path = Path , selected = true)))
                recyclerView?.adapter?.notifyDataSetChanged()
                Lastpos == imagePathList.indexOf(CreatPostUi(path = Path , selected = true))
                imageSelected = true

            }


        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }







    }


    private fun getToken() {
        token = requireArguments().getString("token")
//        Log.e(TAG + "Token", token.toString())

        TOKEN = token

        mUser = GamePointApplication.shared!!.getCurrentUser(requireContext())
    }

    private fun getImagePathFromStorage(): ArrayList<CreatPostUi> {

        var ImagePathList: ArrayList<CreatPostUi> = arrayListOf()

        val col: Array<String> = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID)
        val orderBy = MediaStore.Images.Media.DATE_TAKEN;

        var imagecursor: Cursor? = activity?.managedQuery(
                MediaStore.Images.Media.getContentUri("external"), col, null,
                null, "$orderBy DESC")



        for (i in 0 until imagecursor!!.count) {
            imagecursor!!.moveToPosition(i)
            val dataColumnIndex = imagecursor!!.getColumnIndex(MediaStore.Images.Media.DATA) //get column index
            ImagePathList.add(CreatPostUi(imagecursor!!.getString(dataColumnIndex)))
        }



        return ImagePathList!!
    }





}

