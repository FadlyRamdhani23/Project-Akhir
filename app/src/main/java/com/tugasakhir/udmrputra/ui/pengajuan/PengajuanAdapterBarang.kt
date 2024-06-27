package com.tugasakhir.udmrputra.ui.pengajuan

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
import com.tugasakhir.udmrputra.data.Barang
import com.tugasakhir.udmrputra.ui.barang.DetailStockListActivity
import com.tugasakhir.udmrputra.ui.barang.StockBarangActivity

class PengajuanAdapterBarang(
    private val context: Context,
    private var barangList: List<Barang>,
    private val onItemClick: (Barang) -> Unit
) : RecyclerView.Adapter<PengajuanAdapterBarang.BarangViewHolder>() {

    class BarangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.stock_nama)
        val gambar: ImageView = itemView.findViewById(R.id.stock_image)
        val jenis: TextView = itemView.findViewById(R.id.stock_jenis)
        val jumlah: TextView = itemView.findViewById(R.id.stock_jumlah)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarangViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.stock_item_list, parent, false)
        return BarangViewHolder(view)
    }

    override fun onBindViewHolder(holder: BarangViewHolder, position: Int) {
        val currentItem = barangList[position]
        holder.name.text = currentItem.name
        holder.jenis.text = currentItem.catId
        holder.jumlah.text = "${currentItem.jumlah} Kg"

        Glide.with(context)
            .load(currentItem.gambar)
            .circleCrop()
            .fitCenter()
            .into(holder.gambar)

        holder.itemView.setOnClickListener {
            onItemClick(currentItem)
        }
    }

    override fun getItemCount() = barangList.size

    fun updateList(newList: List<Barang>) {
        barangList = newList
        notifyDataSetChanged()
    }
}

