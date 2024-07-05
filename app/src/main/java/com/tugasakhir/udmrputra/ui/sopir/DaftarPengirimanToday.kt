package com.tugasakhir.udmrputra.ui.sopir

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.data.Pengiriman
import com.tugasakhir.udmrputra.databinding.ActivityDaftarPengirimanTodayBinding
import com.tugasakhir.udmrputra.ui.dashboard.PengirimanAdapter

class DaftarPengirimanToday : AppCompatActivity() {

    private lateinit var binding: ActivityDaftarPengirimanTodayBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth

    private lateinit var pengirimanAdapter: DaftarPengirimanAdapter
    private val pengirimanList = mutableListOf<Pengiriman>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDaftarPengirimanTodayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        pengirimanAdapter = DaftarPengirimanAdapter(pengirimanList)
        recyclerView.adapter = pengirimanAdapter

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val uId = user?.uid
        setupRecyclerView()
        if (uId != null) {
            fetchPengirimanData(uId)
        }
    }

    private fun fetchPengirimanData(supirId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pengiriman")
            .whereEqualTo("supirId", supirId)
            .whereEqualTo("status", "dikemas")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("wow", "Listen failed", e)
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    pengirimanList.clear()
                    for (document in snapshots) {
                        val pengirimanId = document.id
                        val address = document.getString("address") ?: ""
                        val latitudeTujuan = document.getDouble("latitudeTujuan") ?: 0.0
                        val longitudeTujuan = document.getDouble("longitudeTujuan") ?: 0.0
                        val latitudeSupir = document.getDouble("latitudeSupir") ?: 0.0
                        val longitudeSupir = document.getDouble("longitudeSupir") ?: 0.0
                        val supir = document.getString("supir") ?: ""
                        val status = document.getString("status") ?: ""

                        val pengiriman = Pengiriman(
                            pengirimanId,
                            latitudeTujuan,
                            longitudeTujuan,
                            latitudeSupir,
                            longitudeSupir,
                            supir,
                            supirId,
                            address,
                            status
                        )
                        pengirimanList.add(pengiriman)
                    }
                    pengirimanAdapter.notifyDataSetChanged()
                } else {
                    Log.d("wow", "Current data: null")
                }
            }
    }

    private fun setupRecyclerView() {
        pengirimanAdapter = DaftarPengirimanAdapter(pengirimanList)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pengirimanAdapter
        }
    }
}
