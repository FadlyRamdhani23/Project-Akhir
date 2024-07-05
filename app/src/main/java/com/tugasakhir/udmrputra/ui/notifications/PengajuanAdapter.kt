package com.tugasakhir.udmrputra.ui.notifications

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Pengajuan
import com.tugasakhir.udmrputra.ui.pengajuan.DetailPengajuanActivity

class PengajuanAdapter(private val pengajuanList: List<Pengajuan>) :
    RecyclerView.Adapter<PengajuanAdapter.PengajuanViewHolder>() {

    class PengajuanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userId: TextView = view.findViewById(R.id.userId)
        val tanggalPengajuan: TextView = view.findViewById(R.id.tanggalPengajuan)
        val barangAjuan: TextView = view.findViewById(R.id.barangAjuan)
        val listBarang: TextView = view.findViewById(R.id.listBarang)
        val jenisPembayaran: TextView = view.findViewById(R.id.jenisPembayaran)
        val statusPengajuan: TextView = view.findViewById(R.id.statusPengajuan)
        val warna: ImageView = view.findViewById(R.id.warna_jenis)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PengajuanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pengajuan_verification, parent, false)
        return PengajuanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PengajuanViewHolder, position: Int) {
        val pengajuan = pengajuanList[position]
        holder.userId.text = pengajuan.userId
        holder.tanggalPengajuan.text = pengajuan.tanggalPengajuan
        holder.barangAjuan.text = pengajuan.barangAjuan
        holder.listBarang.text = pengajuan.listBarang.joinToString(", ")
        holder.jenisPembayaran.text = pengajuan.jenisPembayaran
        holder.statusPengajuan.text = pengajuan.statusPengajuan
        Log.d("status", pengajuan.statusPengajuan)

        val context = holder.itemView.context

        // Mengatur warna berdasarkan status pengajuan
        when (pengajuan.statusPengajuan) {
            "pending" -> {
                holder.warna.setBackgroundResource(R.color.yellow) // Warna kuning untuk pending
                holder.statusPengajuan.setTextColor(ContextCompat.getColor(context, R.color.black)) // Warna teks hitam
            }
            "approved" -> {
                holder.warna.setBackgroundResource(R.color.green) // Warna hijau untuk approve
                holder.statusPengajuan.setTextColor(ContextCompat.getColor(context, R.color.white)) // Warna teks putih
            }
            "dikemas" -> {
                holder.warna.setBackgroundResource(R.color.blue) // Warna biru untuk dikemas
                holder.statusPengajuan.setTextColor(ContextCompat.getColor(context, R.color.white)) // Warna teks putih
            }
            "pengiriman" -> {
                holder.warna.setBackgroundResource(R.color.light_blue) // Warna biru muda untuk pengiriman
                holder.statusPengajuan.setTextColor(ContextCompat.getColor(context, R.color.black)) // Warna teks hitam
            }
            else -> {
                holder.warna.setBackgroundResource(R.color.gray) // Warna abu-abu untuk status tidak dikenal
                holder.statusPengajuan.setTextColor(ContextCompat.getColor(context, R.color.black)) // Warna teks hitam
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailPengajuanActivity::class.java)
            intent.putExtra("pengajuanId", pengajuan.id) // Menambahkan ID Pengajuan ke Intent
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return pengajuanList.size
    }
}
