package com.jedmahonisgroup.gamepoint.adapters.gameShow

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.gameshow.GameShow
import com.jedmahonisgroup.gamepoint.utils.LogUtil

/**
 * Created by nienle on 05,February,2023
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class GameShowAdapter(private val data: List<GameShow>, private val callback: (item: GameShow) -> Unit) : RecyclerView.Adapter<GameShowAdapter.ViewHolder>(){


    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_gameshow_list_item, parent, false)
        return ViewHolder(itemView, callback)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position], position)
    }



    class ViewHolder(itemView: View, private val callback: (item: GameShow) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private var txtPos: TextView = itemView.findViewById(R.id.txtIndex)
        private var txtName: TextView = itemView.findViewById(R.id.txtGameName)
        private var txtStatus1: TextView = itemView.findViewById(R.id.txtStatus)
        private var txtStatus2: TextView = itemView.findViewById(R.id.txtStatus2)


        @SuppressLint("SetTextI18n")
        fun bind(data: GameShow, pos: Int ){
            try{
                itemView.setOnClickListener {
                    callback(data)
                }

                txtPos.text = (pos + 1).toString()
                txtName.text = data.name
                txtStatus1.text = data.gameShowStatus.toString()
                when(data.gameShowStatus){
                    GameShow.Status.OPEN -> {
                        txtStatus2.text = "Enter Quiz"
                        txtStatus1.setTextColor(Color.parseColor("#62bb3b"))
                    }
                    GameShow.Status.CLOSE -> {
                        txtStatus2.text = "Quiz Complete"
                        txtStatus1.setTextColor(Color.parseColor("#A31621"))
                    }
                    GameShow.Status.JOINED -> {
                        txtStatus2.text = "Enter Quiz"
                        txtStatus1.setTextColor(Color.parseColor("#62bb3b"))
                    }
                    GameShow.Status.END -> {
                        txtStatus2.text = "Quiz Complete"
                        txtStatus1.setTextColor(Color.parseColor("#A31621"))
                    }
                    else -> {

                    }
                }

            }catch(t: Throwable){
                LogUtil.e("ViewHolder > bind > 33: ${t.localizedMessage}")
            }
        }
    }
}