package com.tugasakhir.udmrputra.ui.barang

import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Barang
import com.tugasakhir.udmrputra.ui.ui.main.SectionsPagerAdapter
import com.tugasakhir.udmrputra.databinding.ActivityBarangBinding
import com.tugasakhir.udmrputra.ui.pengajuan.ActivityPengajuan

class BarangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBarangBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BarangAdapter
    private var barangList = arrayListOf<Barang>(
        Barang(1, "Barang 1"),
        Barang(2, "Barang 2"),
        Barang(3, "Barang 3"),
        Barang(4, "Barang 4"),
        Barang(5, "Barang 5"),
        Barang(6, "Barang 6"),
        Barang(7, "Barang 7"),
        Barang(8, "Barang 8"),
        Barang(9, "Barang 9"),
        // Tambahkan lebih banyak data Barang jika diperlukan
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)

        recyclerView = findViewById(R.id.rvBarang)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = BarangAdapter(this, barangList)
        recyclerView.adapter = adapter
        binding.fabAddBarang.setOnClickListener {
            val intent = Intent(this, InputBarangActivity::class.java)
            startActivity(intent)
        }
    }
}