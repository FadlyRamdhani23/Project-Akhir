package com.tugasakhir.udmrputra.ui.mitra

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Mitra
import com.tugasakhir.udmrputra.data.Users
import com.tugasakhir.udmrputra.databinding.ActivityDaftarMitraBinding
import com.tugasakhir.udmrputra.ui.logreg.RegisterMitraActivity

class DaftarMitra : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MitraAdapter
    private lateinit var binding: ActivityDaftarMitraBinding
    private var mitraList = arrayListOf<Mitra>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDaftarMitraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MitraAdapter(mitraList)
        recyclerView.adapter = adapter

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("status", "mitra")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val userId = document.id
                    val nama = document.getString("nama") ?: ""
                    val noHp = document.getString("noHp") ?: ""
                    val status = document.getString("status") ?: ""

                    val user = Mitra(userId, nama, noHp, status)
                    mitraList.add(user)
                    Log.d("DaftarMitra", "Data berhasil ditambahkan: $user")
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("DaftarMitra", "Error getting documents: ", exception)
            }

        binding.fab.setOnClickListener {
            val intent = Intent(this, RegisterMitraActivity::class.java)
            startActivity(intent)
        }
    }
}
