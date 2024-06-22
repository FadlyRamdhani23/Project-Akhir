package com.tugasakhir.udmrputra.ui.logreg

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tugasakhir.udmrputra.databinding.ActivityRegisterBinding
import com.tugasakhir.udmrputra.databinding.ActivityRegisterMitraBinding
import com.tugasakhir.udmrputra.ui.mitra.DaftarMitra

class RegisterMitraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterMitraBinding
    lateinit var auth: FirebaseAuth

    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterMitraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.tvMasuk.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnRegister.setOnClickListener {
            val nama = binding.namaTextField.text.toString()
            val email = binding.emailTextField.text.toString()
            val password = binding.sandiTextField.text.toString()
            val konfirmasiPassword = binding.konfirmasiTextField.text.toString()
            val noHp = binding.nomorTextField.text.toString()

            // Validasi Nama
            if (nama.isEmpty()) {
                binding.namaTextField.error = "Nama Harus Diisi"
                binding.namaTextField.requestFocus()
                return@setOnClickListener
            }

            // Validasi Email
            if (email.isEmpty()) {
                binding.emailTextField.error = "Email Harus Diisi"
                binding.emailTextField.requestFocus()
                return@setOnClickListener
            }

            // Validasi NoHP
            if (noHp.isEmpty()) {
                binding.nomorTextField.error = "Nomor HP Harus Diisi"
                binding.nomorTextField.requestFocus()
                return@setOnClickListener
            }

            // Validasi kesesuaian Email
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailTextField.error = "Email Tidak Valid"
                binding.emailTextField.requestFocus()
                return@setOnClickListener
            }

            //Validasi Password
            if (password.isEmpty()) {
                binding.sandiTextField.error = "Password Harus Diisi"
                binding.sandiTextField.requestFocus()
                return@setOnClickListener
            }

            //Validasi panjang Password
            if (password.length < 8) {
                binding.sandiTextField.error = "Password Minimal 8 Karakter"
                binding.sandiTextField.requestFocus()
                return@setOnClickListener
            }

            //Validasi Konfirmasi Password
            if (password != konfirmasiPassword) {
                binding.konfirmasiTextField.error = "Password tidak sesuai"
                binding.konfirmasiTextField.requestFocus()
                return@setOnClickListener
            }

            RegisterFirebase(nama, email, password, noHp)
        }
    }

    private fun RegisterFirebase(nama: String, email: String, password: String, noHp: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Dapatkan UID pengguna yang baru terdaftar
                    val user = auth.currentUser
                    val uid = user?.uid ?: ""

                    // Buat objek pengguna untuk disimpan di Firestore
                    val userMap = hashMapOf(
                        "nama" to nama,
                        "email" to email,
                        "noHp" to noHp,
                        "status" to "mitra",
                        "uid" to uid
                    )

                    // Simpan data pengguna di Firestore dengan menggunakan UID sebagai ID dokumen
                    db.collection("users").document(uid)
                        .set(userMap)
                        .addOnSuccessListener {
                            Log.d("RegisterActivity", "DocumentSnapshot added with ID: $uid")
                            Toast.makeText(this, "Register Berhasil", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, DaftarMitra::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            Log.w("RegisterActivity", "Error adding document", e)
                            Toast.makeText(this, "Gagal menyimpan data pengguna", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}