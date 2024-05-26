package com.tugasakhir.udmrputra.ui.dashboard.sopir

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Sopir
import com.tugasakhir.udmrputra.ui.sopir.SopirAdapter

class DaftarSopir : AppCompatActivity(){

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SopirAdapter
    private var sopirList = arrayListOf<Sopir>(
            Sopir(1, "Apendi", "Z 2433 EG"),
            Sopir(2, "Agus", "Z 1767 DG"),
            Sopir(3, "Dadan", "Z 4457 BH"),

        )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_sopir)

        recyclerView = findViewById(R.id.recyclerViewSopir)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SopirAdapter(sopirList)
        recyclerView.adapter = adapter
    }


}