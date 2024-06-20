package com.tugasakhir.udmrputra.ui.ui.main

import android.content.Intent
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
import com.tugasakhir.udmrputra.ui.barang.InputMasukActivity

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

        val data =listOf(
            Pencatatan(
                id = 1,
                catId = "Buah",
                barangId = "Mangga",
                namaPetani = "Asep",
                jumlah = "40",
                gambar = "https://example.com/image1.jpg",
                catatan = "Catatan A",
                tanggal = "2023-06-01",
                hargaBeli = "5000"
            ),
            Pencatatan(
                id = 2,
                catId = "Buah",
                barangId = "Pepaya",
                namaPetani = "Dodi",
                jumlah = "50",
                gambar = "https://example.com/image2.jpg",
                catatan = "Catatan B",
                tanggal = "2023-06-02",
                hargaBeli = "3000"
            ),
            Pencatatan(
                id = 3,
                catId = "Buah",
                barangId = "Semangka",
                namaPetani = "Dadan",
                jumlah = "30",
                gambar = "https://example.com/image3.jpg",
                catatan = "Catatan C",
                tanggal = "2023-06-03",
                hargaBeli = "7000"
            ),
            Pencatatan(
                id = 4,
                catId = "Sayur",
                barangId = "Tomat",
                namaPetani = "Zaenal",
                jumlah = "60",
                gambar = "https://example.com/image4.jpg",
                catatan = "Catatan D",
                tanggal = "2023-06-04",
                hargaBeli = "8000"
            ),
            Pencatatan(
                id = 5,
                catId = "Sayur",
                barangId = "Timun",
                namaPetani = "Jajang",
                jumlah = "70",
                gambar = "https://example.com/image5.jpg",
                catatan = "Catatan E",
                tanggal = "2023-06-05",
                hargaBeli = "5500"
            ),
            Pencatatan(
                id = 6,
                catId = "Sayur",
                barangId = "Kol",
                namaPetani = "Aldi",
                jumlah = "80",
                gambar = "https://example.com/image6.jpg",
                catatan = "Catatan F",
                tanggal = "2023-06-06",
                hargaBeli = "3500"
            )
        )

        binding.fabAddBarang.setOnClickListener {
            val intent = Intent(context,  InputMasukActivity::class.java)
            startActivity(intent)
        }

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