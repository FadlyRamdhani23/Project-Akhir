package com.tugasakhir.udmrputra.ui.barang

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
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class DetailPencatatanKeluarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailPencatatanBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPencatatanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        // Initialize Firebase Firestore
        firestore = Firebase.firestore

        // Retrieve data from intent
        val barangId = intent.getStringExtra("barangId") ?: ""
        val barId = intent.getStringExtra("barId") ?: ""
        val masukId = intent.getStringExtra("keluarId") ?: ""
        Log.d("DetailPencatatanActivity", "masukId: $masukId")
        val namaPetani = intent.getStringExtra("namaPetani")
        val jumlah = intent.getStringExtra("jumlah")
        val hargaBeli = intent.getStringExtra("hargaJual")
        val tanggal = intent.getStringExtra("tanggal")
        val catatan = intent.getStringExtra("catatan")
        val catId = intent.getStringExtra("catId")

        getImageSlider(barId)

        if (barangId.isNotEmpty() && masukId.isNotEmpty()) {
            binding.pctKategori.text = if (catId == "JV9d40TfUWOHoyg8i5Wt") {
                "Buah"
            } else {
                "Sayur"
            }
            binding.pctNama.text = barangId
            binding.pctPetani.text = namaPetani
            binding.pctJumlah.text = jumlah
            binding.pctHarga.text = formatRupiah(hargaBeli)
            binding.pctTanggal.text = tanggal
            binding.pctCatatan.text = catatan
        }
    }

    private fun getImageSlider(barangId: String) {
        val imageSlider = binding.ImageSlider
        val imageList = ArrayList<SlideModel>()

        firestore.collection("barang").document(barangId).get()
            .addOnSuccessListener { result ->
                val imageUrls = result.get("gambar") as? List<String>
                if (imageUrls != null && imageUrls.isNotEmpty()) {
                    for (imageUrl in imageUrls) {
                        imageList.add(SlideModel(imageUrl, ScaleTypes.CENTER_CROP))
                    }
                }
                imageSlider.setImageList(imageList)

                Log.d("DetailPencatatanActivity", "Gambar Berhasil didapatkan: $imageList")
            }
            .addOnFailureListener { exception ->
                Log.e("DetailPencatatanActivity", "Error getting images: ", exception)
            }
    }

    private fun formatRupiah(numberString: String?): String {
        if (numberString.isNullOrEmpty()) return "Rp 0"
        val number = numberString.replace(".", "").toLongOrNull() ?: return "Rp 0"
        val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
            groupingSeparator = '.'
        }
        val decimalFormat = DecimalFormat("Rp #,###", symbols)
        return decimalFormat.format(number)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
