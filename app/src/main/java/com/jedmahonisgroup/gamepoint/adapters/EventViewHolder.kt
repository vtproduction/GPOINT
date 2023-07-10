package com.jedmahonisgroup.gamepoint.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jedmahonisgroup.gamepoint.GamePointResultListener
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.events.EventsModel
import com.jedmahonisgroup.gamepoint.utils.StringFormatter
import com.squareup.picasso.Picasso
import org.joda.time.DateTime
import java.util.*


class EventViewHolder(inflater: LayoutInflater, private val parent: ViewGroup, private val schoolColor: String?) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.layout_new_list_event_item, parent, false)) {

    private var TAG: String = EventViewHolder::class.java.simpleName
    /*private var mSportImage: ImageView? = null
    private var mTitle: TextView? = null
    private var mStartTime: TextView? = null
    private var mLocation: TextView? = null
    private var mPointsButton: Button? = null
    private var mView: View? = null
    private var mCheckInButton: Button? = null
    var separator: View? = null*/

    private var imgType: ImageView? = null
    private var txtTime: TextView? = null
    private var txtLocation: TextView? = null
    private var imgLogo1: ImageView? = null
    private var imgLogo2: ImageView? = null


    init {
        /*mSportImage = itemView.findViewById(R.id.sportPic)
        mTitle = itemView.findViewById(R.id.gameTitle)
        mStartTime = itemView.findViewById(R.id.gameDateTime)
        mLocation = itemView.findViewById(R.id.gameLocation)
        mPointsButton = itemView.findViewById(R.id.pointsButton)
        mView = itemView
        mCheckInButton = itemView.findViewById(R.id.checkedIn)

        separator = itemView.findViewById(R.id.separator)*/
        imgType = itemView.findViewById(R.id.img_logo_0)
        imgLogo1 = itemView.findViewById(R.id.img_logo_1)
        imgLogo2 = itemView.findViewById(R.id.img_logo_2)
        txtTime = itemView.findViewById(R.id.txt_time)
        txtLocation = itemView.findViewById(R.id.txt_location)
    }

    fun bind(data: EventsModel, mListener: GamePointResultListener?) {


        if (!data.sport?.icon.isNullOrEmpty()){
            Picasso.get()
                .load( data.sport?.icon)
                // .placeholder(R.drawable.event_fallback1000)
                .error(R.drawable.bg_event_list_item)
                .into(imgType)
        }
        if (!data.school?.logo.isNullOrEmpty()){
            Picasso.get()
                .load( data.school?.logo)
                // .placeholder(R.drawable.event_fallback1000)
                .error(R.drawable.bg_event_list_item)
                .into(imgLogo1)
        }
        if (!data.awaySchool?.logo.isNullOrEmpty()){
            Picasso.get()
                .load( data.awaySchool?.logo)
                // .placeholder(R.drawable.event_fallback1000)
                .error(R.drawable.bg_event_list_item)
                .into(imgLogo2)
        }

        txtLocation?.text = data.venue.name
        Log.d("MEOMEO", DateTime.now().toString("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
        txtTime?.text = StringFormatter.convertTimeSpan(data.startTime,data.minutesToRedeem)
        itemView.setOnClickListener { mListener?.onCardClicked(data) }


        /*mTitle?.text = data.name
        mStartTime?.text = StringFormatter.convertTimeSpan(data.startTime,data.minutesToRedeem)
        mPointsButton?.text = "${data.pointValue} pts"
        mLocation?.text = data.venue.name*/




        /*val btnShape = GradientDrawable()
        btnShape.shape = GradientDrawable.RECTANGLE
        btnShape.cornerRadius= 10.0f
        btnShape.setColor(Color.parseColor(schoolColor))
        mCheckInButton!!.background = btnShape*/
       // val time = dt.hourOfDay.toString() + ":" +dt.minuteOfHour.toString()

        //Log.e(TAG, "TIME: ${time}")

        /*if (!data.heroImage.isNullOrEmpty()) {
            Picasso.get()
                    .load( data.heroImage)
                    // .placeholder(R.drawable.event_fallback1000)
                    .error(R.drawable.event_fallback1000)
                    .into(mSportImage)
        }*/

        //launch detail activity

        /*mView?.setOnClickListener {
            mListener?.onCardClicked(data)
        }

        mCheckInButton!!.setOnClickListener {
            mListener?.onCardClicked(data)
        }*/


        //load svg
//        SvgLoader.pluck()
//                .with(parent.getParentActivity())
//                .load(eventsFromServer.sport.icon, mLeadingIcon)

    }

}