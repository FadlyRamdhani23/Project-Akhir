package com.tugasakhir.udmrputra.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Pengajuan

class PengajuanAdapter(private val pengajuanList: List<Pengajuan>) :
    RecyclerView.Adapter<PengajuanAdapter.PengajuanViewHolder>() {

    class PengajuanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userId: TextView = view.findViewById(R.id.userId)
        val tanggalPengajuan: TextView = view.findViewById(R.id.tanggalPengajuan)
        val barangAjuan: TextView = view.findViewById(R.id.barangAjuan)
        val listBarang: TextView = view.findViewById(R.id.listBarang)
        val jenisPembayaran: TextView = view.findViewById(R.id.jenisPembayaran)
        val statusPengajuan: TextView = view.findViewById(R.id.statusPengajuan)
        val warna = view.findViewById<View>(R.id.warna_jenis)
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


    }

    override fun getItemCount(): Int {
        return pengajuanList.size
    }
}
