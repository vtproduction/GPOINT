package com.jedmahonisgroup.gamepoint.adapters.deal

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.RedeemPointsListener
import com.jedmahonisgroup.gamepoint.model.DealModel

class DetailViewHolder(inflater: LayoutInflater, parent: ViewGroup, private val dealDetailAdapter: DealDetailAdapter) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(inflater.inflate(R.layout.item_deal_list, parent, false)) {

    /*private var mDealName: TextView? = null
    private var mRedeemPointsBtn: Button? = null
    var separator: View? = null*/

    private var mImg1: ImageView? = null
    private var mImg2: ImageView? = null
    private var mTxt1: TextView? = null
    private var mTxt2: TextView? = null
    private var mTxt3: TextView? = null

    init {
        /*mDealName = itemView.findViewById(R.id.dealName)
        mRedeemPointsBtn = itemView.findViewById(R.id.redeemPointsButton)
        separator = itemView.findViewById(R.id.separator2)*/

        mImg1 = itemView.findViewById(R.id.img_logo_1)
        mImg2 = itemView.findViewById(R.id.img_logo_2)
        mTxt1 = itemView.findViewById(R.id.txt_title)
        mTxt2 = itemView.findViewById(R.id.txt_sub_title)
        mTxt3 = itemView.findViewById(R.id.txt_balance)
    }

    fun bind(deal: DealModel, mListener: RedeemPointsListener) {
        mTxt1?.text = deal.name
        mTxt2?.text = deal.description
        mTxt3?.text = deal.point_value.toString()
        Glide.with(itemView).load(deal.businesses.logo).circleCrop().error(R.drawable.grey_bitmap_bkg).into(mImg1!!)
        Glide.with(itemView).load(deal.hero_image).error(R.drawable.grey_bitmap_bkg).into(mImg2!!)


        if (deal.isBeingRedeemed){

            itemView.setOnClickListener {
                mListener.onActiveDealClicked(deal)
            }
        }else{

            itemView.setOnClickListener {
                mListener.onRedeemPointsClicked(deal)
            }

        }

    }


}