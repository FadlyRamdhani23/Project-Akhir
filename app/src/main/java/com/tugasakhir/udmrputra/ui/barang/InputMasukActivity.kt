package com.tugasakhir.udmrputra.ui.barang

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.tugasakhir.udmrputra.R
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.tugasakhir.udmrputra.databinding.ActivityInputBarangBinding
import com.tugasakhir.udmrputra.databinding.ActivityInputBarangMasukBinding
import java.util.UUID

class InputMasukActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputBarangMasukBinding
    private var currentImageUri: Uri? = null
    private val imageList = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputBarangMasukBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = FirebaseFirestore.getInstance()

        // List untuk spinner, tambahkan "Pilih Jenis Barang" sebagai placeholder
        val list = ArrayList<String>()
        list.add("Pilih Jenis Barang")

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val categoryMap = hashMapOf<String, String>()

        db.collection("kategori")
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

                // Buat ArrayAdapter tanpa menyertakan placeholder dalam pilihan yang dapat dipilih
                val adapter = object : ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_item,
                    list
                ) {
                    override fun isEnabled(position: Int): Boolean {
                        // Tetapkan posisi 0 (placeholder) tidak dapat dipilih
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
                                // Warna teks untuk placeholder
                                ContextCompat.getColor(context, R.color.green)
                            } else {
                                // Warna teks untuk item yang dapat dipilih
                                ContextCompat.getColor(context, android.R.color.black)
                            }
                        )
                        return view
                    }
                }

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.jenisBarangSpinner.adapter = adapter
            }

        // ...

        binding.btnCheckoutt.setOnClickListener {
            val catName = binding.jenisBarangSpinner.selectedItem?.toString()

            // Pastikan catName tidak sama dengan placeholder sebelum memproses
            if (catName != "Pilih Jenis Barang") {
                // Proses data
            } else {
                // Tampilkan pesan bahwa jenis barang harus dipilih
                Toast.makeText(this, "Harap pilih jenis barang", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        pickImageLauncher.launch(gallery)
    }

    private val pickImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let {
                    currentImageUri = it
                    imageList.add(it) // Tambahkan URI gambar ke imageList
                    binding.buttonChooseImage.setImageURI(currentImageUri)
                }
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
