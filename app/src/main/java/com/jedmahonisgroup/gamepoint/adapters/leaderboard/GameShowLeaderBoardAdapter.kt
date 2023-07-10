package com.jedmahonisgroup.gamepoint.adapters.leaderboard

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jedmahonisgroup.gamepoint.model.gameshowLeaderboard.GameShowLeaderboardUser
import com.jedmahonisgroup.gamepoint.model.leaderboard.EventLeaderboardModel
import com.jedmahonisgroup.gamepoint.utils.LogUtil

class GameShowLeaderBoardAdapter(private val response: List<GameShowLeaderboardUser>,) : RecyclerView.Adapter<GameShowLeaderBoardHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameShowLeaderBoardHolder {
        val inflater = LayoutInflater.from(parent.context)
        return GameShowLeaderBoardHolder(inflater, parent)
    }

    override fun getItemCount(): Int {
        LogUtil.d("GameShowLeaderBoardAdapter > getItemCount > 19: ${response.size}")
        return response.size
    }

    override fun onBindViewHolder(holder: GameShowLeaderBoardHolder, position: Int) {
       holder.bind(response[position])
    }

}