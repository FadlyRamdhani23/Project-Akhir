package com.tugasakhir.udmrputra.ui.barang

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Pencatatan
import com.tugasakhir.udmrputra.ui.pengajuan.ActivityPengajuan

class PencatatanAdapter (private val context: Context, private val pencatatanList: List<Pencatatan>) : RecyclerView.Adapter<PencatatanAdapter.PencatatanViewHolder>(){
    class PencatatanViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
        val jenis = itemView.findViewById<TextView>(R.id.jenis_barang)
        val namaBrg = itemView.findViewById<TextView>(R.id.barang_name)
        val namaMitra = itemView.findViewById<TextView>(R.id.mitra_name)
        val harga = itemView.findViewById<TextView>(R.id.harga_barang)
        val jumlah = itemView.findViewById<TextView>(R.id.jumlah_barang)
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
        holder.jenis.text = data.catId
        holder.namaBrg.text = data.barangId
        holder.namaMitra.text = data.namaPetani
        holder.harga.text = data.hargaBeli
        holder.jumlah.text = data.jumlah.toString() + " Kg"

        holder.itemView.setOnClickListener {
             val intent = Intent(context, ActivityPengajuan::class.java)
             context.startActivity(intent)
        }
    }


}