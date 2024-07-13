package com.tugasakhir.udmrputra.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.PencatatanKeluar
import com.tugasakhir.udmrputra.ui.barang.DetailPencatatanKeluarActivity
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class PencatatanKeluarAdapter(private val context: Context, private val pencatatanList: List<PencatatanKeluar>) : RecyclerView.Adapter<PencatatanKeluarAdapter.PencatatanViewHolder>() {
    class PencatatanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jenis: TextView = itemView.findViewById(R.id.jenis_barang)
        val namaBrg: TextView = itemView.findViewById(R.id.barang_name)
        val namaMitra: TextView = itemView.findViewById(R.id.mitra_name)
        val harga: TextView = itemView.findViewById(R.id.harga_barang)
        val jumlah: TextView = itemView.findViewById(R.id.jumlah_barang)
        val warna = itemView.findViewById<ImageView>(R.id.warna_jenis)!!
        val tanggal: TextView = itemView.findViewById(R.id.tanggal_masuk)
    }
    private fun formatRupiah(numberString: String?): String {
        if (numberString.isNullOrEmpty()) return "Rp 0"
        val number = numberString.replace(".", "").toLongOrNull() ?: return "Rp 0"
        val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
            groupingSeparator = '.'
        }
        val decimalFormat = DecimalFormat("Rp #,###", symbols)
        return decimalFormat.format(number)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PencatatanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_pencatatan, parent, false)
        return PencatatanViewHolder(view)
    }

    override fun getItemCount(): Int {
        return pencatatanList.size
    }

    @SuppressLint("SetTextI18n")
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
        holder.harga.text = formatRupiah(data.hargaJual)
        holder.jumlah.text = "-${data.jumlah} Kg"
        holder.jumlah.setTextColor(context.resources.getColor(R.color.red))
        holder.tanggal.text = data.tanggal


        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailPencatatanKeluarActivity::class.java)
            intent.putExtra("keluarId", data.id)
            intent.putExtra("barId", data.barId)
            intent.putExtra("barangId", data.barangId)
            intent.putExtra("catId", data.catId)
            intent.putExtra("namaPetani", data.namaPetani)
            intent.putExtra("hargaJual", data.hargaJual)
            intent.putExtra("jumlah", data.jumlah)
            intent.putExtra("tanggal", data.tanggal)
            intent.putExtra("catatan", data.catatan)
            intent.putExtra("gambar", data.gambar)
            context.startActivity(intent)
        }
    }
}
