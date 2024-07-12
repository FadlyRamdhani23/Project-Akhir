package com.tugasakhir.udmrputra.ui.sopir

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.data.Pengiriman
import com.tugasakhir.udmrputra.databinding.ActivityDaftarPengirimanTodayBinding
import com.tugasakhir.udmrputra.ui.dashboard.PengirimanAdapter
import java.text.SimpleDateFormat
import java.util.Locale

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

        // Query untuk status "dikemas"
        val queryDikemas = db.collection("pengiriman")
            .whereEqualTo("supirId", supirId)
            .whereEqualTo("status", "Dikemas")

        // Query untuk status "pengiriman"
        val queryPengiriman = db.collection("pengiriman")
            .whereEqualTo("supirId", supirId)
            .whereEqualTo("status", "Pengiriman")

        // Menggabungkan hasil kedua query
        queryDikemas.addSnapshotListener { snapshotsDikemas, e ->
            if (e != null) {
                Log.w("fetchPengirimanData", "Listen failed", e)
                return@addSnapshotListener
            }

            queryPengiriman.addSnapshotListener { snapshotsPengiriman, e2 ->
                if (e2 != null) {
                    Log.w("fetchPengirimanData", "Listen failed", e2)
                    return@addSnapshotListener
                }

                pengirimanList.clear()

                if (snapshotsDikemas != null && !snapshotsDikemas.isEmpty) {
                    for (document in snapshotsDikemas) {
                        addDocumentToList(document)
                    }
                }

                if (snapshotsPengiriman != null && !snapshotsPengiriman.isEmpty) {
                    for (document in snapshotsPengiriman) {
                        addDocumentToList(document)
                    }
                }

                pengirimanAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun addDocumentToList(document: DocumentSnapshot) {
        val pengirimanId = document.id
        val address = document.getString("address") ?: ""
        val latitudeTujuan = document.getDouble("latitudeTujuan") ?: 0.0
        val longitudeTujuan = document.getDouble("longitudeTujuan") ?: 0.0
        val latitudeSupir = document.getDouble("latitudeSupir") ?: 0.0
        val longitudeSupir = document.getDouble("longitudeSupir") ?: 0.0
        val supir = document.getString("supir") ?: ""
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
            document.getString("supirId") ?: "",
            address,
            status,
            tanggal
        )
        pengirimanList.add(pengiriman)
    }

    private fun setupRecyclerView() {
        pengirimanAdapter = DaftarPengirimanAdapter(pengirimanList)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pengirimanAdapter
        }
    }
}
