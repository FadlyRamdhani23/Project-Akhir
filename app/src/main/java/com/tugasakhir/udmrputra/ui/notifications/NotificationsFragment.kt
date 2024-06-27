package com.tugasakhir.udmrputra.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.data.Pengajuan
import com.tugasakhir.udmrputra.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var pengajuanAdapter: PengajuanAdapter
    private val pengajuanList = mutableListOf<Pengajuan>()

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
        fetchPengajuanData()

        return root
    }

    private fun setupRecyclerView() {
        pengajuanAdapter = PengajuanAdapter(pengajuanList)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pengajuanAdapter
        }
    }

    private fun fetchPengajuanData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("pengajuan")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val pengajuanId = document.id
                    val userId = document.getString("namaPetani") ?: ""
                    val tanggalPengajuan = document.getString("tanggalPengajuan") ?: ""
                    val barangAjuan = document.getString("barangAjuan") ?: ""
                    val jenisPembayaran = document.getString("jenisPembayaran") ?: ""
                    val statusPengajuan = document.getString("status") ?: ""

                    // Mengambil list barang dari sub-collection "barang"
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
                                statusPengajuan
                            )
                            pengajuanList.add(pengajuan)
                            pengajuanAdapter.notifyDataSetChanged()
                        }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
