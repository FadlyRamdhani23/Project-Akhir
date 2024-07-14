package com.tugasakhir.udmrputra.ui.sopir

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Pengiriman

class DaftarPengirimanAdapter(private val pengirimanList: List<Pengiriman>) : RecyclerView.Adapter<DaftarPengirimanAdapter.MitraViewHolder>() {

    class MitraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.supirId)
        val status: TextView = itemView.findViewById(R.id.status_pengiriman)
        val tanggal: TextView = itemView.findViewById(R.id.tanggalPengajuan)
        val tujuan : TextView = itemView.findViewById(R.id.alamat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MitraViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pengiriman_item, parent, false)
        return MitraViewHolder(view)
    }

    override fun onBindViewHolder(holder: MitraViewHolder, position: Int) {
        val currentItem = pengirimanList[position]
        holder.name.text = currentItem.supir
        holder.status.text = currentItem.status
        holder.tanggal.text = currentItem.tanggal
        holder.tujuan.text = currentItem.address
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, SupirActivity::class.java)
            intent.putExtra("PENGIRIMAN_ID", currentItem.id) // Menambahkan ID Pengiriman ke Intent
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = pengirimanList.size
}