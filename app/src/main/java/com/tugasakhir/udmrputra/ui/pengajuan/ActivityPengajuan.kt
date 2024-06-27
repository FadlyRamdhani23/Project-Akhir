package com.tugasakhir.udmrputra.ui.pengajuan
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
import com.tugasakhir.udmrputra.databinding.ActivityPengajuanBinding
import com.tugasakhir.udmrputra.databinding.BottomSheetSelectItemBinding
import com.tugasakhir.udmrputra.ui.pengajuan.PengajuanAdapterBarang
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityPengajuan : AppCompatActivity() {

    private lateinit var binding: ActivityPengajuanBinding
    private lateinit var auth: FirebaseAuth
    private val categoryMap = mutableMapOf<String, String>()
    private val barangList = mutableListOf<Barang>()
    private lateinit var adapter: PengajuanAdapterBarang

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPengajuanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        initializeTextView()
        setupSpinner()

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

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetBinding = BottomSheetSelectItemBinding.inflate(LayoutInflater.from(this))
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        adapter = PengajuanAdapterBarang(this, barangList) { selectedBarang ->
            binding.textViewInputBarang.text = selectedBarang.name
            categoryMap[selectedBarang.id] = selectedBarang.name
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
        db.collection("barang")
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
                    Log.d("gambar", "imageUrls: $imageUrls")

                    db.collection("kategori").document(catId)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            val catName = documentSnapshot.getString("nama").toString()

                            val data = Barang(
                                barangId,
                                namaBarang,
                                catName,
                                jumlahBarang,
                                imageUrls.getOrNull(0) ?: ""
                            )
                            barangList.add(data)
                            adapter.notifyDataSetChanged()
                            Log.d("BarangActivity", "Data berhasil ditambahkan: $data")
                            bottomSheetBinding.progressBar.visibility = View.GONE
                        }
                        .addOnFailureListener { e ->
                            Log.w("BarangActivity", "Error mendapatkan nama kategori", e)
                            bottomSheetBinding.progressBar.visibility = View.GONE
                        }
                }
                Log.d("BarangActivity", "Data berhasil ditampilkan")
                Log.d("BarangActivity", "Data: $barangList")
                bottomSheetBinding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Log.w("BarangActivity", "Data gagal ditampilkan", exception)
                bottomSheetBinding.progressBar.visibility = View.GONE
            }
    }

    private fun filterBarangList(query: String) {
        val filteredList = barangList.filter { it.name.contains(query, ignoreCase = true) }
        adapter.updateList(filteredList)
    }

    private fun submitForm() {
        val mainNamaPetani = binding.inputNamaPetani.text.toString().trim()
        val mainNamaBarang = binding.textViewInputBarang.text.toString().trim()
        val mainJumlahBarang = binding.editTextQuantity.text.toString().trim()
        val mainHargaPasar = binding.inputHargaPasar.text.toString().trim()
        val mainHargaBeli = binding.inputHargaBeli.text.toString().trim()
        val mainCatatan = binding.inputCatatan.text.toString().trim()
        val mainJenisPembayaran = binding.inputJenisPembayaran.selectedItem.toString()

        // Validate main form data
        if (mainNamaPetani.isEmpty() || mainNamaBarang == "Pilih Jenis Barang" || mainJumlahBarang.isEmpty() ||
            mainHargaPasar.isEmpty() || mainHargaBeli.isEmpty() || mainCatatan.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val mainBarangId = categoryMap.entries.firstOrNull { it.value == mainNamaBarang }?.key
        if (mainBarangId == null) {
            Toast.makeText(this, "Invalid item selected", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val mainPengajuanData = hashMapOf(
            "namaPetani" to mainNamaPetani,
            "userId" to userId,
            "status" to "pending",
            "jenisPembayaran" to mainJenisPembayaran,
            "tanggalPengajuan" to currentDate
        )

        // Collect additional pengajuan data from dynamically added card views
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

                val barangId = categoryMap.entries.firstOrNull { it.value == namaBarang }?.key
                if (barangId == null) {
                    Toast.makeText(this, "Invalid item selected in additional items", Toast.LENGTH_SHORT).show()
                    return
                }

                val additionalPengajuanData = hashMapOf(
                    "namaBarang" to namaBarang,
                    "jumlahBarang" to jumlahBarang,
                    "hargaPasar" to hargaPasar,
                    "hargaBeli" to hargaBeli,
                    "catatan" to catatan,
                    "barangId" to barangId
                )
                additionalPengajuanDataList.add(additionalPengajuanData)
            }
        }

        val db = FirebaseFirestore.getInstance()

        // Add main pengajuan data
        db.collection("pengajuan")
            .add(mainPengajuanData)
            .addOnSuccessListener { documentReference ->
                val pengajuanId = documentReference.id
                Log.d("ActivityPengajuan", "DocumentSnapshot added with ID: $pengajuanId")

                // Add main form item to barang sub-collection
                val mainBarangData = hashMapOf(
                    "namaBarang" to mainNamaBarang,
                    "jumlahBarang" to mainJumlahBarang,
                    "hargaPasar" to mainHargaPasar,
                    "hargaBeli" to mainHargaBeli,
                    "catatan" to mainCatatan,
                    "barangId" to mainBarangId,
                    "pengajuanId" to pengajuanId
                )
                db.collection("pengajuan").document(pengajuanId).collection("barang")
                    .add(mainBarangData)
                    .addOnSuccessListener {
                        Log.d("ActivityPengajuan", "Main form item added to barang sub-collection")
                    }
                    .addOnFailureListener { e ->
                        Log.w("ActivityPengajuan", "Error adding main form item to barang sub-collection", e)
                    }

                // Add additional pengajuan data
                for (additionalPengajuanData in additionalPengajuanDataList) {
                    val additionalData = additionalPengajuanData.toMutableMap()
                    additionalData["pengajuanId"] = pengajuanId
                    db.collection("pengajuan").document(pengajuanId).collection("barang")
                        .add(additionalData)
                        .addOnSuccessListener { additionalDocRef ->
                            Log.d("ActivityPengajuan", "Additional DocumentSnapshot added with ID: ${additionalDocRef.id}")
                        }
                        .addOnFailureListener { e ->
                            Log.w("ActivityPengajuan", "Error adding additional document", e)
                        }
                }
                Toast.makeText(this, "Pengajuan berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.w("ActivityPengajuan", "Error adding document", e)
                Toast.makeText(this, "Pengajuan gagal disimpan", Toast.LENGTH_SHORT).show()
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
        var etNamaBarang = newPengajuanView.findViewById<TextView>(R.id.textViewInputBarangTambahan)
        val etJumlahBarang = newPengajuanView.findViewById<EditText>(R.id.editTextQuantityTambahan)
        val etHargaPasar = newPengajuanView.findViewById<EditText>(R.id.inputHargaPasarTambahan)
        val etHargaBeli = newPengajuanView.findViewById<EditText>(R.id.inputHargaBeliTambahan)
        val etCatatan = newPengajuanView.findViewById<EditText>(R.id.inputCatatanTambahan)

        val btnHapus = newPengajuanView.findViewById<ImageButton>(R.id.btn_hapus)

       etNamaBarang.setOnClickListener {
           val bottomSheetDialog = BottomSheetDialog(this)
           val bottomSheetBinding = BottomSheetSelectItemBinding.inflate(LayoutInflater.from(this))
           bottomSheetDialog.setContentView(bottomSheetBinding.root)

           adapter = PengajuanAdapterBarang(this, barangList) { selectedBarang ->
              etNamaBarang.text = selectedBarang.name
               categoryMap[selectedBarang.id] = selectedBarang.name
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
        btnHapus.setOnClickListener {
            binding.cardContainer.removeView(newPengajuanView)
        }

        binding.cardContainer.addView(newPengajuanView)
    }

}
