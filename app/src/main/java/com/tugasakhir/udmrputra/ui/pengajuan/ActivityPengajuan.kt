package com.tugasakhir.udmrputra.ui.pengajuan

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Barang
import com.tugasakhir.udmrputra.databinding.ActivityPengajuanBinding
import com.tugasakhir.udmrputra.databinding.BottomSheetSelectItemBinding

class ActivityPengajuan : AppCompatActivity() {

    private lateinit var binding: ActivityPengajuanBinding
    private val viewModel: PengajuanViewModel by viewModels()
    private lateinit var adapter: PengajuanAdapterBarang
    private lateinit var bottomSheetDialog: BottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPengajuanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinner()
        initializeTextView()

        adapter = PengajuanAdapterBarang(this, emptyList()) { selectedBarang ->
            binding.textViewInputBarang.text = selectedBarang.name
            viewModel.updateCategoryMap(selectedBarang.id, selectedBarang.name)
            bottomSheetDialog.dismiss() // Ensure BottomSheetDialog is dismissed
        }

        setupObservers()

        binding.btnCheckout.setOnClickListener {
            submitForm()
        }
        binding.buttonIncrement.setOnClickListener {
            incrementCount()
        }
        binding.buttonDecrement.setOnClickListener {
            decrementCount()
        }
        binding.inputTambahan.setOnClickListener {
            addNewPengajuanCard()
        }
    }

    private fun initializeTextView() {
        binding.textViewInputBarang.setOnClickListener {
            showBottomSheetDialog()
        }
    }

    private fun setupObservers() {
        viewModel.barangList.observe(this, Observer { barangList ->
            adapter.updateList(barangList)
        })

        viewModel.toastMessage.observe(this, Observer { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        })
        viewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
        viewModel.isSubmitSuccessful.observe(this, Observer { isSuccessful ->
            if (isSuccessful) {
                // Tutup halaman jika submit berhasil
                finish()
            }
        })
    }

    private fun showBottomSheetDialog() {
        bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetBinding = BottomSheetSelectItemBinding.inflate(LayoutInflater.from(this))
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        bottomSheetBinding.recyclerViewItems.layoutManager = LinearLayoutManager(this)
        bottomSheetBinding.recyclerViewItems.adapter = adapter

        bottomSheetBinding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filteredList = viewModel.filterBarangList(s.toString())
                adapter.updateList(filteredList)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        bottomSheetDialog.show()
    }

    private fun submitForm() {
        val mainNamaPetani = binding.inputNamaPetani.text.toString().trim()
        val mainNamaBarang = binding.textViewInputBarang.text.toString().trim()
        val mainJumlahBarang = binding.editTextQuantity.text.toString().trim()
        val mainHargaPasar = binding.inputHargaPasar.text.toString().trim()
        val mainHargaBeli = binding.inputHargaBeli.text.toString().trim()
        val mainCatatan = binding.inputCatatan.text.toString().trim()
        val mainJenisPembayaran = binding.inputJenisPembayaran.selectedItem.toString()


        if (mainNamaPetani.isEmpty() || mainNamaBarang == "Pilih Jenis Barang" || mainJumlahBarang.isEmpty() ||
            mainHargaPasar.isEmpty() || mainHargaBeli.isEmpty() || mainCatatan.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val additionalPengajuanDataList = mutableListOf<Map<String, Any>>()
        val cardContainer = binding.cardContainer
        for (i in 0 until cardContainer.childCount) {
            val cardView = cardContainer.getChildAt(i)
            if (cardView is ViewGroup) {
                val etNamaBarang = cardView.findViewById<TextView>(R.id.textViewInputBarangTambahan)
                val etJumlahBarang = cardView.findViewById<EditText>(R.id.editTextQuantityTambahan)
                val etHargaPasar = cardView.findViewById<EditText>(R.id.inputHargaPasarTambahan)
                val etHargaBeli = cardView.findViewById<EditText>(R.id.inputHargaBeliTambahan)
                val etCatatan = cardView.findViewById<EditText>(R.id.inputCatatanTambahan)

                val namaBarang = etNamaBarang.text.toString().trim()
                val jumlahBarang = etJumlahBarang.text.toString().trim()
                val hargaPasar = etHargaPasar.text.toString().trim()
                val hargaBeli = etHargaBeli.text.toString().trim()
                val catatan = etCatatan.text.toString().trim()

                if (namaBarang.isEmpty() || jumlahBarang.isEmpty() || hargaPasar.isEmpty() || hargaBeli.isEmpty() || catatan.isEmpty()) {
                    Toast.makeText(this, "Please fill out all fields in additional items", Toast.LENGTH_SHORT).show()
                    return
                }

                val additionalPengajuanData = hashMapOf(
                    "namaBarang" to namaBarang,
                    "jumlahBarang" to jumlahBarang,
                    "hargaPasar" to hargaPasar,
                    "hargaBeli" to hargaBeli,
                    "catatan" to catatan
                )
                additionalPengajuanDataList.add(additionalPengajuanData)
            }
        }

        viewModel.submitForm(
            mainNamaPetani,
            mainNamaBarang,
            mainJumlahBarang,
            mainHargaPasar,
            mainHargaBeli,
            mainCatatan,
            mainJenisPembayaran,
            additionalPengajuanDataList
        )
    }

    private fun incrementCount() {
        val currentCount = binding.editTextQuantity.text.toString().toIntOrNull() ?: 0
        val newCount = currentCount + 1
        binding.editTextQuantity.setText(newCount.toString())
    }

    private fun decrementCount() {
        val currentCount = binding.editTextQuantity.text.toString().toIntOrNull() ?: 0
        if (currentCount > 0) {
            val newCount = currentCount - 1
            binding.editTextQuantity.setText(newCount.toString())
        }
    }

    private fun setupSpinner() {
        val spinner: Spinner = findViewById(R.id.inputJenisPembayaran)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.payment_methods,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun addNewPengajuanCard() {
        val inflater = LayoutInflater.from(this)
        val newPengajuanView = inflater.inflate(R.layout.item_pengajuan, null)
        val etNamaBarang = newPengajuanView.findViewById<TextView>(R.id.textViewInputBarangTambahan)
        val etJumlahBarang = newPengajuanView.findViewById<EditText>(R.id.editTextQuantityTambahan)
        val etHargaPasar = newPengajuanView.findViewById<EditText>(R.id.inputHargaPasarTambahan)
        val etHargaBeli = newPengajuanView.findViewById<EditText>(R.id.inputHargaBeliTambahan)
        val etCatatan = newPengajuanView.findViewById<EditText>(R.id.inputCatatanTambahan)

        val btnHapus = newPengajuanView.findViewById<ImageButton>(R.id.btn_hapus)

        etNamaBarang.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(this)
            val bottomSheetBinding = BottomSheetSelectItemBinding.inflate(LayoutInflater.from(this))
            bottomSheetDialog.setContentView(bottomSheetBinding.root)

            adapter = PengajuanAdapterBarang(this, viewModel.barangList.value ?: emptyList()) { selectedBarang ->
                etNamaBarang.text = selectedBarang.name
                viewModel.updateCategoryMap(selectedBarang.id, selectedBarang.name)
                bottomSheetDialog.dismiss()
            }

            bottomSheetBinding.recyclerViewItems.layoutManager = LinearLayoutManager(this)
            bottomSheetBinding.recyclerViewItems.adapter = adapter

            bottomSheetBinding.editTextSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val filteredList = viewModel.filterBarangList(s.toString())
                    adapter.updateList(filteredList)
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            bottomSheetDialog.show()
        }

        btnHapus.setOnClickListener {
            binding.cardContainer.removeView(newPengajuanView)
        }

        binding.cardContainer.addView(newPengajuanView)
    }
}
