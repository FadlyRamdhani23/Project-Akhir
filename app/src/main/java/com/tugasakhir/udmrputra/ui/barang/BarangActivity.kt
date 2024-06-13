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
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
    private var barangList = arrayListOf<Barang>(
        Barang(1, "Tomat", R.drawable.tomato),
        Barang(2, "Timun", R.drawable.timun),
        Barang(3, "Cabai", R.drawable.cabai),
        Barang(4, "Barang 4", R.drawable.tomato),
        Barang(5, "Barang 5", R.drawable.tomato),
        Barang(6, "Barang 6", R.drawable.tomato),
        Barang(7, "Barang 7", R.drawable.tomato),
        Barang(8, "Barang 8", R.drawable.tomato),
        Barang(9, "Barang 9", R.drawable.tomato),
        // Tambahkan lebih banyak data Barang jika diperlukan
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar: Toolbar = findViewById(R.id.topAppBarr)
        setSupportActionBar(toolbar)
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