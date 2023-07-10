package com.jedmahonisgroup.gamepoint.adapters.gameShow

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.gameshow.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by nienle on 06,February,2023
 * Midsummer.
 * Ping me at nienbkict@gmail.com
 * Happy coding ^_^
 */
class GameShowResultAdapter(private val data: GameShowResult) : RecyclerView.Adapter<ViewHolder>() {



    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtQuestionName : TextView = itemView.findViewById(R.id.txtQuestionName)

        private val imgPlayer1Ava :  CircleImageView = itemView.findViewById(R.id.player1Ava)
        private val imgPlayer2Ava :  CircleImageView = itemView.findViewById(R.id.player2Ava)

        private val txtPlayer1AnswerText :  TextView = itemView.findViewById(R.id.player1AnswerText)
        private val txtPlayer2AnswerText :  TextView = itemView.findViewById(R.id.player2AnswerText)

        private val imgPlayer1Result :  ImageView = itemView.findViewById(R.id.player1ResultImg)
        private val imgPlayer2Result :  ImageView = itemView.findViewById(R.id.player2ResultImg)

        private val txtPlayer1Score :  TextView = itemView.findViewById(R.id.player1Score)
        private val txtPlayer2Score :  TextView = itemView.findViewById(R.id.player2Score)

        fun bind(user1Data: Question, user2Data: Question, user1: GameShowPlayer, user2: GameShowPlayer?, user1QuizScore: Int, user2QuizScore: Int){
            if (user1Data.id != user2Data.id) return

            val user1Answer = user1Data.answers.find { a -> a.isPlayerSelected == true }
            val user2Answer = if (user2 == null) user2Data.answers.find { a -> a.isGamePointSelected }
            else user1Data.answers.find { a -> a.isPlayerSelected == true }





            txtQuestionName.text = user1Data.title
            //user1
            Picasso.get().load(user1.avatar).placeholder(R.drawable.icon_feather_meh)
                .error(R.drawable.icon_feather_meh).into(imgPlayer1Ava)
            txtPlayer1AnswerText.text = user1Answer?.title ?: ""
            imgPlayer1Result.setImageResource(if (user1Answer?.isCorrectAnswer == true)  R.drawable.ic_action_tick else R.drawable.ic_action_cancel)
            txtPlayer1Score.text = "$user1QuizScore"


            //user2
            Picasso.get().load(user2?.avatar).placeholder(R.drawable.icon_feather_meh)
                .error(R.drawable.icon_feather_meh).into(imgPlayer2Ava)
            txtPlayer2AnswerText.text = user2Answer?.title ?: ""
            imgPlayer2Result.setImageResource(if (user2Answer?.isCorrectAnswer == true)  R.drawable.ic_action_tick else R.drawable.ic_action_cancel)
            txtPlayer2Score.text = "$user2QuizScore"

        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val txtPlayer1Ava :  CircleImageView = itemView.findViewById(R.id.imgAva1)
        private val txtPlayer2Ava :  CircleImageView = itemView.findViewById(R.id.imgAva2)

        private val txtPlayer1Name :  TextView = itemView.findViewById(R.id.txtName1)
        private val txtPlayer2Name :  TextView = itemView.findViewById(R.id.txtName2)

        private val txtPlayer1Score :  TextView = itemView.findViewById(R.id.txtPlayer1Score)
        private val txtPlayer2Score :  TextView = itemView.findViewById(R.id.txtPlayer2Score)

        private val txtResult :  TextView = itemView.findViewById(R.id.txtResult)
        private val txtWager :  TextView = itemView.findViewById(R.id.txtWagedPoint)

        fun bind(data: GameShowResult){
            Picasso.get().load(data.player1?.avatar).placeholder(R.drawable.icon_feather_meh)
                .error(R.drawable.icon_feather_meh).into(txtPlayer1Ava)
            if (data.player2?.avatar.isNullOrEmpty()){
                txtPlayer2Ava.setImageResource(R.drawable.app_icon)
            }else{

                Picasso.get().load(data.player2?.avatar).placeholder(R.drawable.icon_feather_meh)
                    .error(R.drawable.icon_feather_meh).into(txtPlayer2Ava)
            }

            txtPlayer1Name.text = data.player1?.name ?: ""
            txtPlayer2Name.text = data.player2?.name ?: "GamePoint"

            val player1Score = data.player1Score ?: -1
            val player2Score = data.player2Score ?: -1

            txtPlayer1Score.text = player1Score.toString()
            txtPlayer2Score.text = player2Score.toString()

            txtWager.text = data.wager?.toString() ?: "-"

            if (player1Score == player2Score){
                txtResult.text = "DRAW GAME!"
                txtResult.setTextColor(Color.parseColor("#FF9A2C"))
            }
            if (player1Score > player2Score){
                txtResult.text = "YOU WON!"
                txtResult.setTextColor(Color.parseColor("#62bb3b"))
            }
            if (player1Score < player2Score){
                txtResult.text = "YOU LOSE!"
                txtResult.setTextColor(Color.parseColor("#A31621"))
            }
        }
    }

    private val user1Answers: List<Question>
    private val user2Answers: List<Question>

    val TYPE_HEADER = 0
    val TYPE_QUESTION = 1

    init {
        val gson = Gson()
        user1Answers = gson.fromJson<List<Question>>(data.player1Answer ?: "[]", object : TypeToken<List<Question>>(){}.type)
        user2Answers = gson.fromJson<List<Question>>(data.player2Answer ?: "[]", object : TypeToken<List<Question>>(){}.type)
    }

    override fun getItemViewType(position: Int) = if (position == 0) TYPE_HEADER else TYPE_QUESTION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType){
            TYPE_HEADER -> HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_game_result_header, parent, false))
            else -> ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_game_result_question, parent, false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is HeaderViewHolder){
            holder.bind(data)
        }
        if (holder is ItemViewHolder){
            val q1 = user1Answers[position - 1]
            val q2 = user2Answers[position - 1]


            val isUser2GamePoint = data.player2 == null

            val user1QuizScore = user1Answers.subList(0, position).filter { question ->
                question.answers.find { answer -> answer.isPlayerSelected == true && answer.isCorrectAnswer } != null }.size
            val user2QuizScore = user2Answers.subList(0, position).filter { question ->
                question.answers.find { answer -> (if (isUser2GamePoint) answer.isGamePointSelected else answer.isPlayerSelected == true) && answer.isCorrectAnswer } != null }.size


            holder.bind(q1, q2, data.player1!!, data.player2, user1QuizScore, user2QuizScore)
        }
    }

    override fun getItemCount(): Int {
        return user1Answers.size + 1
    }
}