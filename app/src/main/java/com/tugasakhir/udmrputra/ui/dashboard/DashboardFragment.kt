package com.tugasakhir.udmrputra.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Pengajuan
import com.tugasakhir.udmrputra.data.Pengiriman
import com.tugasakhir.udmrputra.databinding.FragmentDashboardBinding
import com.tugasakhir.udmrputra.ui.mitra.DaftarMitra
import com.tugasakhir.udmrputra.ui.notifications.PengajuanAdapter
import java.text.SimpleDateFormat
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PengirimanAdapter

    private lateinit var pengirimanAdapter: PengirimanAdapter
    private val pengirimanList = mutableListOf<Pengiriman>()

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        recyclerView = root.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PengirimanAdapter(pengirimanList)
        recyclerView.adapter = adapter

        binding.inputPengiriman.setOnClickListener {
            Intent(requireContext(), InputPengirimanActivity::class.java).also {
                startActivity(it)
            }
        }
        setupRecyclerView()
        fetchPengirimanData()
        return root
    }

    private fun setupRecyclerView() {
        pengirimanAdapter = PengirimanAdapter(pengirimanList)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pengirimanAdapter
        }
    }

    private fun fetchPengirimanData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("pengiriman")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("wow", "Listen failed", e)
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    pengirimanList.clear()
                    for (document in snapshots) {
                        val pengirimanId = document.id
                        var address = document.getString("address") ?: ""
                        val latitudeTujuan = document.getDouble("latitudeTujuan") ?: 0.0
                        val longitudeTujuan = document.getDouble("longitudeTujuan") ?: 0.0
                        val latitudeSupir = document.getDouble("latitudeSupir") ?: 0.0
                        val longitudeSupir = document.getDouble("longitudeSupir") ?: 0.0
                        val supir = document.getString("supir") ?: ""
                        val supirId = document.getString("supirId") ?: ""
                        val status = document.getString("status") ?: ""
                        val tanggalPengajuanTimestamp = document.get("tanggal")
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                        val tanggal = if (tanggalPengajuanTimestamp is com.google.firebase.Timestamp) {
                            dateFormat.format(tanggalPengajuanTimestamp.toDate())
                        } else {
                            ""
                        }

                        // Truncate address if it's too long
                        val maxLength = 30
                        if (address.length > maxLength) {
                            address = address.substring(0, maxLength) + "..."
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
                            tanggal
                        )
                        pengirimanList.add(pengiriman)
                    }

                    // Sort the list by date with the latest at the top
                    pengirimanList.sortByDescending { it.tanggal }

                    pengirimanAdapter.notifyDataSetChanged()

                } else {
                    Log.d("wow", "Current data: null")
                }
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
