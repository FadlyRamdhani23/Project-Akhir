package com.tugasakhir.udmrputra.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.tugasakhir.udmrputra.databinding.FragmentHomeBinding
import com.tugasakhir.udmrputra.ui.barang.BarangActivity
import com.tugasakhir.udmrputra.ui.dashboard.sopir.DaftarSopir
import com.tugasakhir.udmrputra.ui.mitra.DaftarMitra
import com.tugasakhir.udmrputra.ui.pengajuan.ActivityPengajuan

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreat eView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Set data for PieChartView
        val pieChartView = binding.pieChartView
        pieChartView.setData(180f, 180f) // Set data for Buah and Sayur

        binding.pieChartView.setOnClickListener {
            val intent = Intent(activity, BarangActivity::class.java)
            startActivity(intent)
        }

        binding.toDaftarMitra.setOnClickListener {
            val intent = Intent(activity, DaftarMitra::class.java)
            startActivity(intent)
        }
        binding.pengajuan.setOnClickListener {
            val intent = Intent(activity, ActivityPengajuan::class.java)
            startActivity(intent)
        }

        binding.toDaftarSopir.setOnClickListener(){
            val intent = Intent(activity, DaftarSopir::class.java)
            startActivity(intent)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}