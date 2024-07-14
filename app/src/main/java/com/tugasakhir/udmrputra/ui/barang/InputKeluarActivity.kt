package com.tugasakhir.udmrputra.ui.barang

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.databinding.ActivityInputBarangKeluarBinding
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class InputKeluarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputBarangKeluarBinding
    private val categoryMap = hashMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputBarangKeluarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupTextWatchers()
        initializeSpinner()
        setupButtonListeners()
    }

    private fun initializeSpinner() {
        val db = FirebaseFirestore.getInstance()
        val list = ArrayList<String>()
        list.add("Pilih Jenis Barang")

        db.collection("barang")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val id = document.id
                    val name = document.getString("nama")
                    if (name != null) {
                        categoryMap[id] = name
                        list.add(name)
                    }
                }

                val adapter = object : ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_item,
                    list
                ) {
                    override fun isEnabled(position: Int): Boolean {
                        return position != 0
                    }

                    override fun getDropDownView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getDropDownView(position, convertView, parent)
                        val textView = view.findViewById<TextView>(android.R.id.text1)
                        textView.setTextColor(
                            if (position == 0) {
                                ContextCompat.getColor(context, R.color.green)
                            } else {
                                ContextCompat.getColor(context, android.R.color.black)
                            }
                        )
                        return view
                    }
                }

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerInputBarang.adapter = adapter
            }
    }

    private fun setupButtonListeners() {
        binding.btnCheckout.setOnClickListener {
            handleCheckout()
        }
        binding.buttonIncrement.setOnClickListener {
            incrementCount()
        }

        binding.buttonDecrement.setOnClickListener {
            decrementCount()
        }

        binding.buttonDatePicker.setOnClickListener {
            showDatePickerDialog()
        }
    }
    private fun setupTextWatchers() {
        setupCurrencyTextWatcher(binding.inputHargaJual)
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

    private fun handleCheckout() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnCheckout.isEnabled = false

        val db = FirebaseFirestore.getInstance()
        val barangName = binding.spinnerInputBarang.selectedItem?.toString()
        val nama = binding.inputNamaPetani.text?.toString()
        val jumlahString = binding.editTextQuantity.text?.toString()
        val catatan = binding.inputCatatan.text?.toString()
        val tanggal = binding.buttonDatePicker.text?.toString()
        val hargaJual = binding.inputHargaJual.text.toString().replace("[Rp,.]".toRegex(), "").toLongOrNull()

        val barangId = categoryMap.entries.find { it.value == barangName }?.key
        val jumlah = if (!jumlahString.isNullOrEmpty()) {
            jumlahString.toInt()
        } else {
            null
        }

        // Validasi data yang dimasukkan
        if (barangName == "Pilih Jenis Barang") {
            showToast("Harap pilih jenis barang")
            return
        }
        if (nama.isNullOrEmpty()) {
            showToast("Harap masukkan nama petani")
            return
        }
        if (jumlah == null) {
            showToast("Harap masukkan jumlah barang")
            return
        }
        if (tanggal.isNullOrEmpty()) {
            showToast("Harap pilih tanggal")
            return
        }

        val barangKeluar = hashMapOf(
            "namaPetani" to nama,
            "jumlah" to jumlah,
            "catatan" to catatan,
            "tanggal" to tanggal,
            "hargaJual" to hargaJual
        )

        if (barangId != null) {
            db.collection("barang").document(barangId)
                .collection("keluar")
                .add(barangKeluar)
                .addOnSuccessListener {
                    Log.d("InputKeluarActivity", "Data barang keluar berhasil ditambahkan")
                    updateItemQuantity(barangId, jumlah)
                }
                .addOnFailureListener { e ->
                    showToast("Error menambahkan data barang keluar")
                    binding.progressBar.visibility = View.GONE
                    binding.btnCheckout.isEnabled = true
                    Log.e("InputKeluarActivity", "Error menambahkan data barang keluar", e)
                }
        } else {
            showToast("Error menemukan ID barang")
            binding.progressBar.visibility = View.GONE
            binding.btnCheckout.isEnabled = true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun incrementCount() {
        val current = binding.editTextQuantity.text.toString().toInt()
        binding.editTextQuantity.setText((current + 1).toString())
    }

    private fun decrementCount() {
        val current = binding.editTextQuantity.text.toString().toInt()
        if (current > 0) {
            binding.editTextQuantity.setText((current - 1).toString())
        }
    }

    private fun showDatePickerDialog() {
        val calendar: Calendar = Calendar.getInstance()
        val year: Int = calendar.get(Calendar.YEAR)
        val month: Int = calendar.get(Calendar.MONTH)
        val day: Int = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this@InputKeluarActivity,
            { _: DatePicker?, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.buttonDatePicker.text = date
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun updateItemQuantity(barangId: String, jumlah: Int) {
        val db = FirebaseFirestore.getInstance()
        val barangRef = db.collection("barang").document(barangId)

        barangRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val currentQuantity = document.getLong("jumlah") ?: 0
                val newQuantity = currentQuantity - jumlah
                barangRef.update("jumlah", newQuantity)
                    .addOnSuccessListener {
                        Log.d("InputKeluarActivity", "Jumlah barang berhasil diperbarui")
                        setResult(Activity.RESULT_OK) // Set the result code
                        finish()
                    }
                    .addOnFailureListener { e ->
                        showToast("Error memperbarui jumlah barang")
                        Log.e("InputKeluarActivity", "Error memperbarui jumlah barang", e)
                    }
            } else {
                showToast("Document tidak ditemukan")
                Log.e("InputKeluarActivity", "Document tidak ditemukan")
            }
            binding.progressBar.visibility = View.GONE
            binding.btnCheckout.isEnabled = true
        }.addOnFailureListener { e ->
            showToast("Error mendapatkan dokumen barang")
            binding.progressBar.visibility = View.GONE
            binding.btnCheckout.isEnabled = true
            Log.e("InputKeluarActivity", "Error mendapatkan dokumen barang", e)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
