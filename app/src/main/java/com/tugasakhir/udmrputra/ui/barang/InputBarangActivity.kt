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
import com.tugasakhir.udmrputra.R
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.tugasakhir.udmrputra.databinding.ActivityInputBarangBinding
import java.util.UUID

class InputBarangActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputBarangBinding
    private var currentImageUri: Uri? = null
    private val imageList = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = FirebaseFirestore.getInstance()

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

                // Buat ArrayAdapter
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, list)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Atur adapter untuk Spinner
                binding.jenisBarangSpinner.adapter = adapter
            }

        binding.buttonChooseImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    123
                )
            }
        }

        binding.btnCheckoutt.setOnClickListener {
            val catName = binding.jenisBarangSpinner.selectedItem?.toString()
            val catId = categoryMap.entries.find { it.value == catName }?.key
            val nama = binding.inputNamaBarang.text?.toString()
            val jumlahString = binding.inputJumlahBarang.text?.toString()
            val jumlah = if (!jumlahString.isNullOrEmpty()) {
                jumlahString.toInt()
            } else {
                null
            }

            if (catId != null && nama != null && jumlah != null && imageList.isNotEmpty()) {
                val storageRefs = imageList.map {
                    Firebase.storage.reference.child("images/${UUID.randomUUID()}")
                }

                val uploadTasks = mutableListOf<Task<Uri>>()
                val imageUrls = mutableListOf<String>()
                storageRefs.forEachIndexed { index, storageRef ->
                    val uploadTask = storageRef.putFile(imageList[index])
                    uploadTasks.add(uploadTask.continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let { throw it }
                        }
                        storageRef.downloadUrl
                    }.addOnSuccessListener { uri ->
                        imageUrls.add(uri.toString())
                        if (imageUrls.size == storageRefs.size) {
                            val barang = hashMapOf(
                                "catId" to catId,
                                "nama" to nama,
                                "jumlah" to jumlah,
                                "gambar" to imageUrls
                            )

                            Log.d("InputBarangActivity", "Data: $barang")

                            Tasks.whenAllComplete(uploadTasks)
                                .addOnSuccessListener {
                                    db.collection("barang")
                                        .add(barang)
                                        .addOnSuccessListener { documentReference ->
                                            val intent = Intent(this, BarangActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                            Log.d(
                                                "InputBarangActivity",
                                                "DocumentSnapshot added with ID: ${documentReference.id}"
                                            )
                                        }
                                        .addOnFailureListener { e ->
                                            Log.d("InputBarangActivity", "Error adding document", e)
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.d("InputBarangActivity", "Error: ${e.message}")
                                    Log.w(ContentValues.TAG, "Error uploading images!", e)
                                }
                        }
                    }.addOnFailureListener { e ->
                        Log.d("InputBarangActivity", "Error: ${e.message}")
                    })
                }
                Log.d("InputBarangActivity", "Upload tasks: $imageUrls")
            } else {
                Log.d("InputBarangActivity", "Data tidak lengkap atau tidak ada gambar yang dipilih")
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
