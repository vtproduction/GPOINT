package com.jedmahonisgroup.gamepoint.adapters.results

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.jedmahonisgroup.gamepoint.BuildConfig
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.picks.MyPicksModel
import com.jedmahonisgroup.gamepoint.utils.StringFormatter
import com.squareup.picasso.Picasso

class ResultsViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(inflater.inflate(R.layout.conference_card, parent, false)) {


    private var mLeadingIcon: ImageView? = null
    private var mTitle: TextView? = null
    private var mSubTitleText: TextView? = null
    private var mTeam1Button: Button? = null
    private var mTeam2Button: Button? = null
    private var mPickPointValue: TextView? = null
    private var mView: View? = null

    var separator: View? = null


    init {
        mLeadingIcon = itemView.findViewById(R.id.leadingIcon)
        mTitle = itemView.findViewById(R.id.gameTitle)
        mSubTitleText = itemView.findViewById(R.id.subTitleText)
        mTeam1Button = itemView.findViewById(R.id.leadingButton)
        mTeam2Button = itemView.findViewById(R.id.trailingButton)
        mPickPointValue = itemView.findViewById(R.id.pointValue)
        mView = itemView

        separator = itemView.findViewById(R.id.separator)
    }

    fun bind(data: MyPicksModel) {

        mTitle?.text = data.name
        mSubTitleText?.text = StringFormatter.convertTime(data.start_time)
        mTeam1Button?.text = data.team1
        mTeam2Button?.text = data.team2
        mPickPointValue?.text = "${data.point_value}"

        mTeam1Button?.isClickable = false
        mTeam2Button?.isClickable = false

        if (data.my_pick == data.winner){
            //I picked a winner;
            if (data.winner.equals("1")){
                //highlight button 1
                mTeam1Button?.background = (itemView.resources.getDrawable(R.drawable.bg_winner))
                mTeam1Button?.setTextColor(itemView.resources.getColor(R.color.colorWhite))

            }else{
                //high light button 2
                mTeam2Button?.background = (itemView.resources.getDrawable(R.drawable.bg_winner))
                mTeam2Button?.setTextColor(itemView.resources.getColor(R.color.colorWhite))

            }

        }else{
            //user picked a looser
            if (data.my_pick.equals("1")){
                //highlight button 1
                mTeam1Button?.background = (itemView.resources.getDrawable(R.drawable.bg_looser))

            }else{
                mTeam2Button?.background = (itemView.resources.getDrawable(R.drawable.bg_looser))

            }
        }

        if (!data.sport.icon.isNullOrEmpty()) {
            val picasso = Picasso.get()
            picasso.setIndicatorsEnabled(false)
            picasso.load(data.sport.icon).into(mLeadingIcon)
        }

    }

}