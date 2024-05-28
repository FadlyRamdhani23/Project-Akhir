package com.tugasakhir.udmrputra.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Pengiriman
import com.tugasakhir.udmrputra.databinding.FragmentDashboardBinding


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PengirimanAdapter
    private var pengirimanList = arrayListOf<Pengiriman>(
        Pengiriman(1, "Pengiriman 1",  -6.971744, 107.630628, -6.1754,  106.8272, "Alamat 1", "08123456789", "Dalam Proses"),
        Pengiriman(2, "Pengiriman 2", -6.9175, 107.6191, -7.2026, 107.9075, "Alamat 2", "08123456789", "Dalam Proses"),
        Pengiriman(3, "Pengiriman 3", -6.5950, 106.8166, -7.2026, 107.9075, "Alamat 3", "08123456789", "Dalam Proses"),
        // Tambahkan lebih banyak data Pengiriman jika diperlukan
    )


    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        recyclerView = root.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PengirimanAdapter(pengirimanList)
        recyclerView.adapter = adapter
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}