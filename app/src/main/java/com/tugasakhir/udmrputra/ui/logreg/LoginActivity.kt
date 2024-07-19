package com.tugasakhir.udmrputra.ui.logreg

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.tugasakhir.udmrputra.R
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
        val forgotPasswordText = findViewById<TextView>(R.id.forgotPasswordTextView)
        forgotPasswordText.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        auth = FirebaseAuth.getInstance()
        binding.btnMasuk.setOnClickListener {
            val email = binding.emailTextField.text.toString()
            val password = binding.sandiTextField.text.toString()

            if (email.isEmpty()) {
                binding.emailTextField.error = "Email Harus Diisi"
                binding.emailTextField.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailTextField.error = "Email Tidak Valid"
                binding.emailTextField.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.sandiTextField.error = "Password Harus Diisi"
                binding.sandiTextField.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 8) {
                binding.sandiTextField.error = "Password Minimal 8 Karakter"
                binding.sandiTextField.requestFocus()
                return@setOnClickListener
            }

            val db = Firebase.firestore
            db.collection("users").whereEqualTo("email", email).get().addOnSuccessListener { result ->
                if (result.isEmpty) {
                    binding.emailTextField.error = "Email Tidak Terdaftar"
                    binding.emailTextField.requestFocus()
                } else {
                    LoginFirebase(email, password)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Terjadi kesalahan: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun LoginFirebase(email: String, password: String) {
        binding.loadingProgressBar.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                binding.loadingProgressBar.visibility = View.GONE

                if (it.isSuccessful) {
                    val db = Firebase.firestore
                    db.collection("users").whereEqualTo("email", email).get().addOnSuccessListener { result ->
                        if (!result.isEmpty) {
                            val userId = result.documents[0].id
                            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val token = task.result
                                    db.collection("users").document(userId).update("fcmToken", token)
                                        .addOnSuccessListener {
                                            navigateToHome(result.documents[0].getString("status"))
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Failed to update token: ${it.message}", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    Toast.makeText(this, "Failed to get FCM token", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(this, "Pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(this, "Terjadi kesalahan: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Kata sandi salah", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToHome(status: String?) {
        var intent: Intent? = null
        when (status) {
            "supir" -> {
                Toast.makeText(this, "Berhasil Masuk sebagai Supir", Toast.LENGTH_SHORT).show()
                intent = Intent(this, HomeSupirActivity::class.java)
            }
            "admin" -> {
                Toast.makeText(this, "Berhasil Masuk sebagai Admin", Toast.LENGTH_SHORT).show()
                intent = Intent(this, Home::class.java)
            }
            "mitra" -> {
                Toast.makeText(this, "Berhasil Masuk sebagai Mitra", Toast.LENGTH_SHORT).show()
                intent = Intent(this, HomeMitraActivity::class.java)
            }
            else -> {
                Toast.makeText(this, "Berhasil Masuk", Toast.LENGTH_SHORT).show()
                intent = Intent(this, Home::class.java)
            }
        }
        intent?.let {
            startActivity(it)
            finish()
        }
    }
}
