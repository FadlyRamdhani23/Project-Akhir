package com.tugasakhir.udmrputra.ui.pengajuan

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
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
import java.text.NumberFormat
import java.util.Locale

class ActivityPengajuan : AppCompatActivity() {

    private lateinit var binding: ActivityPengajuanBinding
    private val viewModel: PengajuanViewModel by viewModels()
    private lateinit var adapter: PengajuanAdapterBarang
    private lateinit var bottomSheetDialog: BottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPengajuanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        setupSpinner()
        initializeTextView()

        adapter = PengajuanAdapterBarang(this, emptyList()) { selectedBarang ->
            binding.textViewInputBarang.text = selectedBarang.name
            viewModel.updateCategoryMap(selectedBarang.id, selectedBarang.name)
            bottomSheetDialog.dismiss()
        }

        setupObservers()
        setupTextWatchers()

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
                finish()
            }
        })
    }

    private fun showBottomSheetDialog() {
        bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetBinding = BottomSheetSelectItemBinding.inflate(LayoutInflater.from(this))
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        bottomSheetBinding.recyclerViewItems.adapter = adapter
        bottomSheetBinding.recyclerViewItems.layoutManager = LinearLayoutManager(this)

        bottomSheetBinding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.updateList(viewModel.filterBarangList(s.toString()))
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        bottomSheetDialog.show()
    }

    private fun setupTextWatchers() {
        setupCurrencyTextWatcher(binding.inputHargaPasar)
        setupCurrencyTextWatcher(binding.inputHargaBeli)
    }

    private fun setupCurrencyTextWatcher(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                editText.removeTextChangedListener(this)
                try {
                    val originalString = s.toString()

                    // Remove formatting characters
                    val cleanString = originalString.replace("[Rp,.]".toRegex(), "")

                    if (cleanString.isNotEmpty()) {
                        val longVal: Long = cleanString.toLong()

                        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                        formatter.maximumFractionDigits = 0
                        val formattedString = formatter.format(longVal)

                        // Update input field with formatted currency
                        editText.setText(formattedString)
                        editText.setSelection(formattedString.length)
                    }
                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace()
                }

                editText.addTextChangedListener(this)
            }
        })
    }

    private fun submitForm() {
        val mainNamaPetani = binding.inputNamaPetani.text.toString().trim()
        val mainNamaBarang = binding.textViewInputBarang.text.toString().trim()
        val mainJumlahBarang = binding.editTextQuantity.text.toString().toIntOrNull()
        val mainHargaPasar = binding.inputHargaPasar.text.toString().replace("[Rp,.]".toRegex(), "").toLongOrNull()
        val mainHargaBeli = binding.inputHargaBeli.text.toString().replace("[Rp,.]".toRegex(), "").toLongOrNull()
        val mainCatatan = binding.inputCatatan.text.toString().trim()
        val mainJenisPembayaran = binding.inputJenisPembayaran.selectedItem.toString()

        if (mainNamaPetani.isEmpty() || mainNamaBarang == "Pilih Jenis Barang" || mainJumlahBarang == null ||
            mainHargaPasar == null || mainHargaBeli == null || mainCatatan.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields correctly", Toast.LENGTH_SHORT).show()
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
                setupCurrencyTextWatcher(etHargaPasar)
                setupCurrencyTextWatcher(etHargaBeli)
                val namaBarang = etNamaBarang.text.toString().trim()
                val jumlahBarang = etJumlahBarang.text.toString().toIntOrNull()
                val hargaPasar = etHargaPasar.text.toString().replace("[Rp,.]".toRegex(), "").toLongOrNull()
                val hargaBeli = etHargaBeli.text.toString().replace("[Rp,.]".toRegex(), "").toLongOrNull()
                val catatan = etCatatan.text.toString().trim()

                if (namaBarang.isEmpty() || jumlahBarang == null || hargaPasar == null || hargaBeli == null || catatan.isEmpty()) {
                    Toast.makeText(this, "Please fill out all fields in additional items", Toast.LENGTH_SHORT).show()
                    return
                }

                val barangId = viewModel.categoryMap.entries.firstOrNull { it.value == namaBarang }?.key
                val imageUrl = viewModel.barangList.value?.firstOrNull { it.id == barangId }?.gambar

                if (barangId == null || imageUrl == null) {
                    Toast.makeText(this, "Invalid item selected in additional items", Toast.LENGTH_SHORT).show()
                    return
                }

                val additionalPengajuanData = hashMapOf(
                    "namaBarang" to namaBarang,
                    "jumlahBarang" to jumlahBarang,
                    "hargaPasar" to hargaPasar,
                    "hargaBeli" to hargaBeli,
                    "catatan" to catatan,
                    "barangId" to barangId,
                    "imageUrl" to imageUrl
                )
                additionalPengajuanDataList.add(additionalPengajuanData)
            }
        }

        if (mainHargaBeli != null && mainHargaPasar != null && mainJumlahBarang != null) {
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

        // Setup currency TextWatcher for etHargaPasar and etHargaBeli
        setupCurrencyTextWatcher(etHargaPasar)
        setupCurrencyTextWatcher(etHargaBeli)

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
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
