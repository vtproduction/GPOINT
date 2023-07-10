package com.jedmahonisgroup.gamepoint.ui.pickEm

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.jedmahonisgroup.gamepoint.model.gameshow.GameShow
import com.jedmahonisgroup.gamepoint.model.gameshow.Question

/**
 * Created by nienle on 12,January,2023
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class ViewPagerAdapter(fragmentActivity: FragmentActivity, val data: GameShow, val callback: (Int, List<Boolean>) -> Unit, val breakerCallback: (Int) -> Unit) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = data.questions.size + 1

    override fun createFragment(position: Int): Fragment {
        return if (position == itemCount - 1) PickBreakerQuestionFragment.newInstance(position, data, breakerCallback)
        else PickQuestionFragment(position, data.questions[position], callback)
    }

}