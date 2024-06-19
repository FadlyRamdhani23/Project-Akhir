package com.tugasakhir.udmrputra.ui.barang

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.Placeholder
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tugasakhir.udmrputra.R
import com.google.firebase.firestore.FirebaseFirestore
import com.jaredrummler.materialspinner.MaterialSpinner
import com.tugasakhir.udmrputra.adapter.ImageAdapter
import com.tugasakhir.udmrputra.databinding.ActivityInputBarangBinding

class InputBarangActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputBarangBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = FirebaseFirestore.getInstance()

        val jenisSpinner = findViewById<Spinner>(R.id.jenisBarangSpinner)

        ArrayAdapter.createFromResource(
            this,
            R.array.jenis_barang,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            jenisSpinner.adapter = adapter
        }

        binding.buttonChooseImage.setOnClickListener {
            val charSequence = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add Photo!")
            builder.setItems(charSequence) { dialog, which ->
                if (charSequence[which] == "Take Photo") {
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(takePictureIntent, 0)
                } else if (charSequence[which] == "Choose from Gallery") {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, 1)
                } else {
                    dialog.dismiss()
                }
                builder.show()
            }
        }

        binding.btnCheckout.setOnClickListener {

            val nama = binding.inputNamaBarang.text?.toString()
            val jumlahString = binding.inputJumlahBarang.text?.toString()
            val jenis = binding.jenisBarangSpinner.selectedItem.toString()
            val catatan = binding.inputCatatanBarang.text?.toString()
            val jumlah = if (!jumlahString.isNullOrEmpty()) {
                jumlahString.toInt()
            } else {
                null
            }

            if (nama == null) {
                binding.inputNamaBarang.error = "Nama barang tidak boleh kosong"
                binding.inputNamaBarang.requestFocus()
                return@setOnClickListener
            }
            if (jumlah == null) {
                binding.inputJumlahBarang.error = "Jumlah barang tidak boleh kosong"
                binding.inputJumlahBarang.requestFocus()
                return@setOnClickListener
            }
            if (catatan == null) {
                binding.inputCatatanBarang.error = "Catatan barang tidak boleh kosong"
                binding.inputCatatanBarang.requestFocus()
                return@setOnClickListener
            }

                val barang = hashMapOf(
                    "nama" to nama,
                    "jumlah" to jumlah,
                    "jenis" to jenis,
                    "catatan" to catatan
                )

                db.collection("barang")
                    .add(barang)
                    .addOnSuccessListener { documentReference ->
                        val intent = Intent(this, BarangActivity::class.java)
                        startActivity(intent)
                        finish()

                        Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->

                        Toast.makeText(this, "Data gagal disimpan", Toast.LENGTH_SHORT).show()
                    }
        }
//        setSupportActionBar(binding.toolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 0) {
                val bitmap = data?.extras?.get("data") as Bitmap
                binding.imagePreview.setImageBitmap(bitmap)
            } else if (requestCode == 1) {
                val uri: Uri? = data?.data
                binding.imagePreview.setImageURI(uri)
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