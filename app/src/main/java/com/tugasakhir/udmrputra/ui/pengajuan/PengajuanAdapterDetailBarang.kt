package com.tugasakhir.udmrputra.ui.pengajuan

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.DetailPengajuan
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class PengajuanAdapterDetailBarang(
    private val context: Context,
    private val pengajuanList: List<DetailPengajuan>
) : RecyclerView.Adapter<PengajuanAdapterDetailBarang.BarangViewHolder>() {

    // Fungsi utilitas untuk memformat angka menjadi format rupiah tanpa desimal
    private fun formatRupiah(number: Long?): String {
        if (number == null) return "Rp 0"
        val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
            groupingSeparator = '.'
        }
        val decimalFormat = DecimalFormat("Rp #,###", symbols)
        return decimalFormat.format(number)
    }

    inner class BarangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val namaBarang: TextView = itemView.findViewById(R.id.tv_item_nama)
        private val hargaPasar: TextView = itemView.findViewById(R.id.tv_harga_pasar)
        private val hargaBeli: TextView = itemView.findViewById(R.id.tv_harga_ajuan)
        private val jumlah: TextView = itemView.findViewById(R.id.tv_jumlah_barang)
        private val catatan: TextView = itemView.findViewById(R.id.tv_catatan)
        private val tanggal: TextView = itemView.findViewById(R.id.tv_tanggal)
        private val gambar: ImageView = itemView.findViewById(R.id.imageView)
        private val total = itemView.findViewById<TextView>(R.id.tv_total_harga)

        fun bind(detailPengajuan: DetailPengajuan) {
            namaBarang.text = detailPengajuan.nama
            hargaPasar.text = formatRupiah(detailPengajuan.hargaPasar)
            hargaBeli.text = formatRupiah(detailPengajuan.hargaBeli)
            jumlah.text = detailPengajuan.jumlah.toString()
            catatan.text = detailPengajuan.catatan
            tanggal.text = detailPengajuan.tanggal

            total .text = formatRupiah(detailPengajuan.hargaBeli?.times(detailPengajuan.jumlah!!))
            Glide.with(context)
                .load(detailPengajuan.gambar) // replace with your image URL or resource
                .circleCrop()
                .fitCenter()
                .into(gambar)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarangViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_detail_pesanan_mitra, parent, false)
        return BarangViewHolder(view)
    }

    override fun onBindViewHolder(holder: BarangViewHolder, position: Int) {
        holder.bind(pengajuanList[position])
    }

    override fun getItemCount() = pengajuanList.size
}