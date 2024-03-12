package com.tugasakhir.udmrputra.ui.mitra

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Mitra

class DaftarMitra : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MitraAdapter
    private var mitraList = arrayListOf<Mitra>(
        Mitra(1, "Mitra 1", "Lokasi 1"),
        Mitra(2, "Mitra 2", "Lokasi 2"),
        Mitra(3, "Mitra 3", "Lokasi 3"),
        // Tambahkan lebih banyak data Mitra jika diperlukan
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_mitra)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MitraAdapter(mitraList)
        recyclerView.adapter = adapter
    }
}