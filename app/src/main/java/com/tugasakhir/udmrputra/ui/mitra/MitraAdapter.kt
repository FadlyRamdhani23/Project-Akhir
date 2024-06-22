package com.tugasakhir.udmrputra.ui.mitra

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Mitra
import com.tugasakhir.udmrputra.data.Users

class MitraAdapter(private val mitraList: ArrayList<Mitra>) : RecyclerView.Adapter<MitraAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val namaTextView: TextView = itemView.findViewById(R.id.mitra_name)
        val lokasiTextView: TextView = itemView.findViewById(R.id.noHp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mitra_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mitra = mitraList[position]
        holder.namaTextView.text = mitra.nama
        holder.lokasiTextView.text = mitra.noHp
    }

    override fun getItemCount(): Int {
        return mitraList.size
    }
}
