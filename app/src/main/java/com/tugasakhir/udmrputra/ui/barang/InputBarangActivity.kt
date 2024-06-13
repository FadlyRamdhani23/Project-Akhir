package com.tugasakhir.udmrputra.ui.barang

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.tugasakhir.udmrputra.R
import com.google.firebase.firestore.FirebaseFirestore
import com.jaredrummler.materialspinner.MaterialSpinner
import com.tugasakhir.udmrputra.databinding.ActivityInputBarangBinding

class InputBarangActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputBarangBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = FirebaseFirestore.getInstance()

        val list = ArrayList<String>()
        list.add("Pilih Jenis Barang")
        list.add("Buah")
        list.add("Sayur")

        val catSpinner = findViewById<MaterialSpinner>(R.id.inputJenisSipnner)
        catSpinner.setItems(list)

        catSpinner.setOnItemSelectedListener { view, position, id, item ->
            if (position == 0) {
                catSpinner.setError("Pilih jenis barang")
            }else{

            }
        }

//        binding.btnCheckout.setOnClickListener {
//
////            val catId = binding.inputJenisSipnner.selectedIndex.toString()
//            val nama = binding.inputbarang.text?.toString()
//            val jumlahString = binding.inputjumlah.text?.toString()
//            val jumlah = if (!jumlahString.isNullOrEmpty()) {
//                jumlahString.toInt()
//            } else {
//                null
//            }
//
//            if (catId != null && nama != null && jumlah != null) {
//                val barang = hashMapOf(
//                    "catId" to catId,
//                    "nama" to nama,
//                    "jumlah" to jumlah
//                )
//
//                db.collection("barang")
//                    .add(barang)
//                    .addOnSuccessListener { documentReference ->
//                        // Data berhasil disimpan, lakukan intent ke BarangActivity
//                        val intent = Intent(this, BarangActivity::class.java)
//                        startActivity(intent)
//                        finish() // Jika Anda ingin menutup InputBarangActivity setelah intent
//                    }
//                    .addOnFailureListener { e ->
//                        // Terjadi kesalahan saat menyimpan data, handle error di sini
//                    }
//            } else {
//                // Handle kasus di mana catId, nama, atau jumlah adalah null
//            }
//        }






        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        db.collection("kategori")
//            .get()
//            .addOnSuccessListener { result ->
//                for (document in result) {
//                    val data = document.getString("nama")
//                    data?.let { list.add(it) }
//                }
//
//                // Buat ArrayAdapter
//                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, list)
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//
//                // Atur adapter untuk Spinner
//                binding.inputJenisSipnner.adapter = adapter
//            }
//            .addOnFailureListener { exception ->
//                // Handle any errors here
//            }
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