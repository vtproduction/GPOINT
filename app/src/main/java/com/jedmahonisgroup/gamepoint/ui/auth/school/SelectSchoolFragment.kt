package com.jedmahonisgroup.gamepoint.ui.auth.school

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.databinding.FragmentSelectSchoolBinding
import com.jedmahonisgroup.gamepoint.model.UserModel
import com.jedmahonisgroup.gamepoint.model.school.School
import com.jedmahonisgroup.gamepoint.ui.auth.signup.SignUpFragment
import kotlinx.android.synthetic.main.fragment_select_school.*


class SelectSchoolFragment :  androidx.fragment.app.Fragment(), SelectSchoolCallBack {

    private lateinit var adapter: SelectSchoolAdapter
    private lateinit var schools: List<School>
    private lateinit var selectSchoolViewModel: SelectSchoolViewModel
    private lateinit var binding: FragmentSelectSchoolBinding
    private var containerId: Int = 0
    private lateinit var signupInfo: UserModel
    private var fromEdit: Boolean = false
    private var mToolbar: MaterialToolbar? = null


    fun setSchools(schoolList: List<School?>?, userModel: UserModel, fromEdit2: Boolean) {
        schools = schoolList as List<School>
        signupInfo = userModel
        fromEdit = fromEdit2
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


//        selectSchoolViewModel = ViewModelProviders.of(this, ViewModelFactory(this.activity as AppCompatActivity)).get(SelectSchoolViewModel::class.java)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_school, container, false)
        val rootView = binding.root

        containerId = container!!.id

//        binding.viewModel = selectSchoolViewModel

//
        toolbar(rootView)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        getSchools()
        initializeRecyclerView()

    }

    private fun toolbar(mainView: View) {
        mToolbar = mainView.findViewById(R.id.toolbar)

        val activity = (requireActivity() as AppCompatActivity)

        activity.setSupportActionBar(mToolbar)

        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar?.setDisplayShowHomeEnabled(true)
        activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.icon_feather_chevron_left_orange)

        mToolbar!!.setNavigationOnClickListener {
            //on click
            activity.onBackPressed()

        }
    }



    private fun getSchools(){
//        selectSchoolViewModel.getSchools()
//        getSchoolsResult()
    }

    @SuppressLint("ResourceType")
    private fun getSchoolsResult() {
        selectSchoolViewModel.loadingVisibility.observe(this, Observer { type ->
            //     mSignUpParent?.visibility = View.GONE
            //     mSignUpPending?.visibility = View.VISIBLE
        })
        selectSchoolViewModel.errorGetSchools.observe(this, Observer { errorMessage ->
            //    mSignUpParent?.visibility = View.VISIBLE
            //   mSignUpPending?.visibility = View.GONE
            //  if (errorMessage != null) showError(errorMessage)
        })

        selectSchoolViewModel.succesGetSchools.observe(this, Observer { successMessage ->
            //       mSignUpParent?.visibility = View.VISIBLE
            //        mSignUpPending?.visibility = View.GONE
            if (successMessage != null) {
                Log.e("schools", schools.toString())
                schools = successMessage
                initializeRecyclerView()
            }
        })
        }

    private fun initializeRecyclerView() {
        adapter = SelectSchoolAdapter(this)
        selectSchoolRv.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        selectSchoolRv.adapter = adapter
        adapter.changeSchoolList(schools)
    }

    lateinit var sendSchoolBack: SendSchoolBack
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            sendSchoolBack = context as SendSchoolBack
        } catch (e: Exception) {

        }
    }

    interface SendSchoolBack {
        fun onSchoolSelected(school: School)
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onResume() {
        super.onResume()
    }


    override fun onSchoolSelected(school: School) {
        Log.e("schoolSelected", school.name)
        if (fromEdit) {
            sendSchoolBack.onSchoolSelected(school)
            activity?.onBackPressed()

        } else {
            val frag = SignUpFragment()
            frag.setUserModel(signupInfo, school)
            val transaction = activity?.supportFragmentManager!!.beginTransaction()
            transaction.replace(containerId, frag)
//            transaction.disallowAddToBackStack()
            transaction.commit()
        }

    }

    override fun onEmptyData() {
    }

    override fun onNonEmptyData() {
    }


}