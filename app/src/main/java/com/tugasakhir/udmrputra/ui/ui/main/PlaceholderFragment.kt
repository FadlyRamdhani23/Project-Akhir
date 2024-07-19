package com.tugasakhir.udmrputra.ui.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.data.Pencatatan
import com.tugasakhir.udmrputra.data.PencatatanKeluar
import com.tugasakhir.udmrputra.databinding.FragmentBarangBinding
import com.tugasakhir.udmrputra.ui.barang.InputMasukActivity
import com.tugasakhir.udmrputra.ui.barang.InputKeluarActivity
import com.tugasakhir.udmrputra.adapter.PencatatanKeluarAdapter
import com.tugasakhir.udmrputra.adapter.PencatatanMasukAdapter
import java.text.SimpleDateFormat
import java.util.*

class PlaceholderFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentBarangBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var p_adapter: PencatatanMasukAdapter
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
    ): View {

        _binding = FragmentBarangBinding.inflate(inflater, container, false)
        val root = binding.root

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

        setupSpinnerFilter()

        if (arguments?.getInt(ARG_SECTION_NUMBER) == 1) fetchDataFromFirestore()
        else fetchDataFromFirestore2()

        fetchCatIdFromFirestore()

        return root
    }

    private fun setupSpinnerFilter() {
        binding.spinnerFilterTanggal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                Log.d("PlaceholderFragment", "Selected item: $selectedItem")
                filterData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("PlaceholderFragment", "Nothing selected")
            }
        }

        binding.spinnerJenis.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                Log.d("PlaceholderFragment", "Selected jenis: $selectedItem")
                filterData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("PlaceholderFragment", "Nothing selected")
            }
        }
    }

    private val catIdToTextMap = mapOf(
        "zFJX68K6C6ohrq89ePDg" to "sayur",
        "JV9d40TfUWOHoyg8i5Wt" to "buah"
    )

    private fun fetchCatIdFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("barang")
            .get()
            .addOnSuccessListener { result ->
                val catIdSet = mutableSetOf("Semua")  // Use a set to ensure unique values
                for (document in result) {
                    val catId = document.getString("catId") ?: ""
                    if (catId.isNotEmpty()) {
                        catIdSet.add(catIdToTextMap[catId] ?: catId)
                    }
                }
                val catIdList = catIdSet.toList()  // Convert the set back to a list
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, catIdList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerJenis.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.w("PlaceholderFragment", "Error getting catId: ", exception)
            }
    }

    private fun filterData() {
        val filter = binding.spinnerFilterTanggal.selectedItem?.toString() ?: "Semua"
        val jenis = binding.spinnerJenis.selectedItem?.toString() ?: "Semua"

        if (arguments?.getInt(ARG_SECTION_NUMBER) == 1) fetchDataFromFirestore(filter, jenis)
        else fetchDataFromFirestore2(filter, jenis)
    }

    private fun fetchDataFromFirestore(filter: String = "Semua", jenis: String = "Semua") {
        progressBar.visibility = View.VISIBLE

        val db = FirebaseFirestore.getInstance()
        db.collection("barang")
            .addSnapshotListener { barangResult, exception ->
                if (exception != null) {
                    progressBar.visibility = View.GONE
                    Log.w("PlaceholderFragment", "Listen failed.", exception)
                    return@addSnapshotListener
                }

                val dataList = mutableListOf<Pencatatan>()
                if (barangResult != null) {
                    val tasks = barangResult.map { barangDocument ->
                        val catId = barangDocument.getString("catId") ?: ""
                        val barangId = barangDocument.id
                        val barangName = barangDocument.getString("nama") ?: ""

                        barangDocument.reference.collection("masuk").get().continueWith { task ->
                            if (task.isSuccessful) {
                                task.result?.forEach { masukDocument ->
                                    val id = masukDocument.id
                                    val namaPetani = masukDocument.getString("namaPetani") ?: ""
                                    val jumlah = masukDocument.getLong("jumlah")?.toString() ?: ""
                                    val gambarList = masukDocument.get("gambar") as? List<String>
                                    val gambar = gambarList?.joinToString(",") ?: ""
                                    val catatan = masukDocument.getString("catatan") ?: ""
                                    val tanggal = masukDocument.getString("tanggal") ?: ""
                                    val hargaBeli = masukDocument.getLong("hargaBeli")?.toString() ?: ""
                                    val created_at = masukDocument.get("created_at") // Read without casting
                                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                                    val tanggalInput = if (created_at is com.google.firebase.Timestamp) {
                                        dateFormat.format(created_at.toDate())
                                    } else {
                                        // Handle the case where tanggalPengajuan is not a Timestamp
                                        ""
                                    }

                                    val pencatatan = Pencatatan(
                                        id = id,
                                        catId = catId,
                                        barId = barangId,
                                        barangId = barangName,
                                        namaPetani = namaPetani,
                                        jumlah = jumlah,
                                        gambar = gambar,
                                        catatan = catatan,
                                        tanggal = tanggal,
                                        hargaBeli = hargaBeli,
                                        tanggalInput = tanggalInput
                                    )

                                    if (shouldIncludeInFilter(pencatatan.tanggal.toString(), filter, jenis, catId)) {
                                        dataList.add(pencatatan)
                                    }
                                }
                            }
                        }
                    }

                    tasks.forEach { it.addOnCompleteListener {
                        if (tasks.all { it.isComplete } && isAdded) {
                            activity?.runOnUiThread {
                                if (isAdded) {
                                    // Sort the dataList by tanggalInput in descending order
                                    dataList.sortByDescending { pencatatan ->
                                        SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).parse(pencatatan.tanggalInput)
                                    }

                                    p_adapter = PencatatanMasukAdapter(requireContext(), pencatatanList = dataList)
                                    recyclerView.adapter = p_adapter
                                    progressBar.visibility = View.GONE
                                }
                            }
                        }
                    }}
                }
            }
    }


    private fun fetchDataFromFirestore2(filter: String = "Semua", jenis: String = "Semua") {
        progressBar.visibility = View.VISIBLE

        val db = FirebaseFirestore.getInstance()
        db.collection("barang")
            .addSnapshotListener { barangResult, exception ->
                if (exception != null) {
                    progressBar.visibility = View.GONE
                    Log.w("PlaceholderFragment", "Listen failed.", exception)
                    return@addSnapshotListener
                }

                val dataList = mutableListOf<PencatatanKeluar>()
                if (barangResult != null) {
                    val tasks = barangResult.map { barangDocument ->
                        val bard = barangDocument.id
                        val catId = barangDocument.getString("catId") ?: ""
                        val barangName = barangDocument.getString("nama") ?: ""

                        barangDocument.reference.collection("keluar").get().continueWith { task ->
                            if (task.isSuccessful) {
                                task.result?.forEach { masukDocument ->
                                    val id = masukDocument.id
                                    val namaPetani = masukDocument.getString("namaPetani") ?: ""
                                    val jumlah = masukDocument.getLong("jumlah")?.toString() ?: ""
                                    val catatan = masukDocument.getString("catatan") ?: ""
                                    val tanggal = masukDocument.getString("tanggal") ?: ""
                                    val hargaJual = masukDocument.getLong("hargaJual")?.toString() ?: ""
                                    val created_at = masukDocument.get("created_at") // Read without casting
                                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                                    val tanggalInput = if (created_at is com.google.firebase.Timestamp) {
                                        dateFormat.format(created_at.toDate())
                                    } else {
                                        // Handle the case where tanggalPengajuan is not a Timestamp
                                        ""
                                    }

                                    val pencatatanKeluar = PencatatanKeluar(
                                        id = id,
                                        catId = catId,
                                        barId = bard,
                                        barangId = barangName,
                                        namaPetani = namaPetani,
                                        jumlah = jumlah,
                                        catatan = catatan,
                                        tanggal = tanggal,
                                        hargaJual = hargaJual,
                                        tanggalInput = tanggalInput
                                    )

                                    if (shouldIncludeInFilter(pencatatanKeluar.tanggal.toString(), filter, jenis, catId)) {
                                        dataList.add(pencatatanKeluar)
                                    }
                                }
                            }
                        }
                    }

                    tasks.forEach { it.addOnCompleteListener {
                        if (tasks.all { it.isComplete }) {
                            activity?.runOnUiThread {
                                if (isAdded) {
                                    // Sort the dataList by tanggalInput in descending order
                                    dataList.sortByDescending { pencatatanKeluar ->
                                        SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).parse(pencatatanKeluar.tanggalInput)
                                    }

                                    p_adapter2 = PencatatanKeluarAdapter(requireContext(), pencatatanList = dataList)
                                    recyclerView.adapter = p_adapter2
                                    progressBar.visibility = View.GONE
                                }
                            }
                        }
                    }}
                }
            }
    }


    private fun shouldIncludeInFilter(dateString: String, filter: String, jenis: String, catId: String): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        val date = dateFormat.parse(dateString) ?: return false

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"))
        calendar.time

        val dateCalendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"))
        dateCalendar.time = date

        val dateMatches = when (filter) {
            "Semua" -> true
            "Hari Ini" -> {
                calendar.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                        calendar.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)
            }
            "Minggu Ini" -> {
                val weekStart = calendar.apply {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                }.time
                val weekEnd = calendar.apply {
                    add(Calendar.WEEK_OF_YEAR, 1)
                    add(Calendar.DAY_OF_MONTH, -1)
                }.time
                date in weekStart..weekEnd
            }
            "Bulan Ini" -> {
                val monthStart = calendar.apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                }.time
                val monthEnd = calendar.apply {
                    add(Calendar.MONTH, 1)
                    add(Calendar.DAY_OF_MONTH, -1)
                }.time
                date in monthStart..monthEnd
            }
            else -> false
        }

        val jenisMatches = jenis == "Semua" || jenis == catIdToTextMap[catId]

        return dateMatches && jenisMatches
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
