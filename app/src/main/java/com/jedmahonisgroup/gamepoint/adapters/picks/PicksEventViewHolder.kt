package com.jedmahonisgroup.gamepoint.adapters.picks

import android.content.Context
import android.graphics.Color
import android.graphics.ColorFilter
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.jedmahonisgroup.gamepoint.BuildConfig
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.RecyclerviewClickListener
import com.jedmahonisgroup.gamepoint.model.picks.OpenPicksModel
import com.jedmahonisgroup.gamepoint.utils.StringFormatter
import com.squareup.picasso.Picasso
import android.graphics.drawable.GradientDrawable
import com.jedmahonisgroup.gamepoint.ui.color
import com.jedmahonisgroup.gamepoint.ui.sharedPreferences
import com.jedmahonisgroup.gamepoint.utils.Constants


class PicksEventViewHolder(inflater: LayoutInflater, private val parent: ViewGroup) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(inflater.inflate(R.layout.conference_card, parent, false)) {

    private var mLeadingIcon: ImageView? = null
    private var TAG: String = PicksEventViewHolder::class.java.simpleName
    private var mTitle: TextView? = null
    private var mSubTitleText: TextView? = null
    private var mTeam1Button: Button? = null
    private var mTeam2Button: Button? = null
    private var mPickPointValue: TextView? = null
    private var mDescTextView: TextView? = null
    private var mView: View? = null
    private var mContext: Context? = null
    private var color: Int? = null

    var separator: View? = null


    init {
        mLeadingIcon = itemView.findViewById(R.id.leadingIcon)
        mTitle = itemView.findViewById(R.id.gameTitle)
        mSubTitleText = itemView.findViewById(R.id.subTitleText)
        mTeam1Button = itemView.findViewById(R.id.leadingButton)
        mTeam2Button = itemView.findViewById(R.id.trailingButton)
        mPickPointValue = itemView.findViewById(R.id.pointValue)
        mDescTextView = itemView.findViewById(R.id.descTextView)
        mView = itemView

        separator = itemView.findViewById(R.id.separator)
    }

    fun bind(data: OpenPicksModel, mListener: RecyclerviewClickListener?, mContxt: Context?) {
        mContext = mContxt
        mTitle?.text = data.name
        mSubTitleText?.text = StringFormatter.convertTime(data.start_time)
        mTeam1Button?.text = data.team1
        mTeam2Button?.text = data.team2
        mPickPointValue?.text = "${data.point_value}"
        mDescTextView?.text = data.description

        Color.parseColor(
            sharedPreferences.getString(
                Constants.PRIMARY_COLOR,
                R.color.colorPrimary.toString()
            )
        ).also { color = it }
        mTitle!!.setTextColor(color!!)

        mTeam1Button?.setOnClickListener {
            mListener?.onTeam1Clicked(data.team1, data.id)


            mTeam1Button?.isSelected = true
            mTeam1Button?.isPressed = true

            mTeam2Button?.isSelected = false
            mTeam2Button?.isPressed = false

            mTeam1Button?.setTextColor(ContextCompat.getColor(mContext!!, R.color.colorWhite))
            mTeam2Button?.setTextColor(color!!)

            color?.let { it1 -> setCustomBackground(mTeam1Button!!, it1, color!!) }
            color?.let { it1 -> setCustomBackground(mTeam2Button!!, Color.WHITE, it1) }
        }

        mTeam2Button?.setOnClickListener {
            mListener?.onTeam2Clicked(data.team2, data.id)
            mTeam2Button?.isSelected = true
            mTeam2Button?.isPressed = true

            mTeam1Button?.isSelected = false
            mTeam1Button?.isPressed = false
            mTeam2Button?.setTextColor(ContextCompat.getColor(mContext!!, R.color.colorWhite))
            mTeam1Button?.setTextColor(color!!)

            color?.let { it1 -> setCustomBackground(mTeam2Button!!, it1, color!!) }
            color?.let { it1 -> setCustomBackground(mTeam1Button!!, Color.WHITE, it1) }

        }
        Log.e(TAG, "data.my_pick: " + data.my_pick)


        if ( data.my_pick == 2) {
            mTeam2Button?.isSelected = true
            mTeam2Button?.isPressed = true

            mTeam1Button?.isSelected = false
            mTeam1Button?.isPressed = false

            mTeam2Button?.setTextColor(ContextCompat.getColor(mContext!!, R.color.colorWhite))
            mTeam1Button?.setTextColor(color!!)

            color?.let { it1 -> setCustomBackground(mTeam2Button!!, it1, color!!) }
            color?.let { it1 -> setCustomBackground(mTeam1Button!!, Color.WHITE, it1) }
        } else if (data.my_pick == 1) {
            mTeam1Button?.isSelected = true
            mTeam1Button?.isPressed = true



            mTeam2Button?.isSelected = false
            mTeam2Button?.isPressed = false

            mTeam1Button?.setTextColor(ContextCompat.getColor(mContext!!, R.color.colorWhite))
            mTeam2Button?.setTextColor(color!!)

            color?.let { it1 -> setCustomBackground(mTeam1Button!!, it1, color!!) }
            color?.let { it1 -> setCustomBackground(mTeam2Button!!, Color.WHITE, it1) }
        }

        if (!data.sport.icon.isNullOrEmpty()) {
            val picasso = Picasso.get()
            picasso.setIndicatorsEnabled(false)
            picasso.load(data.sport.icon)
                    //  .placeholder(R.drawable.user_placeholder)
                    //      .error(R.drawable.user_placeholder_error)
                    .into(mLeadingIcon)
        }
        mLeadingIcon!!.setColorFilter(color!!)


    }

    fun setCustomBackground(v: View, backgroundColor: Int, borderColor: Int) {
        v.post {
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            var halfHeight = v.height / 2.0f
            shape.cornerRadii = floatArrayOf(
                halfHeight,
                halfHeight,
                halfHeight,
                halfHeight,
                halfHeight,
                halfHeight,
                halfHeight,
                halfHeight
            )
            shape.setColor(backgroundColor)
            shape.setStroke(1, borderColor)
            v.background = shape
        }
    }

}