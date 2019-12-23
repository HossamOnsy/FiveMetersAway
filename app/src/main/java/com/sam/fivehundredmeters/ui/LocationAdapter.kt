package com.sam.fivehundredmeters.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sam.fivehundredmeters.MyApplication.Companion.appContext
import com.sam.fivehundredmeters.R
import com.sam.fivehundredmeters.models.location.Item
import com.sam.fivehundredmeters.models.location.Venue


class LocationAdapter(var dataList: List<Venue>) :
    RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(appContext).inflate(
                R.layout.location_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvName.text = dataList[position].name
        holder.tvAddress.text = dataList[position].location.formattedAddress[0]
    }

    fun updatelist(list: List<Venue>) {
        dataList = list
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvName: TextView = itemView.findViewById(R.id.locationname)
        var tvAddress: TextView = itemView.findViewById(R.id.locationaddress)
    }
}