package com.jedmahonisgroup.gamepoint.ui.auth.school

import com.jedmahonisgroup.gamepoint.model.school.School

interface SelectSchoolCallBack {
    fun onSchoolSelected(school: School)
    fun onEmptyData()
    fun onNonEmptyData()
}