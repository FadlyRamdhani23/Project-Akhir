package com.tugasakhir.udmrputra.ui.mitra

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Mitra

class MitraAdapter(private val mitraList: List<Mitra>) : RecyclerView.Adapter<MitraAdapter.MitraViewHolder>() {

    class MitraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.mitra_name)
        val location: TextView = itemView.findViewById(R.id.mitra_location)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MitraViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mitra_item, parent, false)
        return MitraViewHolder(view)
    }

    override fun onBindViewHolder(holder: MitraViewHolder, position: Int) {
        val currentItem = mitraList[position]
        holder.name.text = currentItem.name
        holder.location.text = currentItem.location
    }

    override fun getItemCount() = mitraList.size
}