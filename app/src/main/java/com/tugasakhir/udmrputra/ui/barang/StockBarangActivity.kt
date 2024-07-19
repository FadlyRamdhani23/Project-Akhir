package com.tugasakhir.udmrputra.ui.barang

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.adapter.StockBarangAdapter
import com.tugasakhir.udmrputra.data.Barang
import com.tugasakhir.udmrputra.databinding.ActivityStockBarangBinding

class StockBarangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStockBarangBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StockBarangAdapter
    private lateinit var progressBar: View
    private lateinit var editTextSearch: EditText
    private lateinit var btnSearch: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStockBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressBar = findViewById(R.id.stockLoading)
        editTextSearch = findViewById(R.id.editTextSearch)
        btnSearch = findViewById(R.id.btn_search)

        catListRecyclerView() // Inisialisasi tanpa filter

        setupSpinnerFilter()
        setupToolbar()
        setupSearchButton()
        setupSearchEditText()
    }

    private fun setupSpinnerFilter() {
        binding.spinnerJenis.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                Log.d("StockBarangActivity", "Selected jenis: $selectedItem")
                filterData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("StockBarangActivity", "Nothing selected")
            }
        }
    }

    private fun filterData() {
        val jenis = binding.spinnerJenis.selectedItem?.toString() ?: "Semua"
        val filter = when (jenis) {
            "Sayur" -> "zFJX68K6C6ohrq89ePDg"
            "Buah" -> "JV9d40TfUWOHoyg8i5Wt"
            else -> null
        }
        catListRecyclerView(filter)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.topAppBarr)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun catListRecyclerView(filter: String? = null) {
        val barangList = arrayListOf<Barang>()
        val db = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.rvBarangList)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = StockBarangAdapter(this, barangList)
        recyclerView.adapter = adapter

        progressBar.visibility = View.VISIBLE

        db.collection("barang")
            .get()
            .addOnSuccessListener { result ->
                var pendingTasks = result.size()
                for (document in result) {
                    val barangId = document.id
                    val catId = document.data["catId"].toString()
                    val namaBarang = document.data["nama"].toString()
                    val jumlahBarang = document.data["jumlah"].toString()

                    val imageUrls = if (document.get("gambar") is List<*>) {
                        (document.get("gambar") as? List<String>) ?: emptyList()
                    } else {
                        emptyList()
                    }
                    Log.d("StockBarangActivity", "imageUrls: $imageUrls")

                    if (filter != null && catId != filter) {
                        pendingTasks -= 1
                        if (pendingTasks == 0) {
                            adapter.notifyDataSetChanged()
                            progressBar.visibility = View.GONE
                        }
                        continue
                    }

                    db.collection("kategori").document(catId)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            val catName = documentSnapshot.getString("nama").toString()

                            val data = Barang(
                                barangId,
                                namaBarang,
                                catName,
                                jumlahBarang,
                                imageUrls.getOrNull(0).toString()
                            )
                            barangList.add(data)
                            Log.d("StockBarangActivity", "Data berhasil ditambahkan: $data")

                            pendingTasks -= 1
                            if (pendingTasks == 0) {
                                adapter.notifyDataSetChanged()
                                progressBar.visibility = View.GONE
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w("StockBarangActivity", "Error mendapatkan nama kategori", e)
                            pendingTasks -= 1
                            if (pendingTasks == 0) {
                                adapter.notifyDataSetChanged()
                                progressBar.visibility = View.GONE
                            }
                        }
                }
                if (pendingTasks == 0) {
                    adapter.notifyDataSetChanged()
                    progressBar.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                Log.w("StockBarangActivity", "Data gagal ditampilkan", exception)
                progressBar.visibility = View.GONE
            }
    }

    private fun setupSearchButton() {
        btnSearch.setOnClickListener {
            if (editTextSearch.isVisible) {
                editTextSearch.visibility = View.GONE
            } else {
                editTextSearch.visibility = View.VISIBLE
                editTextSearch.requestFocus()
            }
        }
    }

    private fun setupSearchEditText() {
        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    searchByNamaBarang(query)
                } else {
                    catListRecyclerView()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun searchByNamaBarang(query: String) {
        val barangList = arrayListOf<Barang>()
        val db = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.rvBarangList)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = StockBarangAdapter(this, barangList)
        recyclerView.adapter = adapter

        progressBar.visibility = View.VISIBLE

        db.collection("barang")
            .whereGreaterThanOrEqualTo("nama", query)
            .whereLessThanOrEqualTo("nama", query + "\uf8ff")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val barangId = document.id
                    val catId = document.data["catId"].toString()
                    val namaBarang = document.data["nama"].toString()
                    val jumlahBarang = document.data["jumlah"].toString()

                    val imageUrls = if (document.get("gambar") is List<*>) {
                        (document.get("gambar") as? List<String>) ?: emptyList()
                    } else {
                        emptyList()
                    }
                    Log.d("StockBarangActivity", "imageUrls: $imageUrls")

                    db.collection("kategori").document(catId)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            val catName = documentSnapshot.getString("nama").toString()

                            val data = Barang(
                                barangId,
                                namaBarang,
                                catName,
                                jumlahBarang,
                                imageUrls.getOrNull(0).toString()
                            )
                            barangList.add(data)
                            Log.d("StockBarangActivity", "Data berhasil ditambahkan: $data")

                            adapter.notifyDataSetChanged()
                            progressBar.visibility = View.GONE
                        }
                        .addOnFailureListener { e ->
                            Log.w("StockBarangActivity", "Error mendapatkan nama kategori", e)
                            progressBar.visibility = View.GONE
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("StockBarangActivity", "Data gagal ditampilkan", exception)
                progressBar.visibility = View.GONE
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
