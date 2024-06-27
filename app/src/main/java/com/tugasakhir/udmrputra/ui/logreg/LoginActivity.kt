package com.tugasakhir.udmrputra.ui.logreg

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tugasakhir.udmrputra.databinding.ActivityLoginBinding
import com.tugasakhir.udmrputra.ui.Home
import com.tugasakhir.udmrputra.ui.mitra.HomeMitraActivity
import com.tugasakhir.udmrputra.ui.sopir.HomeSupirActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvRegistrasi.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        auth = FirebaseAuth.getInstance()
        binding.btnMasuk.setOnClickListener {
            val email = binding.emailTextField.text.toString()
            val password = binding.sandiTextField.text.toString()

            // Validasi Email
            if (email.isEmpty()) {
                binding.emailTextField.error = "Email Harus Diisi"
                binding.emailTextField.requestFocus()
                return@setOnClickListener
            }

            // Validasi kesesuaian Email
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailTextField.error = "Email Tidak Valid"
                binding.emailTextField.requestFocus()
                return@setOnClickListener
            }

            // Validasi Password
            if (password.isEmpty()) {
                binding.sandiTextField.error = "Password Harus Diisi"
                binding.sandiTextField.requestFocus()
                return@setOnClickListener
            }

            // buatkan validasi email jika tidak terdapat dalam database
            val db = Firebase.firestore
            db.collection("users").whereEqualTo("email", email).get().addOnSuccessListener { result ->
                if (result.isEmpty) {
                    binding.emailTextField.error = "Email Tidak Terdaftar"
                    binding.emailTextField.requestFocus()
                    return@addOnSuccessListener
                }
            }


            // Validasi jumlah karakter dari password
            if (password.length < 8) {
                binding.sandiTextField.error = "Password Minimal 8 Karakter"
                binding.sandiTextField.requestFocus()
                return@setOnClickListener
            }

            LoginFirebase(email, password)
        }
    }

    private fun LoginFirebase(email: String, password: String) {
        // Tampilkan ProgressBar
        binding.loadingProgressBar.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                // Sembunyikan ProgressBar
                binding.loadingProgressBar.visibility = View.GONE

                if (it.isSuccessful) {
                    val db = Firebase.firestore
                    db.collection("users").whereEqualTo("email", email).get().addOnSuccessListener { result ->
                        if (!result.isEmpty) {
                            var statusFound = false
                            for (document in result) {
                                val status = document.getString("status")
                                if (status != null) {
                                    statusFound = true
                                    when (status) {
                                        "supir" -> {
                                            Toast.makeText(this, "Berhasil Masuk sebagai Supir", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(this, HomeSupirActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        "admin" -> {
                                            Toast.makeText(this, "Berhasil Masuk sebagai Admin", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(this, Home::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        "mitra" -> {
                                            Toast.makeText(this, "Berhasil Masuk sebagai Mitra", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(this, HomeMitraActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        else -> {
                                            Toast.makeText(this, "Berhasil Masuk", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(this, Home::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                    }
                                }
                            }
                            if (!statusFound) {
                                Toast.makeText(this, "Akun tidak terdaftar", Toast.LENGTH_SHORT).show()
                                auth.signOut()
                            }
                        } else {
                            Toast.makeText(this, "Pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

}
