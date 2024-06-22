package com.tugasakhir.udmrputra.ui.barang

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Barang
import com.tugasakhir.udmrputra.databinding.ActivityDetailListBinding
import com.tugasakhir.udmrputra.ui.home.HomeFragment
import com.tugasakhir.udmrputra.ui.ui.main.SectionsPagerAdapter2

class DetailStockListActivity : AppCompatActivity(){

    private lateinit var binding: ActivityDetailListBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter2(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager2
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs2
        tabs.setupWithViewPager(viewPager)

        val  topBar = binding.topAppBar
        topBar.setNavigationOnClickListener {
            val intent = Intent(this, HomeFragment::class.java)
            startActivity(intent)
            finish()
        }

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
                                binding.listJumlah.text = "${jumlahBarang} /Kg"
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

