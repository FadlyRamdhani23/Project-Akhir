package com.tugasakhir.udmrputra.ui.barang

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.databinding.ActivityInputBarangBinding
import java.util.UUID

class InputBarangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInputBarangBinding
    private var currentImageUri: Uri? = null
    private val imageList = mutableListOf<Uri>()
    private lateinit var progressBar: ProgressBar
    private val categoryMap = hashMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        progressBar = findViewById(R.id.progressBar)
        setupSpinner()
        setupImagePicker()
        setupSaveButton()
//        setupTakePhotoButton()
    }


    private fun setupSpinner() {
        val db = FirebaseFirestore.getInstance()
        val list = arrayListOf("Pilih Jenis Barang")

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
                setupSpinnerAdapter(list)
            }
    }

    private fun setupSpinnerAdapter(list: ArrayList<String>) {
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            list
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
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
        binding.jenisBarangSpinner.adapter = adapter
    }

    private fun setupImagePicker() {
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
                    imageList.add(it)
                    binding.buttonChooseImage.setImageURI(currentImageUri)
                }
            }
        }

//    private fun setupTakePhotoButton() {
//        binding.buttonAmbilPhoto.setOnClickListener {
//            if (ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.CAMERA
//                ) == PackageManager.PERMISSION_GRANTED &&
//                ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//                ) == PackageManager.PERMISSION_GRANTED
//            ) {
//                openCamera()
//            } else {
//                requestPermissions(
//                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                    124
//                )
//            }
//        }
//    }
//
//    private fun openCamera() {
//        val values = ContentValues().apply {
//            put(MediaStore.Images.Media.TITLE, "New Picture")
//            put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
//        }
//        currentImageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
//        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
//            putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri)
//        }
//        takePhotoLauncher.launch(cameraIntent)
//    }
//
//    private val takePhotoLauncher: ActivityResultLauncher<Intent> =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                currentImageUri?.let {
//                    imageList.add(it)
//                    binding.buttonChooseImage.setImageURI(it)
//                }
//            }
//        }

    private fun setupSaveButton() {
        binding.btnCheckoutt.setOnClickListener {
            saveBarang()
        }
    }

    private fun saveBarang() {
        val catName = binding.jenisBarangSpinner.selectedItem?.toString()
        if (catName != "Pilih Jenis Barang") {
            val catId = categoryMap.entries.find { it.value == catName }?.key
            val nama = binding.inputNamaBarang.text?.toString()
            val jumlahString = binding.inputJumlahBarang.text?.toString()
            val jumlah = jumlahString?.toIntOrNull()

            Log.d("InputBarangActivity", "catId: $catId, nama: $nama, jumlah: $jumlah, imageList: $imageList")

            if (catId != null && !nama.isNullOrBlank() && jumlah != null && imageList.isNotEmpty()) {
                uploadData(catId, nama, jumlah)
            } else {
                Toast.makeText(this, "Data tidak lengkap atau tidak ada gambar yang dipilih", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Harap pilih jenis barang", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadData(catId: String, nama: String, jumlah: Int) {
        progressBar.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
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
                                    progressBar.visibility = View.GONE
                                    setResult(Activity.RESULT_OK) // Set the result code
                                    finish()
                                    Log.d(
                                        "InputBarangActivity",
                                        "DocumentSnapshot added with ID: ${documentReference.id}"
                                    )
                                }
                                .addOnFailureListener { e ->
                                    progressBar.visibility = View.GONE
                                    Log.d("InputBarangActivity", "Error adding document", e)
                                }
                        }
                        .addOnFailureListener { e ->
                            progressBar.visibility = View.GONE
                            Log.d("InputBarangActivity", "Error: ${e.message}")
                            Log.w(ContentValues.TAG, "Error uploading images!", e)
                        }
                }
            }.addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Log.d("InputBarangActivity", "Error: ${e.message}")
            })
        }
        Log.d("InputBarangActivity", "Upload tasks: $imageUrls")
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
