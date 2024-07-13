package com.tugasakhir.udmrputra.ui.dashboard

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Pengiriman
import com.tugasakhir.udmrputra.ui.pengiriman.MapsActivity

class PengirimanAdapter(private val pengirimanList: List<Pengiriman>) : RecyclerView.Adapter<PengirimanAdapter.MitraViewHolder>() {

    class MitraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.supirId)
        val status: TextView = itemView.findViewById(R.id.status_pengiriman)
        val tanggal: TextView = itemView.findViewById(R.id.tanggalPengajuan)
        val alamat: TextView = itemView.findViewById(R.id.alamat)
        val warna: ImageView = itemView.findViewById(R.id.warna_jenis)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MitraViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pengiriman_item, parent, false)
        return MitraViewHolder(view)
    }

    override fun onBindViewHolder(holder: MitraViewHolder, position: Int) {
        val currentItem = pengirimanList[position]
        holder.name.text = currentItem.supir
        holder.status.text = currentItem.status
        holder.alamat.text = currentItem.address
        holder.tanggal.text = currentItem.tanggal
        val context = holder.itemView.context
        when (currentItem.status) {
            "Penawaran" -> {
                holder.warna.setBackgroundResource(R.color.blue) // Warna biru untuk penawaran
                holder.status.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.black
                    )
                ) // Warna teks hitam
            }

            "Bayar" -> {
                holder.warna.setBackgroundResource(R.color.yellow) // Warna kuning untuk bayar
                holder.status.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.black
                    )
                ) // Warna teks hitam
            }

            "Disetujui" -> {
                holder.warna.setBackgroundResource(R.color.green) // Warna hijau untuk disetujui
                holder.status.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                ) // Warna teks putih
            }

            "Dikemas" -> {
                holder.warna.setBackgroundResource(R.color.orange) // Warna oranye untuk dikemas
                holder.status.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                ) // Warna teks putih
            }

            "Pengiriman" -> {
                holder.warna.setBackgroundResource(R.color.blue) // Warna biru muda untuk pengiriman
                holder.status.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.black
                    )
                ) // Warna teks hitam
            }

            "Selesai" -> {
                holder.warna.setBackgroundResource(R.color.blue_2) // Warna biru muda untuk selesai
                holder.status.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.black
                    )
                ) // Warna teks hitam
            }

            "Ditolak" -> {
                holder.warna.setBackgroundResource(R.color.red) // Warna merah untuk ditolak
                holder.status.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                ) // Warna teks putih
            }

            else -> {
                holder.warna.setBackgroundResource(R.color.gray) // Warna abu-abu untuk status tidak dikenal
                holder.status.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.black
                    )
                ) // Warna teks hitam
            }
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, MapsActivity::class.java)
            intent.putExtra("PENGIRIMAN_ID", currentItem.id) // Menambahkan ID Pengiriman ke Intent
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = pengirimanList.size
}