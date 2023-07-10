package com.jedmahonisgroup.gamepoint.adapters.deal

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jedmahonisgroup.gamepoint.RedeemPointsListener
import com.jedmahonisgroup.gamepoint.model.DealModel
import com.jedmahonisgroup.gamepoint.ui.deals.DealDetailFragment

class DealDetailAdapter(listener: DealDetailFragment) : androidx.recyclerview.widget.RecyclerView.Adapter<DetailViewHolder>() {
    private var mListener: RedeemPointsListener = listener
    private val activeDeals: ArrayList<DealModel> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return DetailViewHolder(inflater, parent, this)
    }

    override fun getItemCount(): Int {
        return activeDeals?.size!!
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        val deal: DealModel = activeDeals[position]
        /*last edit text in rv change click action to done*/

        holder.bind(deal, mListener)
    }

    fun refreshList(deals: List<DealModel>) {
        this.activeDeals.clear()
        val mDeals = ArrayList(deals)
        activeDeals.addAll(mDeals)

        notifyDataSetChanged()

    }

}


