package com.tugasakhir.udmrputra.ui.barang

import ImageSliderAdapter
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tugasakhir.udmrputra.databinding.ActivityDetailPencatatanBinding

class DetailPencatatanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailPencatatanBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPencatatanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Firestore
        firestore = Firebase.firestore

       getImageSlider()

        // Retrieve data from intent
        val barangId = intent.getStringExtra("barangId")
        val masukId = intent.getStringExtra("masukId")
        val namaPetani = intent.getStringExtra("namaPetani")
        val jumlah = intent.getStringExtra("jumlah")
        val hargaBeli = intent.getStringExtra("hargaBeli")
        val tanggal = intent.getStringExtra("tanggal")
        val catatan = intent.getStringExtra("catatan")
        val catId = intent.getStringExtra("catId")

//        val imageUrls = intent.getStringArrayListExtra("gambar")
//
//        val imageList = ArrayList<SlideModel>()
//
//        imageUrls?.let {
//            for (imageUrl in it) {
//                imageList.add(SlideModel(imageUrl, ScaleTypes.CENTER_CROP))
//            }
//        }
//
//        // Set up the image slider
//        imageSlider.setImageList(imageList)

        if (barangId != null && masukId != null) {
            binding.pctKategori.text = if (catId == "JV9d40TfUWOHoyg8i5Wt") {
                "Buah"
            } else {
                "Sayur"
            }
            binding.pctNama.text = barangId
            binding.pctPetani.text = namaPetani
            binding.pctJumlah.text = jumlah.toString()
            binding.pctHarga.text = hargaBeli
            binding.pctTanggal.text = tanggal
            binding.pctCatatan.text = catatan
        }
    }

    private fun getImageSlider(){
        val imageSlider = binding.ImageSlider
        val imageList = ArrayList<SlideModel>()
        firestore.collection("masuk").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val imageUrls = document.get("gambar") as? List<String>
                    if (imageUrls != null && imageUrls.isNotEmpty()) {
                        for (imageUrl in imageUrls) {
                            imageList.add(SlideModel(imageUrl, ScaleTypes.CENTER_CROP))
                        }
                    }
                }
                imageSlider.setImageList(imageList)

                Log.d("DetailPencatatanActivity", "Gambar Berhasil didapatkan: $imageList")
            }
            .addOnFailureListener { exception ->
                // Handle any errors
            }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
