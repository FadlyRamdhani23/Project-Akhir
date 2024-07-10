package com.tugasakhir.udmrputra.ui.mitra

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.midtrans.sdk.uikit.api.model.TransactionResult
import com.midtrans.sdk.uikit.internal.util.UiKitConstants
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Pengajuan
import com.tugasakhir.udmrputra.data.TransactionStatus
import com.tugasakhir.udmrputra.databinding.ActivityPesananMitraBinding
import com.tugasakhir.udmrputra.ui.service.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class PesananMitraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPesananMitraBinding
    private lateinit var adapter: PesananMitraAdapter
    private val pesananList = mutableListOf<Pengajuan>()
    private val filteredList = mutableListOf<Pengajuan>()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var paymentLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPesananMitraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSpinner()
        fetchPengajuanData()



        FirebaseApp.initializeApp(this)

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String> ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }
                // Get new FCM registration token
                val token = task.result
                Log.d("FCM", "Token: $token")
            }
    }

    private fun setupRecyclerView() {
        val launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result?.resultCode == RESULT_OK) {
                    result.data?.let {
                        val transactionResult = it.getParcelableExtra<TransactionResult>(UiKitConstants.KEY_TRANSACTION_RESULT)
                        Toast.makeText(this, "${transactionResult?.transactionId}", Toast.LENGTH_LONG).show()
                    }
                }
            }

        adapter = PesananMitraAdapter(filteredList, this, launcher) // Pass the Activity context here
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@PesananMitraActivity.adapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            val transactionResult = data.getParcelableExtra<TransactionResult>(UiKitConstants.KEY_TRANSACTION_RESULT)
            transactionResult?.let {
                val message = when (it.status) {
                    UiKitConstants.STATUS_SUCCESS -> {
                        "Transaction Finished. ID: ${it.transactionId}"
                    }
                    UiKitConstants.STATUS_PENDING -> {
                        "Transaction Finished. ID: ${it.message}"
                        fetchTransactionStatusByTransactionId(it.transactionId.toString())
                    }
                    UiKitConstants.STATUS_FAILED -> "Transaction Failed. ID: ${it.transactionId}"
                    UiKitConstants.STATUS_CANCELED -> "Transaction Cancelled"
                    UiKitConstants.STATUS_INVALID -> "Transaction Invalid. ID: ${it.transactionId}"
                    else -> "Transaction ID: ${it.transactionId}. Message: ${it.status}"
                }
            } ?: run {
                Toast.makeText(this, "Transaction Invalid", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun fetchTransactionStatusByTransactionId(transactionId: String) {
        ApiClient.instance.getTransactionStatusByTransactionId(transactionId).enqueue(object :
            Callback<TransactionStatus> {
            override fun onResponse(call: Call<TransactionStatus>, response: Response<TransactionStatus>) {
                if (response.isSuccessful) {
                    val transactionStatus = response.body()
                    if (transactionStatus != null) {
                        if (transactionStatus.transaction_status == "settlement") {
                            sendNotification(
                                "Transaksi Berhasil",
                                "Transaksi dengan ID Pesanan $transactionId telah berhasil diselesaikan"
                            )
                        } else if (transactionStatus.transaction_status == "pending") {
                            sendNotification(
                                "Transaksi Tertunda",
                                "Transaksi dengan ID Pesanan $transactionId masih tertunda"
                            )
                        } else if (transactionStatus.transaction_status == "cancel") {
                            sendNotification(
                                "Transaksi Dibatalkan",
                                "Transaksi dengan ID Pesanan $transactionId telah dibatalkan"
                            )
                        } else if (transactionStatus.transaction_status == "deny") {
                            sendNotification(
                                "Transaksi Ditolak",
                                "Transaksi dengan ID Pesanan $transactionId telah ditolak"
                            )
                        } else if (transactionStatus.transaction_status == "expire") {
                            sendNotification(
                                "Transaksi Kedaluwarsa",
                                "Transaksi dengan ID Pesanan $transactionId telah kedaluwarsa"
                            )
                        } else if (transactionStatus.transaction_status == "refund") {
                            sendNotification(
                                "Transaksi Dikembalikan",
                                "Transaksi dengan ID Pesanan $transactionId telah dikembalikan"
                            )
                        } else if (transactionStatus.transaction_status == "capture") {
                            sendNotification(
                                "Transaksi Ditangkap",
                                "Transaksi dengan ID Pesanan $transactionId telah ditangkap"
                            )
                        } else if (transactionStatus.transaction_status == "authorize") {
                            sendNotification(
                                "Transaksi Diotorisasi",
                                "Transaksi dengan ID Pesanan $transactionId telah diotorisasi"
                            )
                        } else if (transactionStatus.transaction_status == "void") {
                            sendNotification(
                                "Transaksi Dibatalkan",
                                "Transaksi dengan ID Pesanan $transactionId telah dibatalkan"
                            )
                        } else {
                            Toast.makeText(
                                this@PesananMitraActivity,
                                "Status transaksi tidak diketahui",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        Toast.makeText(
                            this@PesananMitraActivity,
                            "Failed to fetch transaction status",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@PesananMitraActivity,
                        "Failed to fetch transaction status",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<TransactionStatus>, t: Throwable) {
                Toast.makeText(this@PesananMitraActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "transaction_status_channel"
        val channelName = "Transaction Status"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
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


    private fun setupSpinner() {
        val options = arrayOf("Semua", "Hari Ini", "Minggu Ini", "Bulan Ini")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilter.adapter = spinnerAdapter

        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterByDate(options[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun filterByDate(filter: String) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = Calendar.getInstance()
        filteredList.clear()

        when (filter) {
            "Semua" -> filteredList.addAll(pesananList)
            "Hari Ini" -> {
                val today = dateFormat.format(currentDate.time)
                filteredList.addAll(pesananList.filter { it.tanggalPengajuan == today })
            }
            "Minggu Ini" -> {
                currentDate.set(Calendar.DAY_OF_WEEK, currentDate.firstDayOfWeek)
                val startOfWeek = dateFormat.format(currentDate.time)
                currentDate.add(Calendar.DAY_OF_WEEK, 6)
                val endOfWeek = dateFormat.format(currentDate.time)
                filteredList.addAll(pesananList.filter {
                    val date = dateFormat.parse(it.tanggalPengajuan)
                    date >= dateFormat.parse(startOfWeek) && date <= dateFormat.parse(endOfWeek)
                })
            }
            "Bulan Ini" -> {
                currentDate.set(Calendar.DAY_OF_MONTH, 1)
                val startOfMonth = dateFormat.format(currentDate.time)
                currentDate.add(Calendar.MONTH, 1)
                currentDate.add(Calendar.DAY_OF_MONTH, -1)
                val endOfMonth = dateFormat.format(currentDate.time)
                filteredList.addAll(pesananList.filter {
                    val date = dateFormat.parse(it.tanggalPengajuan)
                    date >= dateFormat.parse(startOfMonth) && date <= dateFormat.parse(endOfMonth)
                })
            }
        }

        adapter.notifyDataSetChanged()
        checkIfEmpty()
    }

    private fun fetchPengajuanData() {
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid ?: return
        db.collection("pengajuan").whereEqualTo("userId", userId)
            .addSnapshotListener { result, error ->
                if (error != null) {
                    // Handle the error
                    return@addSnapshotListener
                }

                pesananList.clear()
                for (document in result!!) {
                    val pengajuanId = document.id
                    val namaPetani = document.getString("namaPetani") ?: ""
                    val tanggalPengajuan = document.getString("tanggalPengajuan") ?: ""
                    val barangAjuan = document.getString("barangAjuan") ?: ""
                    val jenisPembayaran = document.getString("jenisPembayaran") ?: ""
                    val statusPengajuan = document.getString("status") ?: ""
                    val address = document.getString("address") ?: ""
                    val latitude = document.getDouble("latitude") ?: 0.0
                    val longitude = document.getDouble("longitude") ?: 0.0
                    val idPengiriman = document.getString("idPengiriman") ?: ""
                    val totalHarga = document.getLong("totalHarga") ?: 0


                    db.collection("pengajuan").document(pengajuanId).collection("barang")
                        .addSnapshotListener { barangResult, error ->
                            if (error != null) {
                                // Handle the error
                                return@addSnapshotListener
                            }

                            val listBarang = mutableListOf<String>()
                            for (barangDocument in barangResult!!) {
                                val namaBarang = barangDocument.getString("namaBarang") ?: ""
                                listBarang.add(namaBarang)
                            }
                            val pengajuan = Pengajuan(
                                pengajuanId,
                                namaPetani,
                                tanggalPengajuan,
                                barangAjuan,
                                listBarang,
                                jenisPembayaran,
                                statusPengajuan,
                                address,
                                latitude,
                                longitude,
                                idPengiriman,
                                totalHarga
                            )
                            pesananList.add(pengajuan)
                            filterByDate(binding.spinnerFilter.selectedItem.toString())
                        }
                }
            }
    }

    private fun checkIfEmpty() {
        if (adapter.itemCount == 0) {
            binding.recyclerView.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
        }
    }
}
