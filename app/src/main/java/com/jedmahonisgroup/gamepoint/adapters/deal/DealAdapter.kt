package com.jedmahonisgroup.gamepoint.adapters.deal

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jedmahonisgroup.gamepoint.GamePointDealDetailListener
import com.jedmahonisgroup.gamepoint.model.DealsUi

class DealAdapter(private val response: List<DealsUi>, redeemFragment: GamePointDealDetailListener, private val schoolColor: String?) : androidx.recyclerview.widget.RecyclerView.Adapter<DealViewHolder>() {
    private var mListener: GamePointDealDetailListener = redeemFragment

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DealViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return DealViewHolder(inflater, parent, schoolColor)
    }

    override fun getItemCount(): Int {
        return response.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun onBindViewHolder(holder: DealViewHolder, position: Int) {
        val data: DealsUi = response[position]
        holder.bind(data, mListener)
    }

}