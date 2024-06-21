package com.tugasakhir.udmrputra.ui.barang

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Barang
import com.tugasakhir.udmrputra.databinding.ActivityStockBarangBinding

class StockBarangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStockBarangBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BarangListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStockBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        catListRecyclerView()
    }

    private fun catListRecyclerView() {
        val barangList = arrayListOf<Barang>()
        val db = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.rvBarangList)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = BarangListAdapter(this, barangList)
        recyclerView.adapter = adapter

        db.collection("barang")
            .get()
            .addOnSuccessListener { result ->
                var pendingTasks = result.size()
                for (document in result) {
                    val barangId = document.id
                    val catId = document.data["catId"].toString()
                    val namaBarang = document.data["nama"].toString()
                    val jumlahBarang = document.data["jumlah"].toString()

                    // Pengecekan tipe sebelum casting
                    val imageUrls = if (document.get("gambar") is List<*>) {
                        (document.get("gambar") as? List<String>) ?: emptyList()
                    } else {
                        emptyList()
                    }
                    Log.d("gambar", "imageUrls: $imageUrls")

                    db.collection("kategori").document(catId)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            val catName = documentSnapshot.getString("nama").toString()

                            val data = Barang(
                                barangId,
                                namaBarang,
                                catName,
                                jumlahBarang,
                                imageUrls.getOrNull(0).toString(),
                            )
                            barangList.add(data)
                            Log.d("StockBarangActivity", "Data berhasil ditambahkan: $data")

                            // Update adapter setelah semua data selesai diproses
                            pendingTasks -= 1
                            if (pendingTasks == 0) {
                                adapter.notifyDataSetChanged()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w("StockBarangActivity", "Error mendapatkan nama kategori", e)

                            // Handle pending tasks on failure as well
                            pendingTasks -= 1
                            if (pendingTasks == 0) {
                                adapter.notifyDataSetChanged()
                            }
                        }
                }
                Log.d("StockBarangActivity", "Data berhasil ditampilkan")
                Log.d("StockBarangActivity", "Data: $barangList")
            }
            .addOnFailureListener { exception ->
                Log.w("StockBarangActivity", "Data gagal ditampilkan", exception)
            }
    }
}
