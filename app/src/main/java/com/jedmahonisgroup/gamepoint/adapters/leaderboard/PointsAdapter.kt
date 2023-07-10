package com.jedmahonisgroup.gamepoint.adapters.leaderboard

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jedmahonisgroup.gamepoint.model.leaderboard.EventLeaderboardModel

class PointsAdapter(private val response: List<EventLeaderboardModel>, private val schoolColor: String?) : androidx.recyclerview.widget.RecyclerView.Adapter<PointsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PointsViewHolder(inflater, parent, schoolColor)
    }

    override fun getItemCount(): Int {
        Log.e("JMG", "response.size: " + response.size)
        return response.size
    }

    override fun onBindViewHolder(holder: PointsViewHolder, position: Int) {
       if (position >= 0) {
           val data: EventLeaderboardModel = response[position]
           val num = data.rank
           holder.bind(data, num, response)


//           if (data.position == 0){
//               val num = position +1
//               holder.bind(data, num, response)
//           }else{
//               val num = data.position +1
//               holder.bind(data, num, response)
//           }

           /*if (position % 2 == 0) {
//               holder.view?.setBackgroundColor(Color.parseColor("#FAFAFA"))
           }else{
               //odd nums
               if (!data.current_user){
                   holder.view?.setBackgroundColor(Color.parseColor("#e3e4e8"))

               }
           }*/
           /*val indexOfFirstNum = response.indexOfFirst {it.position+1 > 9}


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

           }*/
       }




//        if (position % 2 == 0) {
//        }else{
//            //odd numbers
//           // holder.view?.setBackgroundColor(Color.parseColor("#e3e4e8"))
//
//        }
//
//        if (data.current_user){
//            holder.view?.setBackgroundColor(Color.parseColor("#7a0019"))
//            holder.userName?.setTextColor(Color.WHITE)
//
//        }


    }

}