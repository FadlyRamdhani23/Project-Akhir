package com.tugasakhir.udmrputra.ui.dashboard.sopir

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Mitra
import com.tugasakhir.udmrputra.data.Sopir
import com.tugasakhir.udmrputra.databinding.ActivityDaftarSopirBinding
import com.tugasakhir.udmrputra.ui.logreg.RegisterSupirActivity
import com.tugasakhir.udmrputra.ui.sopir.SopirAdapter

class DaftarSopir : AppCompatActivity(){

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SopirAdapter

    private lateinit var binding : ActivityDaftarSopirBinding
    private var supirList = arrayListOf<Sopir>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDaftarSopirBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()

        recyclerView = findViewById(R.id.recyclerViewSopir)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("status", "supir")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val userId = document.id
                    val nama = document.getString("nama") ?: ""
                    val noHp = document.getString("noHp") ?: ""
                    val status = document.getString("status") ?: ""

                    val sopir = Sopir(userId, nama, noHp, status)
                    supirList.add(sopir)
                    Log.d("DaftarMitra", "Data berhasil ditambahkan: $sopir")
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("DaftarMitra", "Error getting documents: ", exception)
            }
        adapter = SopirAdapter(supirList)
        recyclerView.adapter = adapter

        binding.fabAddSopir.setOnClickListener {
            val intent = Intent(this, RegisterSupirActivity::class.java)
            startActivity(intent)
        }
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