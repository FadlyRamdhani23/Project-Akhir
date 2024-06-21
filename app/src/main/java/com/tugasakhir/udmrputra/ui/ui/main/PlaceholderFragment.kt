package com.tugasakhir.udmrputra.ui.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Pencatatan
import com.tugasakhir.udmrputra.data.PencatatanKeluar
import com.tugasakhir.udmrputra.databinding.FragmentBarangBinding
import com.tugasakhir.udmrputra.ui.barang.InputMasukActivity
import com.tugasakhir.udmrputra.ui.barang.InputKeluarActivity
import com.tugasakhir.udmrputra.ui.barang.PencatatanAdapter
import com.tugasakhir.udmrputra.ui.barang.PencatatanKeluarAdapter

class PlaceholderFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentBarangBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var p_adapter: PencatatanAdapter
    private lateinit var p_adapter2: PencatatanKeluarAdapter
    private lateinit var progressBar: ProgressBar

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

        recyclerView = binding.recyclerViewPencatatan
        recyclerView.layoutManager = LinearLayoutManager(context)

        progressBar = binding.progressBar

        binding.fabAddBarang.setOnClickListener {
            val sectionNumber = arguments?.getInt(ARG_SECTION_NUMBER) ?: 1
            val intent = if (sectionNumber == 1) {
                Intent(context, InputMasukActivity::class.java)
            } else {
                Intent(context, InputKeluarActivity::class.java)
            }
            startActivity(intent)
        }

        if (arguments?.getInt(ARG_SECTION_NUMBER) == 1) fetchDataFromFirestore()
        else fetchDataFromFirestore2()

        return root
    }

    private fun fetchDataFromFirestore() {
        progressBar.visibility = View.VISIBLE

        val db = FirebaseFirestore.getInstance()
        db.collection("barang")
            .get()
            .addOnSuccessListener { barangResult ->
                val dataList = mutableListOf<Pencatatan>()
                val tasks = barangResult.map { barangDocument ->
                    val catId = barangDocument.getString("catId") ?: ""
                    val barangName = barangDocument.getString("nama") ?: ""

                    barangDocument.reference.collection("masuk").get().continueWith { task ->
                        if (task.isSuccessful) {
                            task.result?.forEach { masukDocument ->
                                val namaPetani = masukDocument.getString("namaPetani") ?: ""
                                val jumlah = masukDocument.getLong("jumlah")?.toString() ?: ""
                                val gambarList = masukDocument.get("gambar") as? List<String>
                                val gambar = gambarList?.joinToString(",") ?: ""
                                val catatan = masukDocument.getString("catatan") ?: ""
                                val tanggal = masukDocument.getString("tanggal") ?: ""
                                val hargaBeli = masukDocument.getString("hargaBeli") ?: ""

                                dataList.add(
                                    Pencatatan(
                                        id = 0,
                                        catId = catId,
                                        barangId = barangName,
                                        namaPetani = namaPetani,
                                        jumlah = jumlah,
                                        gambar = gambar,
                                        catatan = catatan,
                                        tanggal = tanggal,
                                        hargaBeli = hargaBeli
                                    )
                                )
                            }
                        }
                    }
                }

                // When all tasks are complete, update the adapter
                tasks.forEach { it.addOnCompleteListener {
                    if (tasks.all { it.isComplete }) {
                        p_adapter = PencatatanAdapter(requireContext(), pencatatanList = dataList)
                        recyclerView.adapter = p_adapter
                        progressBar.visibility = View.GONE
                    }
                }}
            }
            .addOnFailureListener { exception ->
                progressBar.visibility = View.GONE
            }
    }

    private fun fetchDataFromFirestore2() {
        progressBar.visibility = View.VISIBLE

        val db = FirebaseFirestore.getInstance()
        db.collection("barang")
            .get()
            .addOnSuccessListener { barangResult ->
                val dataList = mutableListOf<PencatatanKeluar>()
                val tasks = barangResult.map { barangDocument ->
                    val catId = barangDocument.getString("catId") ?: ""
                    val barangName = barangDocument.getString("nama") ?: ""

                    barangDocument.reference.collection("keluar").get().continueWith { task ->
                        if (task.isSuccessful) {
                            task.result?.forEach { masukDocument ->
                                val namaPetani = masukDocument.getString("namaPetani") ?: ""
                                val jumlah = masukDocument.getLong("jumlah")?.toString() ?: ""
                                val catatan = masukDocument.getString("catatan") ?: ""
                                val tanggal = masukDocument.getString("tanggal") ?: ""
                                val hargaJual = masukDocument.getString("hargaJual") ?: ""


                                dataList.add(
                                    PencatatanKeluar(
                                        id = 0,
                                        catId = catId,
                                        barangId = barangName,
                                        namaPetani = namaPetani,
                                        jumlah = jumlah,
                                        catatan = catatan,
                                        tanggal = tanggal,
                                        hargaJual = hargaJual
                                    )
                                )
                            }
                        }
                    }
                }

                // When all tasks are complete, update the adapter
                tasks.forEach { it.addOnCompleteListener {
                    if (tasks.all { it.isComplete }) {
                        p_adapter2 = PencatatanKeluarAdapter(requireContext(), pencatatanList = dataList)
                        recyclerView.adapter = p_adapter2
                        progressBar.visibility = View.GONE
                    }
                }}
            }
            .addOnFailureListener { exception ->
                progressBar.visibility = View.GONE
            }
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
