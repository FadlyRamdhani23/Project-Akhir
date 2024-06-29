package com.tugasakhir.udmrputra.ui.mitra

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.data.Pengajuan
import com.tugasakhir.udmrputra.databinding.ActivityPesananMitraBinding
import java.text.SimpleDateFormat
import java.util.*

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
    }

    private fun setupRecyclerView() {
        adapter = PesananMitraAdapter(filteredList)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@PesananMitraActivity.adapter
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
                    it.tanggalPengajuan in startOfWeek..endOfWeek
                })
            }
            "Bulan Ini" -> {
                currentDate.set(Calendar.DAY_OF_MONTH, 1)
                val startOfMonth = dateFormat.format(currentDate.time)
                currentDate.add(Calendar.MONTH, 1)
                currentDate.add(Calendar.DAY_OF_MONTH, -1)
                val endOfMonth = dateFormat.format(currentDate.time)
                filteredList.addAll(pesananList.filter {
                    it.tanggalPengajuan in startOfMonth..endOfMonth
                })
            }
        }

        adapter.notifyDataSetChanged()
    }

    private fun fetchPengajuanData() {
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid ?: ""
        db.collection("pengajuan").whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
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

                    db.collection("pengajuan").document(pengajuanId).collection("barang")
                        .get()
                        .addOnSuccessListener { barangResult ->
                            val listBarang = mutableListOf<String>()
                            for (barangDocument in barangResult) {
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
                                idPengiriman
                            )
                            pesananList.add(pengajuan)
                            filterByDate(binding.spinnerFilter.selectedItem.toString())
                        }
                }
            }
    }
}
