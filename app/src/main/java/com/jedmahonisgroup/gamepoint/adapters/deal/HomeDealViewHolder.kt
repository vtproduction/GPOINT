package com.jedmahonisgroup.gamepoint.adapters.deal

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.alpha
import androidx.core.view.marginEnd
import androidx.core.view.marginTop
import com.bumptech.glide.Glide
import com.jedmahonisgroup.gamepoint.BuildConfig
import com.jedmahonisgroup.gamepoint.GamePointDealDetailListener
import com.jedmahonisgroup.gamepoint.R
import com.jedmahonisgroup.gamepoint.model.DealModel
import com.jedmahonisgroup.gamepoint.model.DealsUi
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso


class HomeDealViewHolder(inflater: LayoutInflater, private val parent: ViewGroup, private val schoolColor: String?) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(inflater.inflate(R.layout.item_home_deal_list_2, parent, false)) {
    private var TAG: String = HomeDealViewHolder::class.java.simpleName
    /*private var mBusinessIcon: ImageView? = null
    private var mBusinessPic: ImageView? = null
    private var mView: View? = null
    private var mDealsRow: View? = null
    private var mBusinessName: TextView? = null
    private var mNumDeals: TextView? = null
    private var mDistance: TextView? = null
    private var mDeal: TextView? = null
    private var mDealRow: LinearLayout? = null
    private var mNewRewardsTv: TextView? = null*/

    private var mImg1: ImageView? = null
    private var mImg2: ImageView? = null
    private var mTxt1: TextView? = null
    private var mTxt2: TextView? = null

    init {
        mImg1 = itemView.findViewById(R.id.img_logo_1)
        mImg2 = itemView.findViewById(R.id.img_logo_2)
        mTxt1 = itemView.findViewById(R.id.txt_title)
        mTxt2 = itemView.findViewById(R.id.txt_sub_title)

        /*mBusinessIcon = itemView.findViewById(R.id.businessLogo)
        mBusinessPic = itemView.findViewById(R.id.businessPic)
        mBusinessName = itemView.findViewById(R.id.businessName)
        mNumDeals = itemView.findViewById(R.id.numDealsText)
        mDealsRow = itemView.findViewById(R.id.dealsRow)
        mDeal = itemView.findViewById(R.id.dealText)
        mView = itemView.findViewById(R.id.dealView)
        mDistance = itemView.findViewById(R.id.distanceText)
        mDealRow = itemView.findViewById(R.id.dealsRow)
        mNewRewardsTv = itemView.findViewById(R.id.new_rewards_tv)*/
    }

    fun bind(data: DealsUi, mListener: GamePointDealDetailListener?) {

        try {

/*            val dealsShape = GradientDrawable()
            dealsShape.shape = GradientDrawable.RECTANGLE
            dealsShape.cornerRadii = floatArrayOf(0f, 0f, 20f, 20f, 0f, 0f, 20f, 20f)
            val colorWithAlpha = "#80${schoolColor?.replace("#","")}"
            dealsShape.setColor(Color.parseColor(colorWithAlpha))
            dealsShape.setStroke(2, Color.parseColor(colorWithAlpha))
            mNewRewardsTv!!.background = dealsShape

            mBusinessName?.text = data.dealBusinessName

            mDeal?.setTextColor(Color.parseColor(schoolColor))

            if (data.online){
                mDistance!!.visibility = View.GONE
                val param = mDealRow!!.layoutParams as ViewGroup.MarginLayoutParams
                param.setMargins(20,20,0,0)
                mDealRow!!.layoutParams = param
            }else {


                if (!data.distance.isNullOrEmpty()) {
                    mDistance?.text = "${data.distance} mi away"
                } else {
                    mDistance?.text = ""

                }
            }

            if (data.numberOfDeals == 0){
                mDealsRow?.visibility = View.INVISIBLE
            }else{
                mDealsRow?.visibility = View.VISIBLE

                Log.e("viewholderdeal", data.deal!!.size.toString())
                val sat = data.deal!!.distinctBy { it.id }
                val uniqueDeals: Set<DealModel> = HashSet<DealModel>(sat)

                var act = ArrayList<DealModel>()
                for (unique in uniqueDeals) {
                    for (addy in unique.businesses.addresses) {
                        if (addy.id == data.address.id) {
                            act.add(unique)

                        }else{

                            if (data!!.online){
                                act.add(unique)
                            }
                        }
                    }
                }
                act.sortedBy { it.point_value }
                Log.e("viewholderdeal", "act: " + act)
                Log.e("JMG", "act[0].name" + act[0].name)
                mDeal?.text = act[0].name

                if (act!!.size > 1){

                    mNumDeals?.text = "${act!!.size} Deals"

                }else{

                    mNumDeals?.text = "${act!!.size} Deal"


                }


            }*/


            //get images first from cache, if not then get from server

            Glide.with(itemView).load(data.business.logo).circleCrop().error(R.drawable.grey_bitmap_bkg).into(mImg1!!)
            Glide.with(itemView).load(data.business.image).error(R.drawable.grey_bitmap_bkg).into(mImg2!!)
            mTxt1?.text = data.dealBusinessName


            if (data.numberOfDeals == 0){

            }else{


                Log.e("viewholderdeal", data.deal!!.size.toString())
                val sat = data.deal!!.distinctBy { it.id }
                val uniqueDeals: Set<DealModel> = HashSet<DealModel>(sat)

                var act = ArrayList<DealModel>()
                for (unique in uniqueDeals) {
                    for (addy in unique.businesses.addresses) {
                        if (addy.id == data.address.id) {
                            act.add(unique)

                        }else{

                            if (data!!.online){
                                act.add(unique)
                            }
                        }
                    }
                }
                act.sortedBy { it.point_value }
                mTxt2?.text = act[0].name

            }
            //launch detail activity
            itemView.setOnClickListener {
                mListener?.onLaunchDetailScreen(data)
            }
        } catch (e: NullPointerException) {
            Log.e(TAG, "NPE: $e")
        }


    }


}