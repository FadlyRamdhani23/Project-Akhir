package com.tugasakhir.udmrputra.ui.mitra

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Mitra
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
        setupToolbar()
        fecthData()

        binding.fab.setOnClickListener {
            val intent = Intent(this, RegisterMitraActivity::class.java)
            inputBarangLauncher.launch(intent)
        }
    }

    private val inputBarangLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            fecthData()
        }
    }

    private fun fecthData(){
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("status", "mitra")
            .get()
            .addOnSuccessListener { result ->
                mitraList.clear()
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
