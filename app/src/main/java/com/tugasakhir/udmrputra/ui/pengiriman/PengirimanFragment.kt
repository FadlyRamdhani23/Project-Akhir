package com.tugasakhir.udmrputra.ui.pengiriman

import android.annotation.SuppressLint
import android.app.Activity
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
    companion object {
        private const val REQUEST_CODE_INPUT_PENGIRIMAN = 100
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        setupInputPengirimanButton()

        return root
    }

    override fun onResume() {
        super.onResume()
        fetchPengirimanData()
    }

    private fun setupRecyclerView() {
        _binding?.let {
            recyclerView = it.recyclerView
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            adapter = PengirimanAdapter(pengirimanList)
            recyclerView.adapter = adapter
        }
    }

    private fun setupInputPengirimanButton() {
        _binding?.let {
            it.inputPengiriman.setOnClickListener {
                Intent(requireContext(), InputPengirimanActivity::class.java).also {
                    startActivityForResult(it, REQUEST_CODE_INPUT_PENGIRIMAN)
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_INPUT_PENGIRIMAN && resultCode == Activity.RESULT_OK) {
            // Refresh data pengiriman setelah InputPengirimanActivity selesai
            fetchPengirimanData()
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
                _binding?.let {
                    if (pengirimanList.isEmpty()) {
                        it.emptyView.visibility = View.VISIBLE
                        it.recyclerView.visibility = View.GONE
                    } else {
                        it.emptyView.visibility = View.GONE
                        it.recyclerView.visibility = View.VISIBLE
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
