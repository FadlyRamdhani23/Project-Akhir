package com.tugasakhir.udmrputra.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Barang
import com.tugasakhir.udmrputra.data.Pengajuan
import com.tugasakhir.udmrputra.ui.pengajuan.PengajuanAdapterBarang
class InpuPengajuanAdapter(
    private val context: Context,
    private var pengajuanList: List<Pengajuan>,
    private val onItemClick: (Pengajuan) -> Unit
) : RecyclerView.Adapter<InpuPengajuanAdapter.InputPengajuanViewHolder>() {

    private val selectedPengajuan = mutableSetOf<Pengajuan>()

    class InputPengajuanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userId: TextView = view.findViewById(R.id.userId)
        val tanggalPengajuan: TextView = view.findViewById(R.id.tanggalPengajuan)
        val barangAjuan: TextView = view.findViewById(R.id.barangAjuan)
        val listBarang: TextView = view.findViewById(R.id.listBarang)
        val jenisPembayaran: TextView = view.findViewById(R.id.jenisPembayaran)
        val statusPengajuan: TextView = view.findViewById(R.id.statusPengajuan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InputPengajuanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pengajuan_verification, parent, false)
        return InputPengajuanViewHolder(view)
    }

    override fun onBindViewHolder(holder: InputPengajuanViewHolder, position: Int) {
        val currentItem = pengajuanList[position]
        holder.userId.text = currentItem.userId
        holder.tanggalPengajuan.text = currentItem.tanggalPengajuan
        holder.barangAjuan.text = currentItem.barangAjuan
        holder.listBarang.text = currentItem.listBarang.joinToString(", ")
        holder.jenisPembayaran.text = currentItem.jenisPembayaran
        holder.statusPengajuan.text = currentItem.statusPengajuan

        holder.itemView.setOnClickListener {
            if (selectedPengajuan.contains(currentItem)) {
                selectedPengajuan.remove(currentItem)
            } else {
                selectedPengajuan.add(currentItem)
            }
            notifyItemChanged(position)
            onItemClick(currentItem)
        }

        // Ubah tampilan item berdasarkan status pemilihannya
        if (selectedPengajuan.contains(currentItem)) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.green))
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        }
    }

    override fun getItemCount() = pengajuanList.size

    fun updateList(newList: List<Pengajuan>) {
        pengajuanList = newList
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<Pengajuan> {
        return selectedPengajuan.toList()
    }
}
