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
                fetchTransactionStatusByOrderId(pengajuanId)
            }
        } else {
            Toast.makeText(this, "Order ID hilang", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchTransactionStatusByOrderId(orderId: String) {
        ApiClient.instance.getTransactionStatusByOrderId(orderId)
            .enqueue(object : Callback<TransactionStatus> {
                override fun onResponse(
                    call: Call<TransactionStatus>,
                    response: Response<TransactionStatus>
                ) {
                    if (response.isSuccessful) {
                        val transactionStatus = response.body()
                        if (transactionStatus != null) {
                            val status = transactionStatus.transaction_status
                            binding.tvStatus.text = status
                            if (transactionStatus.transaction_status == "settlement") {
                                sendNotification(
                                    "Transaksi Berhasil",
                                    "Transaksi dengan ID Pesanan $orderId telah berhasil diselesaikan"
                                )
                                updateStatus(orderId, "approved")

//                                val intent = Intent(this@DetailPembayaran, PesananMitraActivity::class.java)
//                                startActivity(intent)
//                                finish()
                            } else if (transactionStatus.transaction_status == "pending") {
                                sendNotification(
                                    "Transaksi Tertunda",
                                    "Transaksi dengan ID Pesanan $orderId masih tertunda"
                                )
                            } else if (transactionStatus.transaction_status == "cancel") {
                                sendNotification(
                                    "Transaksi Dibatalkan",
                                    "Transaksi dengan ID Pesanan $orderId telah dibatalkan"
                                )
                            } else if (transactionStatus.transaction_status == "deny") {
                                sendNotification(
                                    "Transaksi Ditolak",
                                    "Transaksi dengan ID Pesanan $orderId telah ditolak"
                                )
                            } else if (transactionStatus.transaction_status == "expire") {
                                sendNotification(
                                    "Transaksi Kedaluwarsa",
                                    "Transaksi dengan ID Pesanan $orderId telah kedaluwarsa"
                                )
                            } else if (transactionStatus.transaction_status == "refund") {
                                sendNotification(
                                    "Transaksi Dikembalikan",
                                    "Transaksi dengan ID Pesanan $orderId telah dikembalikan"
                                )
                            } else if (transactionStatus.transaction_status == "capture") {
                                sendNotification(
                                    "Transaksi Ditangkap",
                                    "Transaksi dengan ID Pesanan $orderId telah ditangkap"
                                )
                            } else if (transactionStatus.transaction_status == "authorize") {
                                sendNotification(
                                    "Transaksi Diotorisasi",
                                    "Transaksi dengan ID Pesanan $orderId telah diotorisasi"
                                )
                            } else if (transactionStatus.transaction_status == "void") {
                                sendNotification(
                                    "Transaksi Dibatalkan",
                                    "Transaksi dengan ID Pesanan $orderId telah dibatalkan"
                                )
                            } else {
                                Toast.makeText(
                                    this@DetailPembayaran,
                                    "Status transaksi tidak diketahui",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        } else {
                            Toast.makeText(
                                this@DetailPembayaran,
                                "Failed to fetch transaction status",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@DetailPembayaran,
                            "Failed to fetch transaction status",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<TransactionStatus>, t: Throwable) {
                    Toast.makeText(this@DetailPembayaran, "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
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

//                        if (status == "settlement") {
//                            binding.llVirtualAccountNumber.visibility = android.view.View.GONE
//                            binding.cekPembayaran.isEnabled = false
//                        }

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

    private fun updateStatus(pengajuanId: String, status: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pengajuan").document(pengajuanId)
            .update("status", status)
            .addOnSuccessListener {
            Log.d("DetailPembayaran", "Status updated to $status")
            }
            .addOnFailureListener {
                Log.e("DetailPembayaran", "Error updating status", it)
            }
    }


    private fun sendNotification(title: String, message: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "transaction_status_channel"
        val channelName = "Transaction Status"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo4)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(0, notification)
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
