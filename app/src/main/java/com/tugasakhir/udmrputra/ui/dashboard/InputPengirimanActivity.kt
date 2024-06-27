package com.tugasakhir.udmrputra.ui.dashboard
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Barang
import com.tugasakhir.udmrputra.data.Pengajuan
import com.tugasakhir.udmrputra.databinding.ActivityInputPengirimanBinding
import com.tugasakhir.udmrputra.databinding.ActivityPengajuanBinding
import com.tugasakhir.udmrputra.databinding.BottomSheetSelectItemBinding
import com.tugasakhir.udmrputra.ui.pengajuan.PengajuanAdapterBarang
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InputPengirimanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInputPengirimanBinding
    private lateinit var auth: FirebaseAuth
    private val categoryMap = mutableMapOf<String, String>()
    private val barangList = mutableListOf<Pengajuan>()
    private lateinit var adapter: InpuPengajuanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputPengirimanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        initializeTextView()
        setupSpinner()

        binding.btnCheckout.setOnClickListener {
            submitForm()
        }

    }

    private fun initializeTextView() {
        binding.textViewInputBarang.setOnClickListener {
            showBottomSheetDialog()
        }

    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetBinding = BottomSheetSelectItemBinding.inflate(LayoutInflater.from(this))
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        adapter = InpuPengajuanAdapter(this, barangList) { selectedPengajuan ->
            binding.textViewInputBarang.text = selectedPengajuan.userId
            categoryMap[selectedPengajuan.id] = selectedPengajuan.userId
            bottomSheetDialog.dismiss()
        }

        bottomSheetBinding.recyclerViewItems.layoutManager = LinearLayoutManager(this)
        bottomSheetBinding.recyclerViewItems.adapter = adapter

        loadBarangData(bottomSheetBinding)

        bottomSheetBinding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterBarangList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        bottomSheetDialog.show()
    }

    private fun loadBarangData(bottomSheetBinding: BottomSheetSelectItemBinding) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pengajuan")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val pengajuanId = document.id
                    val userId = document.getString("namaPetani") ?: ""
                    val tanggalPengajuan = document.getString("tanggalPengajuan") ?: ""
                    val barangAjuan = document.getString("barangAjuan") ?: ""
                    val jenisPembayaran = document.getString("jenisPembayaran") ?: ""
                    val statusPengajuan = document.getString("status") ?: ""

                    // Mengambil list barang dari sub-collection "barang"
                    db.collection("pengajuan").document(pengajuanId).collection("barang")
                        .get()
                        .addOnSuccessListener { barangResult ->
                            val listBarang = mutableListOf<String>()
                            for (barangDocument in barangResult) {
                                val namaBarang = barangDocument.getString("namaBarang") ?: ""
                                listBarang.add(namaBarang)
                            }
                            val pengajuan = Pengajuan(
                                pengajuanId,
                                userId,
                                tanggalPengajuan,
                                barangAjuan,
                                listBarang,
                                jenisPembayaran,
                                statusPengajuan
                            )
                            barangList.add(pengajuan)
                            adapter.notifyDataSetChanged()
                            bottomSheetBinding.progressBar.visibility = View.GONE
                        }
                        .addOnFailureListener { e ->
                            Log.w("BarangActivity", "Error mendapatkan nama kategori", e)
                            bottomSheetBinding.progressBar.visibility = View.GONE
                        }
                }
                Log.d("BarangActivity", "Data: $barangList")
                bottomSheetBinding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Log.w("BarangActivity", "Data gagal ditampilkan", exception)
                bottomSheetBinding.progressBar.visibility = View.GONE
            }
    }

    private fun filterBarangList(query: String) {
        val filteredList = barangList.filter { it.userId.contains(query, ignoreCase = true) }
        adapter.updateList(filteredList)
    }

    private fun submitForm() {
        Toast.makeText(this, "Pengiriman berhasil diajukan", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun setupSpinner() {
        val spinner: Spinner = findViewById(R.id.inputNamaSupir)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.payment_methods,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }


}
