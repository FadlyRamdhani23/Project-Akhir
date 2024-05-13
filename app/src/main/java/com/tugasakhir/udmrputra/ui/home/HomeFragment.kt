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
import com.tugasakhir.udmrputra.ui.mitra.DaftarMitra
import com.tugasakhir.udmrputra.ui.pengajuan.ActivityPengajuan

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
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


        binding.toDaftarMitra.setOnClickListener {
            val intent = Intent(activity, DaftarMitra::class.java)
            startActivity(intent)
        }
        binding.pengajuan.setOnClickListener {
            val intent = Intent(activity, ActivityPengajuan::class.java)
            startActivity(intent)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}