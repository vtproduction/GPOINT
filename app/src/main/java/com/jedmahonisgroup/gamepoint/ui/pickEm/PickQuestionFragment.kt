package com.jedmahonisgroup.gamepoint.ui.pickEm

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import androidx.core.view.children
import com.google.android.material.checkbox.MaterialCheckBox
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.helpers.Extensions.getCheckedRadioButtonPosition
import com.jedmahonisgroup.gamepoint.model.gameshow.Question
import com.jedmahonisgroup.gamepoint.ui.BaseFragment
import kotlinx.android.synthetic.main.item_question_single_choice.*

/**
 * Created by nienle on 12,January,2023
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class PickQuestionFragment(private val position: Int, private val question: Question, private val callback: (Int, List<Boolean>) -> Unit) : BaseFragment() {

    //private lateinit var answers : MutableList<Boolean>



    companion object {
        fun newInstance(
            position: Int,
            question: Question,
            callback: (Int, List<Boolean>) -> Unit
        ): PickQuestionFragment {

            return PickQuestionFragment(position, question, callback)
        }
    }


    private fun getAnswersList() : List<Boolean> {
        val answers : MutableList<Boolean> = mutableListOf()
        if (question.isSingleChoice) {
            val selectedPos = radioGroup.getCheckedRadioButtonPosition()
            question.answers.forEachIndexed { index, _ ->
                answers.add(selectedPos == index)
            }
        }else{
            val checkboxes = containerCheckbox.children
            checkboxes.forEach { item ->
                if (item is MaterialCheckBox){
                    answers.add(item.isChecked)
                }
            }
        }

        return  answers
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.item_question_single_choice, null)

        return rootView
    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtQuestionNumber.text = "${position + 1}"
        txtQuestionTitle.text = question.title

        radioGroup.visibility = if (question.isSingleChoice) View.VISIBLE else View.GONE
        containerCheckbox.visibility = if (question.isSingleChoice) View.GONE else View.VISIBLE

        if (question.isSingleChoice){
            radioGroup.removeAllViews()

            question.answers.forEach {
                val btn = LayoutInflater.from(requireContext()).inflate(R.layout.custom_radio_button, null) as RadioButton
                btn.text = it.title
                btn.id = it.id
                radioGroup.addView(btn)

            }

            radioGroup.setOnCheckedChangeListener { _, _ ->  callback(position, getAnswersList())}


        }else{
            containerCheckbox.removeAllViews()
            question.answers.forEach {
                val btn = LayoutInflater.from(requireContext()).inflate(R.layout.custom_checkbox, null) as CheckBox
                btn.text = it.title
                containerCheckbox.addView(btn)
                btn.setOnCheckedChangeListener { _, _ -> callback(position, getAnswersList()) }
            }
        }
    }

}