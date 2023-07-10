package com.jedmahonisgroup.gamepoint.ui.auth.school

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.school.School
import kotlinx.android.synthetic.main.item_select_school.view.*

class SelectSchoolAdapter(val selectSchoolCallBack: SelectSchoolCallBack) :
        RecyclerView.Adapter<SelectSchoolAdapter.SelectSchoolViewHolder>() {

    var schoolList: List<School> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectSchoolViewHolder {
        return SelectSchoolViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.item_select_school,
                        parent,
                        false
                )
        )
    }

    override fun getItemCount() = schoolList.size

    override fun onBindViewHolder(holder: SelectSchoolViewHolder, position: Int) {
        holder.bindLibraryToView(schoolList[position])
    }

    fun changeSchoolList(businessList: List<School>) {
        schoolList = businessList
        if (schoolList.isEmpty()) selectSchoolCallBack.onEmptyData()
        else {
            selectSchoolCallBack.onNonEmptyData()
            notifyDataSetChanged()
        }
    }

    inner class SelectSchoolViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindLibraryToView(school: School) {


            itemView.schoolName.text = school.name
            Glide.with(itemView).load(school.logo)
                    .placeholder(R.drawable.ic_user_account)
                    .into(itemView.school_logo)

            itemView.setOnClickListener {
                selectSchoolCallBack.onSchoolSelected(school)
            }


        }
    }
}