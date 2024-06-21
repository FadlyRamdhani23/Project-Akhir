package com.tugasakhir.udmrputra.ui.barang

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Pencatatan
import com.tugasakhir.udmrputra.data.PencatatanKeluar
import com.tugasakhir.udmrputra.ui.pengajuan.ActivityPengajuan

class PencatatanKeluarAdapter(private val context: Context, private val pencatatanList: List<PencatatanKeluar>) : RecyclerView.Adapter<PencatatanKeluarAdapter.PencatatanViewHolder>() {
    class PencatatanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jenis: TextView = itemView.findViewById(R.id.jenis_barang)
        val namaBrg: TextView = itemView.findViewById(R.id.barang_name)
        val namaMitra: TextView = itemView.findViewById(R.id.mitra_name)
        val harga: TextView = itemView.findViewById(R.id.harga_barang)
        val jumlah: TextView = itemView.findViewById(R.id.jumlah_barang)
        val warna = itemView.findViewById<ImageView>(R.id.warna_jenis)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PencatatanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_pencatatan, parent, false)
        return PencatatanViewHolder(view)
    }

    override fun getItemCount(): Int {
        return pencatatanList.size
    }

    override fun onBindViewHolder(holder: PencatatanViewHolder, position: Int) {
        val data = pencatatanList[position]
        holder.jenis.text = if (data.catId == "JV9d40TfUWOHoyg8i5Wt") {
            holder.warna.setImageResource(R.color.blue)
            "Buah"
        } else {
            holder.warna.setImageResource(R.color.green)
            "Sayur"
        }
        holder.namaBrg.text = data.barangId
        holder.namaMitra.text = data.namaPetani
        holder.harga.text = data.hargaJual + " Rp"
        holder.jumlah.text = "${data.jumlah} Kg"


        holder.itemView.setOnClickListener {
            val intent = Intent(context, ActivityPengajuan::class.java)
            context.startActivity(intent)
        }
    }
}
