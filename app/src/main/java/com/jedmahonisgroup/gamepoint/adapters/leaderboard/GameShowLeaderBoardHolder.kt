package com.jedmahonisgroup.gamepoint.adapters.leaderboard

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.jedmahonisgroup.gamepoint.BuildConfig
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.gameshowLeaderboard.GameShowLeaderboardUser
import com.jedmahonisgroup.gamepoint.model.leaderboard.EventLeaderboardModel
import com.jedmahonisgroup.gamepoint.ui.leaderboard.GameShowLeaderBoardViewModel
import com.jedmahonisgroup.gamepoint.utils.LogUtil
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text

import java.text.DecimalFormat


class GameShowLeaderBoardHolder(inflater: LayoutInflater, private val parent: ViewGroup):
        androidx.recyclerview.widget.RecyclerView.ViewHolder(inflater.inflate(R.layout.item_game_show_leader_board, parent, false)){

    private var imgAva: CircleImageView
    private var txtName: TextView
    private var txtPoint: TextView
    private var view: View

    init {
        imgAva = itemView.findViewById(R.id.imgAva)
        txtName = itemView.findViewById(R.id.txtName)
        txtPoint = itemView.findViewById(R.id.txtPoint)
        view = itemView


    }

    fun bind(data: GameShowLeaderboardUser) {
        try{
            txtName.text = data.name
            txtPoint.text = data.points.toString()
            Picasso.get().load(data.avatar).placeholder(R.drawable.icon_feather_meh)
                .error(R.drawable.icon_feather_meh)
                .into(imgAva)
        }catch(t: Throwable){
            LogUtil.e("GameShowLeaderBoardHolder > bind > 44: ${t.localizedMessage}")
        }


    }


}