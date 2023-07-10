package com.jedmahonisgroup.gamepoint.utils

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.databinding.BindingAdapter
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.jedmahonisgroup.gamepoint.utils.extension.getParentActivity


@BindingAdapter("mutableVisibility")
fun setMutableVisibility(view: View,  visibility: MutableLiveData<Int>?) {
    val parentActivity:AppCompatActivity? = view.getParentActivity()
    if(parentActivity != null && visibility != null) {
        visibility.observe(parentActivity, Observer { value -> view.visibility = value?:View.VISIBLE})
    }
}