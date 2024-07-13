package com.tugasakhir.udmrputra.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Pengajuan

class InputPengajuanAdapter(
    private val context: Context,
    private var pengajuanList: List<Pengajuan>,
    private val onItemClick: (Pengajuan) -> Unit
) : RecyclerView.Adapter<InputPengajuanAdapter.InputPengajuanViewHolder>() {

    private val selectedPengajuan = mutableSetOf<Pengajuan>()

    class InputPengajuanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userId: TextView = view.findViewById(R.id.userId)
        val tanggalPengajuan: TextView = view.findViewById(R.id.tanggalPengajuan)
        val listBarang: TextView = view.findViewById(R.id.listBarang)
        val jenisPembayaran: TextView = view.findViewById(R.id.jenisPembayaran)
        val statusPengajuan: TextView = view.findViewById(R.id.statusPengajuan)
        val warna : ImageView = view.findViewById(R.id.warna_jenis)
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

        when (currentItem.statusPengajuan) {
            "Penawaran" -> {
                holder.warna.setBackgroundResource(R.color.blue) // Warna biru untuk penawaran
                holder.statusPengajuan.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.black
                    )
                ) // Warna teks hitam
            }

            "Bayar" -> {
                holder.warna.setBackgroundResource(R.color.yellow) // Warna kuning untuk bayar
                holder.statusPengajuan.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.black
                    )
                ) // Warna teks hitam
            }

            "Disetujui" -> {
                holder.warna.setBackgroundResource(R.color.green) // Warna hijau untuk disetujui
                holder.statusPengajuan.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                ) // Warna teks putih
            }

            "Dikemas" -> {
                holder.warna.setBackgroundResource(R.color.orange) // Warna oranye untuk dikemas
                holder.statusPengajuan.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                ) // Warna teks putih
            }

            "Pengiriman" -> {
                holder.warna.setBackgroundResource(R.color.light_blue) // Warna biru muda untuk pengiriman
                holder.statusPengajuan.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.black
                    )
                ) // Warna teks hitam
            }

            "Selesai" -> {
                holder.warna.setBackgroundResource(R.color.light_blue) // Warna biru muda untuk selesai
                holder.statusPengajuan.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.black
                    )
                ) // Warna teks hitam
            }

            "Ditolak" -> {
                holder.warna.setBackgroundResource(R.color.red) // Warna merah untuk ditolak
                holder.statusPengajuan.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                ) // Warna teks putih
            }

            else -> {
                holder.warna.setBackgroundResource(R.color.gray) // Warna abu-abu untuk status tidak dikenal
                holder.statusPengajuan.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.black
                    )
                ) // Warna teks hitam
            }
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
