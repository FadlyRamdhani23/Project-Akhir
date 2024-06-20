package com.tugasakhir.udmrputra.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.tugasakhir.udmrputra.databinding.FragmentNotificationsBinding
import com.tugasakhir.udmrputra.ui.ui.main.SectionsPagerAdapter

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

       val sectionsPagerAdapter = SectionsPagerAdapter(requireContext(), childFragmentManager)
        val viewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs = binding.tabs
        tabs.setupWithViewPager(viewPager)

        // Set data for PieChartView
//        val pieChartView = binding.pieChartView
//        pieChartView.setData(180f, 180f) // Set data for Buah and Sayur
//
//        binding.btnRefresh.setOnClickListener {
//            val intent = Intent(activity, SupirActivity::class.java)
//            startActivity(intent)
//        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
