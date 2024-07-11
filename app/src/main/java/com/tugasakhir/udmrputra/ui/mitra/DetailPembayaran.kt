package com.tugasakhir.udmrputra.ui.mitra

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Pengajuan
import com.tugasakhir.udmrputra.data.TransactionStatus
import com.tugasakhir.udmrputra.databinding.ActivityDetailPembayaranBinding
import com.tugasakhir.udmrputra.ui.service.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailPembayaran : AppCompatActivity() {

    private lateinit var binding: ActivityDetailPembayaranBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPembayaranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pengajuanId = intent.getStringExtra("order_id")
        if (pengajuanId != null) {
            fetchPengajuanData(pengajuanId)
            binding.cekPembayaran.setOnClickListener {
                val intent = Intent(this@DetailPembayaran, PesananMitraActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            Toast.makeText(this, "Order ID hilang", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchPengajuanData(pengajuanId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("transaksi").whereEqualTo("order_id", pengajuanId)
            .addSnapshotListener { result, error ->
                if (error != null) {
                    Toast.makeText(this, "Gagal mengambil detail transaksi", Toast.LENGTH_SHORT)
                        .show()
                    return@addSnapshotListener
                }

                result?.let {
                    for (document in it.documents) {
                        val vaNumbers = document.get("va_numbers") as? List<Map<String, Any>>
                        val orderId = document.getString("order_id")
                        val status = document.getString("transaction_status")

                        if (status == "settlement") {
                            binding.llVirtualAccountNumber.visibility = android.view.View.GONE
                            binding.cekPembayaran.isEnabled = false
                        }

                        if (!vaNumbers.isNullOrEmpty()) {
                            val vaNumber = vaNumbers[0]["va_number"] as? String
                            if (vaNumber != null) {
                                binding.tvVirtualAccountNumber.text = vaNumber
                            }
                        }

                        if (orderId != null) {
                            binding.tvOrderId.text = orderId
                            binding.tvStatus.text = status
                        }
                    }

                    fetchPengajuanDetail(pengajuanId)
                }
            }
    }

    private fun fetchPengajuanDetail(pengajuanId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pengajuan").document(pengajuanId)
            .get()
            .addOnSuccessListener { document ->
                val totalHarga = document.getLong("totalHarga") ?: 0
                binding.tvAmount.text = "Rp $totalHarga"
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengambil detail pengajuan", Toast.LENGTH_SHORT).show()
            }
    }
}
