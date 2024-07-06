package com.tugasakhir.udmrputra.ui.mitra

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Pengajuan
import com.tugasakhir.udmrputra.ui.pengajuan.DetailPengajuanActivity
import com.tugasakhir.udmrputra.ui.pengiriman.MapsActivity

class PesananMitraAdapter(private val pengajuanList: List<Pengajuan>) :
    RecyclerView.Adapter<PesananMitraAdapter.PesananViewHolder>() {

    class PesananViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tanggalPesanan: TextView = view.findViewById(R.id.tv_tanggal)
        val barang: TextView = view.findViewById(R.id.tv_item_nama)
        val status: TextView = view.findViewById(R.id.tv_status)
        val jumlahBarang: TextView = view.findViewById(R.id.tv_jumlah)
        val totalHarga: TextView = view.findViewById(R.id.tv_total_harga)
        val llTrackPengiriman: LinearLayout = view.findViewById(R.id.ll_track_pengiriman)
        val viewTrackPengiriman: View = view.findViewById(R.id.view_track_pengiriman)
        val btnTerima: TextView = view.findViewById(R.id.btn_terima)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PesananViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pesanan_mitra, parent, false)
        return PesananViewHolder(view)
    }

    override fun onBindViewHolder(holder: PesananViewHolder, position: Int) {
        val pengajuan = pengajuanList[position]
        holder.tanggalPesanan.text = pengajuan.tanggalPengajuan
        holder.barang.text = pengajuan.listBarang.joinToString(", ")
        holder.status.text = pengajuan.statusPengajuan
        holder.jumlahBarang.text = "${pengajuan.listBarang.size} Barang"
        holder.totalHarga.text = "Rp. 100.000"  // Anda mungkin ingin mengubah ini untuk mendapatkan data yang sebenarnya
        if (pengajuan.statusPengajuan == "pengiriman" || pengajuan.statusPengajuan == "dikemas") {
            holder.llTrackPengiriman.visibility = View.VISIBLE
            holder.viewTrackPengiriman.visibility = View.VISIBLE
        } else {
            holder.llTrackPengiriman.visibility = View.GONE
            holder.viewTrackPengiriman.visibility = View.GONE
        }

        holder.llTrackPengiriman.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, MapsActivity::class.java).apply {
                putExtra("PENGIRIMAN_ID", pengajuan.idPengiriman)
            }
            context.startActivity(intent)
        }

        when (pengajuan.statusPengajuan) {
            "pengiriman" -> {
                holder.btnTerima.text = "Terima"
                holder.btnTerima.isEnabled = true
                holder.btnTerima.visibility = View.VISIBLE
                holder.btnTerima.setOnClickListener {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("pengajuan").document(pengajuan.id)
                        .update("status", "selesai")
                        .addOnSuccessListener {
                            holder.status.text = "selesai"
                        }
                        .addOnFailureListener {
                            // Handle the error
                        }
                    db.collection("pengiriman").document(pengajuan.idPengiriman.toString())
                        .update("status", "selesai")
                        .addOnSuccessListener {
                            holder.status.text = "selesai"
                        }
                        .addOnFailureListener {
                            // Handle the error
                        }
                }
            }
            "Menunggu pembayaran" -> {
                holder.btnTerima.text = "Bayar"
                holder.btnTerima.isEnabled = true
                holder.btnTerima.visibility = View.VISIBLE
                holder.btnTerima.setOnClickListener {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("pengajuan").document(pengajuan.id)
                        .update("status", "selesai")
                        .addOnSuccessListener {
                            holder.status.text = "selesai"
                        }
                        .addOnFailureListener {
                            // Handle the error
                        }
                    db.collection("pengiriman").document(pengajuan.idPengiriman.toString())
                        .update("status", "selesai")
                        .addOnSuccessListener {
                            holder.status.text = "selesai"
                        }
                        .addOnFailureListener {
                            // Handle the error
                        }
                }
            }
            "selesai" -> {
                holder.btnTerima.text = "Selesai"
                holder.btnTerima.isEnabled = false
            }
            else -> {
                holder.btnTerima.visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailPesananMitra::class.java)
            intent.putExtra("pengajuanId", pengajuan.id) // Menambahkan ID Pengiriman ke Intent
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int {
        return pengajuanList.size
    }
}

