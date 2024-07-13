package com.tugasakhir.udmrputra.ui.barang

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Barang
import com.tugasakhir.udmrputra.databinding.ActivityBarangBinding
import com.tugasakhir.udmrputra.ui.ui.main.SectionsPagerAdapter

class BarangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBarangBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BarangAdapter
    private lateinit var progressBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

        progressBar = findViewById(R.id.loadingProgressBar)

        catRecyclerView()

        binding.fabAddBarang.setOnClickListener {
            val intent = Intent(this, InputBarangActivity::class.java)
            startActivity(intent)
        }


    }
    private fun setupToolbar() {
        setSupportActionBar(binding.topAppBarr)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun catRecyclerView() {
        val barangList = arrayListOf<Barang>()
        val db = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.rvBarang)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = BarangAdapter(this, barangList)
        recyclerView.adapter = adapter

        progressBar.visibility = View.VISIBLE

        db.collection("barang")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val barangId = document.id
                    val catId = document.data["catId"].toString()
                    val namaBarang = document.data["nama"].toString()
                    val jumlahBarang = document.data["jumlah"].toString()

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
                            adapter.notifyDataSetChanged()
                            Log.d("BarangActivity", "Data berhasil ditambahkan: $data")
                            progressBar.visibility = View.GONE
                        }
                        .addOnFailureListener { e ->
                            Log.w("BarangActivity", "Error mendapatkan nama kategori", e)
                            progressBar.visibility = View.GONE
                        }
                }
                recyclerView.adapter = adapter
                Log.d("BarangActivity", "Data berhasil ditampilkan")
                Log.d("BarangActivity", "Data: $barangList")
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Log.w("BarangActivity", "Data gagal ditampilkan", exception)
                progressBar.visibility = View.GONE
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.fab_options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_open_other_activity -> {
                val intent = Intent(this, StockBarangActivity::class.java)
                startActivity(intent)
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
