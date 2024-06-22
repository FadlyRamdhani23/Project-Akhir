package com.tugasakhir.udmrputra.ui.sopir

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Sopir

class SopirAdapter (private val sopirList: ArrayList<Sopir>) : RecyclerView.Adapter<SopirAdapter.SopirViewHolder>() {

    class SopirViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.nama_sopir)
        val plat: TextView = itemView.findViewById(R.id.noHp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SopirViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sopir_item, parent, false)
        return SopirViewHolder(view)
    }

    override fun onBindViewHolder(holder: SopirViewHolder, position: Int) {
        val currentItem = sopirList[position]
        holder.name.text = currentItem.nama
        holder.plat.text = currentItem.noHp
    }

    override fun getItemCount() = sopirList.size
}