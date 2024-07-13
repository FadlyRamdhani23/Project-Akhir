package com.tugasakhir.udmrputra.ui.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.data.Pencatatan
import com.tugasakhir.udmrputra.data.PencatatanKeluar
import com.tugasakhir.udmrputra.databinding.FragmentBarang2Binding
import com.tugasakhir.udmrputra.adapter.PencatatanKeluarAdapter
import com.tugasakhir.udmrputra.adapter.PencatatanMasukAdapter

class PlaceholderFragment2 : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentBarang2Binding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var pAdapter: PencatatanMasukAdapter
    private lateinit var pAdapter2: PencatatanKeluarAdapter
    private lateinit var progressBar: ProgressBar
    private var barangId: String? = null
    private lateinit var emptyView: TextView

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this)[PageViewModel::class.java].apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
        barangId = arguments?.getString(ARG_BARANG_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentBarang2Binding.inflate(inflater, container, false)
        val root = binding.root

        recyclerView = binding.recyclerViewPencatatan
        recyclerView.layoutManager = LinearLayoutManager(context)

        progressBar = binding.progressBar
        emptyView = binding.emptyView

        if (arguments?.getInt(ARG_SECTION_NUMBER) == 1) fetchDataFromFirestore()
        else fetchDataFromFirestore2()

        return root
    }

    private fun fetchDataFromFirestore() {
        progressBar.visibility = View.VISIBLE

        val db = FirebaseFirestore.getInstance()
        val barangDocRef = db.collection("barang").document(barangId ?: "")

        barangDocRef.get().addOnSuccessListener { barangDocument ->
//            val bard = barangDocument.id
            val catId = barangDocument.getString("catId") ?: ""
            val namaBarang = barangDocument.getString("nama") ?: ""

            barangDocRef.collection("masuk")
                .get()
                .addOnSuccessListener { masukResult ->
                    val dataList = mutableListOf<Pencatatan>()
                    masukResult.forEach { masukDocument ->
                        val id = masukDocument.id
                        val namaPetani = masukDocument.getString("namaPetani") ?: ""
                        val jumlah = masukDocument.getLong("jumlah")?.toString() ?: ""
                        val gambarList = masukDocument.get("gambar") as? List<*>
                        val gambar = gambarList?.joinToString(",") ?: ""
                        val catatan = masukDocument.getString("catatan") ?: ""
                        val tanggal = masukDocument.getString("tanggal") ?: ""
                        val hargaBeli = masukDocument.getLong("hargaBeli")?.toString() ?: ""

                        dataList.add(
                        Pencatatan(
                                id = id,
                        catId = catId,
                        barId = barangId,
                        barangId = namaBarang,
                        namaPetani = namaPetani,
                        jumlah = jumlah,
                        gambar = gambar,
                        catatan = catatan,
                        tanggal = tanggal,
                        hargaBeli = hargaBeli
                        )
                        )
                    }
                    if (isAdded) {
                        if (dataList.isEmpty()) {
                            emptyView.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        } else {
                            pAdapter = PencatatanMasukAdapter(requireContext(), pencatatanList = dataList)
                            recyclerView.adapter = pAdapter
                            emptyView.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                        }
                        progressBar.visibility = View.GONE
                    }
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                }
        }
    }

    private fun fetchDataFromFirestore2() {
        progressBar.visibility = View.VISIBLE

        val db = FirebaseFirestore.getInstance()
        val barangDocRef = db.collection("barang").document(barangId ?: "")

        barangDocRef.get().addOnSuccessListener { barangDocument ->
            val bard = barangDocument.id
            val catId = barangDocument.getString("catId") ?: ""
            val namaBarang = barangDocument.getString("nama") ?: ""

            barangDocRef.collection("keluar")
                .get()
                .addOnSuccessListener { keluarResult ->
                    val dataList = mutableListOf<PencatatanKeluar>()
                    keluarResult.forEach { keluarDocument ->
                        val keluarId = keluarDocument.id
                        val namaPetani = keluarDocument.getString("namaPetani") ?: ""
                        val jumlah = keluarDocument.getLong("jumlah")?.toString() ?: ""
                        val catatan = keluarDocument.getString("catatan") ?: ""
                        val tanggal = keluarDocument.getString("tanggal") ?: ""
                        val hargaJual = keluarDocument.getLong("hargaJual")?.toString() ?: ""

                        dataList.add(
                            PencatatanKeluar(
                                id = keluarId,
                                catId = catId,
                                barId = bard,
                                barangId = namaBarang,
                                namaPetani = namaPetani,
                                jumlah = jumlah,
                                catatan = catatan,
                                tanggal = tanggal,
                                hargaJual = hargaJual
                            )
                        )
                    }
                    if (isAdded) {
                        if (dataList.isEmpty()) {
                            emptyView.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        } else {
                            pAdapter2 = PencatatanKeluarAdapter(requireContext(), pencatatanList = dataList)
                            recyclerView.adapter = pAdapter2
                            emptyView.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                        }
                        progressBar.visibility = View.GONE
                    }
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                }
        }
    }

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"
        private const val ARG_BARANG_ID = "barang_id"

        @JvmStatic
        fun newInstance(sectionNumber: Int, barangId: String?): PlaceholderFragment2 {
            return PlaceholderFragment2().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                    putString(ARG_BARANG_ID, barangId)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}