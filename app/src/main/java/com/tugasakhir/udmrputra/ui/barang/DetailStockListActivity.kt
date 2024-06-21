package com.tugasakhir.udmrputra.ui.barang

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.databinding.ActivityDetailListBinding

class DetailStockListActivity : AppCompatActivity(){

    private lateinit var binding: ActivityDetailListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val barangId = intent.getStringExtra("barangId")

        val db = FirebaseFirestore.getInstance()

        db.collection("barang")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (document.id == barangId) {
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

                                binding.listNama.text = namaBarang
                                binding.listJumlah.text = "${jumlahBarang} Kg"
                                binding.listJenis.text = catName
                                Glide.with(this)
                                    .load(imageUrls.getOrNull(0))
                                    .into(binding.listGambar)
                            }
                            .addOnFailureListener { e ->
                                Log.w("DetailStockListActivity", "Error mendapatkan nama kategori", e)
                            }
                    }
                }
            }


    }
}

