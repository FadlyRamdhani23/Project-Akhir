package com.tugasakhir.udmrputra.ui.sopir

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Pengiriman
import com.tugasakhir.udmrputra.databinding.ActivityRiwayatPengirimanSupirBinding
import com.tugasakhir.udmrputra.ui.dashboard.InputPengirimanActivity
import com.tugasakhir.udmrputra.ui.dashboard.PengirimanAdapter
import java.text.SimpleDateFormat
import java.util.Locale

class RiwayatPengirimanSupirActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRiwayatPengirimanSupirBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PengirimanAdapter
    private lateinit var auth : FirebaseAuth

    private lateinit var pengirimanAdapter: PengirimanAdapter
    private val pengirimanList = mutableListOf<Pengiriman>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiwayatPengirimanSupirBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PengirimanAdapter(pengirimanList)
        recyclerView.adapter = adapter
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val uId = user?.uid
        setupRecyclerView()
        fetchPengirimanData(uId.toString())
    }
    private fun setupRecyclerView() {
        pengirimanAdapter = PengirimanAdapter(pengirimanList)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pengirimanAdapter
        }
    }

    private fun fetchPengirimanData(supirId : String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pengiriman").whereEqualTo("supirId", supirId)
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
                        val supirId = document.getString("supirId") ?: ""
                        val status = document.getString("status") ?: ""
                        val tanggalPengajuanTimestamp = document.get("tanggal") // Read without casting
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                        val tanggal = if (tanggalPengajuanTimestamp is com.google.firebase.Timestamp) {
                            dateFormat.format(tanggalPengajuanTimestamp.toDate())
                        } else {
                            // Handle the case where tanggalPengajuan is not a Timestamp
                            ""
                        }

                        val pengiriman = Pengiriman(
                            pengirimanId,
                            latitudeTujuan,
                            longitudeTujuan,
                            latitudeSupir,
                            longitudeSupir,
                            supir,
                            supirId,
                            address,
                            status,
                            tanggal,
                        )
                        pengirimanList.add(pengiriman)
                    }
                    pengirimanAdapter.notifyDataSetChanged()
                } else {
                    Log.d("wow", "Current data: null")
                }
            }
    }

}