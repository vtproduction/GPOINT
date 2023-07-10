package com.jedmahonisgroup.gamepoint.ui.auth.school

import androidx.lifecycle.MutableLiveData
import com.jedmahonisgroup.gamepoint.base.BaseViewModel
import com.jedmahonisgroup.gamepoint.model.school.School
import com.jedmahonisgroup.gamepoint.network.GamePointApi
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class SchoolViewModel : BaseViewModel() {
    private val TAG = SchoolViewModel::class.java.simpleName

    @set:Inject
    lateinit var gamePointApi: GamePointApi

    val loadingVisibility: MutableLiveData<Int> = MutableLiveData()

//    val successGetSchools: MutableLiveData<List<School>> = MutableLiveData()
//    val errorGetSchools: MutableLiveData<String> = MutableLiveData()
    val errorMessage: MutableLiveData<String> = MutableLiveData()

    private var subscription: Disposable? = null
}