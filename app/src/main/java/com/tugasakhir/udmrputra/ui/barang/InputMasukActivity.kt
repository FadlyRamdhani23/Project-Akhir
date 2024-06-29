package com.tugasakhir.udmrputra.ui.barang

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
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
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.adapter.ImageAdapter
import com.tugasakhir.udmrputra.databinding.ActivityInputBarangMasukBinding
import java.util.Calendar
import java.util.UUID

class InputMasukActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputBarangMasukBinding
    private var currentImageUri: Uri? = null
    private val imageList = mutableListOf<Uri>()
    private lateinit var imageAdapter: ImageAdapter
    private val categoryMap = hashMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputBarangMasukBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initializeSpinner()
        setupRecyclerView()
        setupButtonListeners()
        updateRecyclerViewVisibility()
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

    private fun setupRecyclerView() {
        imageAdapter = ImageAdapter(imageList)
        binding.recyclerViewImages.apply {
            layoutManager = LinearLayoutManager(this@InputMasukActivity, RecyclerView.HORIZONTAL, false)
            adapter = imageAdapter
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

        setupTakePhotoButton()
    }

    private fun handleCheckout() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnCheckout.isEnabled = false
        val db = FirebaseFirestore.getInstance()
        val id = db.collection("barang").document().id
        val barangName = binding.spinnerInputBarang.selectedItem?.toString()
        val nama = binding.inputNamaPetani.text?.toString()
        val jumlahString = binding.editTextQuantity.text?.toString()
        val catatan = binding.inputCatatan.text?.toString()
        val tanggal = binding.buttonDatePicker.text?.toString()
        val hargaBeli = binding.inputHargaBeli.text?.toString()

        val barangId = categoryMap.entries.find { it.value == barangName }?.key
        val jumlah = if (!jumlahString.isNullOrEmpty()) {
            jumlahString.toInt()
        } else {
            null
        }

        // Validasi data yang dimasukkan
        if (barangName == "Pilih Jenis Barang") {
            Toast.makeText(this, "Harap pilih jenis barang", Toast.LENGTH_SHORT).show()
            return
        }
        if (nama.isNullOrEmpty()) {
            Toast.makeText(this, "Harap masukkan nama petani", Toast.LENGTH_SHORT).show()
            return
        }
        if (jumlah == null) {
            Toast.makeText(this, "Harap masukkan jumlah barang", Toast.LENGTH_SHORT).show()
            return
        }
        if (tanggal.isNullOrEmpty()) {
            Toast.makeText(this, "Harap pilih tanggal", Toast.LENGTH_SHORT).show()
            return
        }
        if (imageList.isEmpty()) {
            Toast.makeText(this, "Harap pilih gambar", Toast.LENGTH_SHORT).show()
            return
        }

        if (barangId != null) {
            val storageRefs = imageList.map {
                Firebase.storage.reference.child("imagesmasuk/${UUID.randomUUID()}")
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
                        val barangMasuk = hashMapOf(
                            "id" to id,
                            "namaPetani" to nama,
                            "jumlah" to jumlah,
                            "gambar" to imageUrls,
                            "catatan" to catatan,
                            "tanggal" to tanggal,
                            "hargaBeli" to hargaBeli
                        )

                        // Menambahkan data barang masuk ke subkoleksi "masuk"
                        db.collection("barang").document(barangId)
                            .collection("masuk")
                            .add(barangMasuk)
                            .addOnSuccessListener {
                                updateItemQuantity(barangId, jumlah)
                            }
                            .addOnFailureListener { e ->
                                Log.w("InputMasukActivity", "Error adding document", e)
                                binding.progressBar.visibility = View.GONE
                                binding.btnCheckout.isEnabled = true
                                Toast.makeText(this, "Gagal menambahkan data", Toast.LENGTH_SHORT).show()
                            }
                    }
                })
            }
            Tasks.whenAllComplete(uploadTasks).addOnCompleteListener {
                // Handle when all uploads complete
                Log.d("InputMasukActivity", "All image uploads complete")
            }
        }
    }

    private fun incrementCount() {
        val quantityString = binding.editTextQuantity.text.toString()
        var quantity = if (quantityString.isEmpty()) 0 else quantityString.toInt()
        quantity++
        binding.editTextQuantity.setText(quantity.toString())
    }

    private fun decrementCount() {
        val quantityString = binding.editTextQuantity.text.toString()
        var quantity = if (quantityString.isEmpty()) 0 else quantityString.toInt()
        if (quantity > 0) {
            quantity--
        }
        binding.editTextQuantity.setText(quantity.toString())
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val dateString = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.buttonDatePicker.text = dateString
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun setupTakePhotoButton() {
        // Gabungkan logika untuk mengambil foto atau memilih dari galeri
        binding.buttonAmbilPhoto.setOnClickListener {
            showImagePickerDialog()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Ambil Foto", "Pilih dari Galeri")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Pilih Gambar")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        openCamera()
                    } else {
                        requestPermissions(
                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            124
                        )
                    }
                }
                1 -> {
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
        }
        builder.show()
    }

    private fun openCamera() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
        }
        currentImageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri)
        }
        takePhotoLauncher.launch(cameraIntent)
    }

    private val takePhotoLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                currentImageUri?.let {
                    imageAdapter.addImage(it)
                    updateRecyclerViewVisibility()
                }
            }
        }

    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        gallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        pickImageLauncher.launch(gallery)
    }

    private val pickImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data?.clipData != null) {
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count) {
                        val imageUri = data.clipData!!.getItemAt(i).uri
                        imageAdapter.addImage(imageUri)
                    }
                } else {
                    data?.data?.let {
                        imageAdapter.addImage(it)
                    }
                }
                updateRecyclerViewVisibility()
            }
        }

    private fun updateItemQuantity(barangId: String, jumlah: Int) {
        val db = FirebaseFirestore.getInstance()
        val barangRef = db.collection("barang").document(barangId)

        barangRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val currentQuantity = document.getLong("jumlah") ?: 0
                val newQuantity = currentQuantity + jumlah
                barangRef.update("jumlah", newQuantity)
                    .addOnSuccessListener {
                        Log.d("InputMasukActivity", "Jumlah barang berhasil diperbarui")
                        val intent = Intent(this, BarangActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.d("InputMasukActivity", "Error memperbarui jumlah barang", e)
                    }
            } else {
                Log.d("InputMasukActivity", "Document tidak ditemukan")
            }
        }.addOnFailureListener { e ->
            Log.d("InputMasukActivity", "Error mendapatkan dokumen barang", e)
        }
    }

    private fun updateRecyclerViewVisibility() {
        if (imageAdapter.itemCount == 0) {
            binding.recyclerViewImages.visibility = View.GONE
//            binding.textNoImages.visibility = View.VISIBLE
        } else {
            binding.recyclerViewImages.visibility = View.VISIBLE
//            binding.textNoImages.visibility = View.GONE
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
