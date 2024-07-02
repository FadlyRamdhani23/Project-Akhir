package com.tugasakhir.udmrputra.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.tugasakhir.udmrputra.data.Pengajuan
import com.tugasakhir.udmrputra.databinding.FragmentNotificationsBinding
import java.text.SimpleDateFormat
import java.util.*

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var pengajuanAdapter: PengajuanAdapter
    private val pengajuanList = mutableListOf<Pengajuan>()
    private val filteredPengajuanList = mutableListOf<Pengajuan>()
    private var pengajuanListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        setupSpinners()
        fetchPengajuanData()

        return root
    }

    private fun setupRecyclerView() {
        pengajuanAdapter = PengajuanAdapter(filteredPengajuanList)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pengajuanAdapter
        }
    }

    private fun setupSpinners() {
        val dateOptions = arrayOf("Semua", "Hari Ini", "Minggu Ini", "Bulan Ini")
        val statusOptions = arrayOf("Semua", "Pengiriman", "Pending", "Dikemas", "Approved")

        val dateSpinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dateOptions)
        dateSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilterTanggal.adapter = dateSpinnerAdapter

        val statusSpinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statusOptions)
        statusSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilterStatus.adapter = statusSpinnerAdapter

        binding.spinnerFilterTanggal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterPengajuan()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spinnerFilterStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterPengajuan()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun fetchPengajuanData() {
        val db = FirebaseFirestore.getInstance()
        pengajuanListener = db.collection("pengajuan")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    pengajuanList.clear()
                    for (document in snapshot.documents) {
                        val pengajuanId = document.id
                        val userId = document.getString("namaPetani") ?: ""
                        val tanggalPengajuan = document.getString("tanggalPengajuan") ?: ""
                        val barangAjuan = document.getString("barangAjuan") ?: ""
                        val jenisPembayaran = document.getString("jenisPembayaran") ?: ""
                        val statusPengajuan = document.getString("status") ?: ""
                        val address = document.getString("address") ?: ""
                        val latitude = document.getDouble("latitude") ?: 0.0
                        val longitude = document.getDouble("longitude") ?: 0.0

                        db.collection("pengajuan").document(pengajuanId).collection("barang")
                            .get()
                            .addOnSuccessListener { barangResult ->
                                val listBarang = mutableListOf<String>()
                                for (barangDocument in barangResult) {
                                    val namaBarang = barangDocument.getString("namaBarang") ?: ""
                                    listBarang.add(namaBarang)
                                }
                                val pengajuan = Pengajuan(
                                    pengajuanId,
                                    userId,
                                    tanggalPengajuan,
                                    barangAjuan,
                                    listBarang,
                                    jenisPembayaran,
                                    statusPengajuan,
                                    address,
                                    latitude,
                                    longitude
                                )
                                pengajuanList.add(pengajuan)
                                filterPengajuan()
                            }
                    }
                }
            }
    }

    private fun filterPengajuan() {
        val dateFilter = binding.spinnerFilterTanggal.selectedItem.toString()
        val statusFilter = binding.spinnerFilterStatus.selectedItem.toString().lowercase(Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = Calendar.getInstance()

        filteredPengajuanList.clear()

        val dateFilteredPengajuan = when (dateFilter) {
            "Semua" -> pengajuanList
            "Hari Ini" -> {
                val today = dateFormat.format(currentDate.time)
                pengajuanList.filter { it.tanggalPengajuan == today }
            }
            "Minggu Ini" -> {
                currentDate.set(Calendar.DAY_OF_WEEK, currentDate.firstDayOfWeek)
                val startOfWeek = currentDate.time
                currentDate.add(Calendar.DAY_OF_WEEK, 6)
                val endOfWeek = currentDate.time
                pengajuanList.filter {
                    val tanggalPengajuan = dateFormat.parse(it.tanggalPengajuan)
                    tanggalPengajuan in startOfWeek..endOfWeek
                }
            }
            "Bulan Ini" -> {
                currentDate.set(Calendar.DAY_OF_MONTH, 1)
                val startOfMonth = currentDate.time
                currentDate.add(Calendar.MONTH, 1)
                currentDate.add(Calendar.DAY_OF_MONTH, -1)
                val endOfMonth = currentDate.time
                pengajuanList.filter {
                    val tanggalPengajuan = dateFormat.parse(it.tanggalPengajuan)
                    tanggalPengajuan in startOfMonth..endOfMonth
                }
            }
            else -> pengajuanList
        }

        filteredPengajuanList.addAll(
            if (statusFilter == "semua") dateFilteredPengajuan
            else dateFilteredPengajuan.filter { it.statusPengajuan.lowercase(Locale.getDefault()) == statusFilter }
        )

        pengajuanAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        pengajuanListener?.remove()
    }
}
