package com.tugasakhir.udmrputra.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Pengiriman

class PengirimanAdapter(private val pengirimanList: List<Pengiriman>) : RecyclerView.Adapter<PengirimanAdapter.MitraViewHolder>() {

    class MitraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.pengiriman_name)
        val location: TextView = itemView.findViewById(R.id.pengiriman_location)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MitraViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pengiriman_item, parent, false)
        return MitraViewHolder(view)
    }

    override fun onBindViewHolder(holder: MitraViewHolder, position: Int) {
        val currentItem = pengirimanList[position]
        holder.name.text = currentItem.name
        holder.location.text = currentItem.location
        holder.itemView.setOnClickListener {

        }
    }

    override fun getItemCount() = pengirimanList.size
}