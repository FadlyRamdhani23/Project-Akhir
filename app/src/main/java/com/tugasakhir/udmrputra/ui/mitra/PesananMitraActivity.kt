package com.tugasakhir.udmrputra.ui.mitra


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.midtrans.sdk.uikit.api.model.TransactionResult
import com.midtrans.sdk.uikit.internal.util.UiKitConstants
import com.tugasakhir.udmrputra.data.Pengajuan
import com.tugasakhir.udmrputra.databinding.ActivityPesananMitraBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class PesananMitraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPesananMitraBinding
    private lateinit var adapter: PesananMitraAdapter
    private val pesananList = mutableListOf<Pengajuan>()
    private val filteredList = mutableListOf<Pengajuan>()
    private val auth = FirebaseAuth.getInstance()

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
                        "Transaction Pending. ID: ${it.message}"
                    }
                    UiKitConstants.STATUS_FAILED -> "Transaction Failed. ID: ${it.transactionId}"
                    UiKitConstants.STATUS_CANCELED -> "Transaction Cancelled"
                    UiKitConstants.STATUS_INVALID -> "Transaction Invalid. ID: ${it.transactionId}"
                    else -> "Transaction ID: ${it.transactionId}. Message: ${it.status}"
                }
                Log.d("TransactionStatus", message)
            } ?: run {
                Log.e("TransactionStatus", "Transaction Invalid")
            }
        }
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
                    val tanggalPengajuanTimestamp = document.get("tanggalPengajuan") // Read without casting
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

                            // Convert Timestamp to String including time
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                            val tanggalPengajuan = if (tanggalPengajuanTimestamp is com.google.firebase.Timestamp) {
                                dateFormat.format(tanggalPengajuanTimestamp.toDate())
                            } else {
                                // Handle the case where tanggalPengajuan is not a Timestamp
                                ""
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

                            // Sort pesananList by tanggalPengajuan in descending order
                            pesananList.sortByDescending { dateFormat.parse(it.tanggalPengajuan) }

                            filterByDate(binding.spinnerFilter.selectedItem.toString())
                        }
                }
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterByDate(filter: String) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val currentDate = Calendar.getInstance()
        filteredList.clear()

        when (filter) {
            "Semua" -> filteredList.addAll(pesananList)
            "Hari Ini" -> {
                val startOfDay = currentDate.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.time
                val endOfDay = currentDate.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.time
                filteredList.addAll(pesananList.filter {
                    val pengajuanDate = dateFormat.parse(it.tanggalPengajuan)
                    pengajuanDate!! >= startOfDay && pengajuanDate <= endOfDay
                })
            }
            "Minggu Ini" -> {
                currentDate.set(Calendar.DAY_OF_WEEK, currentDate.firstDayOfWeek)
                val startOfWeek = currentDate.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.time
                currentDate.add(Calendar.DAY_OF_WEEK, 6)
                val endOfWeek = currentDate.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.time
                filteredList.addAll(pesananList.filter {
                    val pengajuanDate = dateFormat.parse(it.tanggalPengajuan)
                    pengajuanDate!! >= startOfWeek && pengajuanDate <= endOfWeek
                })
            }
            "Bulan Ini" -> {
                currentDate.set(Calendar.DAY_OF_MONTH, 1)
                val startOfMonth = currentDate.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.time
                currentDate.add(Calendar.MONTH, 1)
                currentDate.add(Calendar.DAY_OF_MONTH, -1)
                val endOfMonth = currentDate.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.time
                filteredList.addAll(pesananList.filter {
                    val pengajuanDate = dateFormat.parse(it.tanggalPengajuan)
                    pengajuanDate!! >= startOfMonth && pengajuanDate <= endOfMonth
                })
            }
        }

        // Sort filteredList by tanggalPengajuan in descending order
        filteredList.sortByDescending { dateFormat.parse(it.tanggalPengajuan) }

        adapter.notifyDataSetChanged()
        checkIfEmpty()
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
