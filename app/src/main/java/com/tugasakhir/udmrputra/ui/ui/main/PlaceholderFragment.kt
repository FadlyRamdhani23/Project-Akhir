package com.tugasakhir.udmrputra.ui.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.databinding.FragmentBarangBinding
import com.tugasakhir.udmrputra.ui.barang.PencatatanAdapter
import com.tugasakhir.udmrputra.data.Pencatatan

class PlaceholderFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentBarangBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var p_adaper: PencatatanAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentBarangBinding.inflate(inflater, container, false)
        val root = binding.root

        val textView: TextView = binding.sectionLabel
        pageViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        // Initialize RecyclerView
        recyclerView = binding.recyclerViewPencatatan
        recyclerView.layoutManager = LinearLayoutManager(context)

        val data = listOf(
            Pencatatan(1, "Buah", "Mangga", "Asep", 5000,40, R.color.blue),
            Pencatatan(2, "Buah", "Pepaya", "Dodi", 3000, 50, R.color.blue),
            Pencatatan(3, "Buah", "Semangka", "Dadan", 7000, 30, R.color.blue),
            Pencatatan(4, "Sayur", "Tomat", "Zaenal", 8000, 60, R.color.green),
            Pencatatan(5, "Sayur", "Timun", "Jajang", 5500, 70, R.color.green),
            Pencatatan(6, "Sayur", "Kol", "Aldi", 3500, 80, R.color.green),
        )

        p_adaper = PencatatanAdapter(requireContext(), pencatatanList = data)
        recyclerView.adapter = p_adaper

        return root
    }

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"

        @JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}