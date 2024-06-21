package com.tugasakhir.udmrputra.ui.logreg

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tugasakhir.udmrputra.data.Users
import com.tugasakhir.udmrputra.databinding.ActivityLoginBinding
import com.tugasakhir.udmrputra.ui.Home
import com.tugasakhir.udmrputra.ui.sopir.HomeSupirActivity


class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvRegistrasi.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        auth = FirebaseAuth.getInstance()
        binding.btnMasuk.setOnClickListener(){
            val email = binding.emailTextField.text.toString()
            val password = binding.sandiTextField.text.toString()

            //Validasi Email
            if (email.isEmpty()) {
                binding.emailTextField.error = "Email Harus Diisi"
                binding.emailTextField.requestFocus()
                return@setOnClickListener
            }

            //Validasi kesesuaian Email
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

            LoginFirebase(email, password)
        }
    }

    private fun LoginFirebase(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    val db = Firebase.firestore
                    db.collection("users").whereEqualTo("email", email).get().addOnSuccessListener { result ->
                        if (!result.isEmpty) {
                            for (document in result) {
                                val status = document.getString("status")
                                if (status != null) {
                                    if (status == "supir") {
                                        Toast.makeText(this, "Berhasil Masuk sebagai Supir", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this,HomeSupirActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Berhasil Masuk", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, Home::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                } else {
                                    Toast.makeText(this, "Status pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
                                }
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
