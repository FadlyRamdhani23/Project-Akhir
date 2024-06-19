package com.tugasakhir.udmrputra.ui.barang

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Barang
import com.tugasakhir.udmrputra.ui.pengajuan.ActivityPengajuan

class BarangAdapter(private val context: Context, private val barangList: List<Barang>) : RecyclerView.Adapter<BarangAdapter.BarangViewHolder>() {

    class BarangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.namaListBarang)
        val gambar: ImageView = itemView.findViewById(R.id.imageListBarang)
        val jumlah: TextView = itemView.findViewById(R.id.jumlahListBarang)
        val jenis: TextView = itemView.findViewById(R.id.jenisListBarang)
        val catatan: TextView = itemView.findViewById(R.id.catatanListBarang)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarangViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_barang, parent, false)
        return BarangViewHolder(view)
    }

    override fun onBindViewHolder(holder: BarangViewHolder, position: Int) {
        val currentItem = barangList[position]
        holder.name.text = currentItem.name
        holder.jumlah.text = currentItem.jumlah
        holder.jenis.text = currentItem.jenis
        holder.catatan.text = currentItem.catatan

        Glide.with(context)
            .load(currentItem.gambar) // replace with your image URL or resource
            .circleCrop()
            .fitCenter()
            .into(holder.gambar)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ActivityPengajuan::class.java)
            context.startActivity(intent)
        }

    }

    override fun getItemCount() = barangList.size
}