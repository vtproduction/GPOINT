package com.jedmahonisgroup.gamepoint.helpers

import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.view.children

/**
 * Created by nienle on 17,January,2023
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
object Extensions {

    fun RadioGroup.getCheckedRadioButtonPosition() : Int {
        val radioButtonId = checkedRadioButtonId
        return children.filter { it is RadioButton }
            .mapIndexed { index, view ->   index to view }
            .firstOrNull { it.second.id == radioButtonId }?.first ?: -1
    }


    fun View.hide(){
        visibility = View.GONE
    }

    fun View.show(){
        visibility = View.VISIBLE
    }
}