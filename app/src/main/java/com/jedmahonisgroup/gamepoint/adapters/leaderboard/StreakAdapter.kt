package com.jedmahonisgroup.gamepoint.adapters.leaderboard

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jedmahonisgroup.gamepoint.model.leaderboard.StreakModel

class StreakAdapter(private val response: List<StreakModel>,private val schoolColor: String?) : androidx.recyclerview.widget.RecyclerView.Adapter<StreakViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StreakViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return StreakViewHolder(inflater, parent, schoolColor)
    }

    override fun getItemCount(): Int {
        return response.size
    }

    override fun onBindViewHolder(holder: StreakViewHolder, position: Int) {
        if (position >= 0) {
            val data: StreakModel = response[position]
            val num = data.rank
            holder.bind(data, num, response)



            if (position % 2 == 0) {
            }else{
                //odd nums
                if (!data.current_user){
                    holder.view?.setBackgroundColor(Color.parseColor("#e3e4e8"))

                }
            }

            val indexOfFirstNum = response.indexOfFirst {it.position+1 > 9}

            if (data.position+1 >= 9){
                if (data.position+1 == response[indexOfFirstNum].position+1){
                    holder.view?.setBackgroundColor(Color.parseColor("#ffffff"))
                    holder.separator1?.visibility = View.VISIBLE
                    holder.separator2?.visibility = View.VISIBLE
                    holder.separator3?.visibility = View.VISIBLE
                }
            }


            if (data.current_user){
                holder.leaderboardPView?.setBackgroundColor(Color.parseColor(schoolColor))
                holder.userName?.setTextColor(Color.WHITE)
                holder.rowNumber?.setTextColor(Color.WHITE)

            }
        }
    }

}