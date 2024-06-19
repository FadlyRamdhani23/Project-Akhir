package com.tugasakhir.udmrputra.ui.barang

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Barang
import com.tugasakhir.udmrputra.data.Pencatatan
import com.tugasakhir.udmrputra.ui.ui.main.SectionsPagerAdapter
import com.tugasakhir.udmrputra.databinding.ActivityBarangBinding
import com.tugasakhir.udmrputra.ui.pengajuan.ActivityPengajuan
import com.tugasakhir.udmrputra.ui.pengiriman.SupirActivity

class BarangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBarangBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BarangAdapter
//    private var barangList = arrayListOf<Barang>(
//        Barang(1, "Tomat", R.drawable.tomato),
//        Barang(2, "Timun", R.drawable.timun),
//        Barang(3, "Cabai", R.drawable.cabai),
//        Barang(4, "Barang 4", R.drawable.tomato),
//        Barang(5, "Barang 5", R.drawable.tomato),
//        Barang(6, "Barang 6", R.drawable.tomato),
//        Barang(7, "Barang 7", R.drawable.tomato),
//        Barang(8, "Barang 8", R.drawable.tomato),
//        Barang(9, "Barang 9", R.drawable.tomato),
//        // Tambahkan lebih banyak data Barang jika diperlukan
//    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar: Toolbar = findViewById(R.id.topAppBarr)
        setSupportActionBar(toolbar)
//        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
//        val viewPager: ViewPager = binding.viewPager
//        viewPager.adapter = sectionsPagerAdapter
//        val tabs: TabLayout = binding.tabs
//        tabs.setupWithViewPager(viewPager)

        catRecyclerView()

//        recyclerView = findViewById(R.id.rvBarang)
//        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        adapter = BarangAdapter(this, barangList)
//        recyclerView.adapter = adapter
        binding.fabAddBarang.setOnClickListener {
            val intent = Intent(this, InputBarangActivity::class.java)
            startActivity(intent)
        }

    }

//    private fun barangRecyclerView() {
//        val barangList = arrayListOf<Barang>()
//        val db = FirebaseFirestore.getInstance()
//
//        recyclerView = findViewById(R.id.)
//        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        adapter = BarangAdapter(this, barangList)
//    }

    private fun catRecyclerView() {
        val barangList = arrayListOf<Barang>()
        val db = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.rvBarang)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = BarangAdapter(this, barangList)


        db.collection("barang")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {

                    val imageUrls = document.get("gambar") as String?
                    Log.d("gambar", "imageUrls: $imageUrls")

                    val data = Barang(
                        document.id,
                        document.data["nama"].toString(),
                        document.data["jenis"].toString(),
                        document.data["jumlah"].toString(),
                        document.data["catatan"].toString(),
                         imageUrls.toString()
                    )
                    barangList.add(data)

                }
                recyclerView.adapter = adapter
                Log.d("BarangActivity", "Data berhasil ditampilkan")
                Log.d("BarangActivity", "Data: $barangList")

            }
            .addOnFailureListener { exception ->
                Log.w("BarangActivity", "Data gagal ditampilkan")
            }



    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.fab_options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_open_other_activity -> {
                // Ganti OtherActivity dengan nama Activity yang ingin Anda buka
                val intent = Intent(this, SupirActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}