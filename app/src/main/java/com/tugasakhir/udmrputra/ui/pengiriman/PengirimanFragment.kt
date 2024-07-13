package com.tugasakhir.udmrputra.ui.pengiriman

import android.annotation.SuppressLint
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
import com.tugasakhir.udmrputra.adapter.PengirimanAdapter
import com.tugasakhir.udmrputra.data.Pengiriman
import com.tugasakhir.udmrputra.databinding.FragmentDashboardBinding
import java.text.SimpleDateFormat
import java.util.Locale

class PengirimanFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PengirimanAdapter
    private val pengirimanList = mutableListOf<Pengiriman>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        setupInputPengirimanButton()
        fetchPengirimanData()

        return root
    }

    private fun setupRecyclerView() {
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PengirimanAdapter(pengirimanList)
        recyclerView.adapter = adapter
    }

    private fun setupInputPengirimanButton() {
        binding.inputPengiriman.setOnClickListener {
            Intent(requireContext(), InputPengirimanActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchPengirimanData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("pengiriman")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("wow", "Listen failed", e)
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    pengirimanList.clear() // Clear the list before adding new data
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

                    adapter.notifyDataSetChanged()
                } else {
                    Log.d("wow", "Current data: null")
                }

                // Toggle visibility of empty view and recycler view
                if (pengirimanList.isEmpty()) {
                    binding.emptyView.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                } else {
                    binding.emptyView.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
