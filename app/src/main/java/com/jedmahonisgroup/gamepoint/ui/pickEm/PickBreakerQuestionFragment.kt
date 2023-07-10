package com.jedmahonisgroup.gamepoint.ui.pickEm

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.gameshow.GameShow
import com.jedmahonisgroup.gamepoint.ui.BaseFragment
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import kotlinx.android.synthetic.main.fragment_pick_em.*
import kotlinx.android.synthetic.main.item_question_multi_choice.*
import kotlinx.android.synthetic.main.item_question_multi_choice.seekBar


/**
 * Created by nienle on 12,January,2023
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class PickBreakerQuestionFragment(private val position: Int, private val gameShow: GameShow, private val callback: (Int) -> Unit) : BaseFragment() {


    companion object {
        fun newInstance(position: Int, gameShow: GameShow, callback: (Int) -> Unit) : PickBreakerQuestionFragment {
            return  PickBreakerQuestionFragment(position,gameShow, callback)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.item_question_multi_choice, null)

        return rootView
    }

    fun getBreakerValue() = seekBar.progress


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtQuestionNumber.text = "${position + 1}"
        txtQuestionTitle.text = gameShow.gameBreakerTitle


        seekBar.min = gameShow.gameBreakerFrom.toFloat()
        seekBar.max = gameShow.gameBreakerTo.toFloat()

        txtBreakerMin.text = "${gameShow.gameBreakerFrom}"
        txtBreakerMax.text = "${gameShow.gameBreakerTo}"


        seekBar.onSeekChangeListener = object : OnSeekChangeListener {

            override fun onSeeking(seekParams: SeekParams) {
            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {}
            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {
                callback(seekBar.progress)
            }
        }


    }

}