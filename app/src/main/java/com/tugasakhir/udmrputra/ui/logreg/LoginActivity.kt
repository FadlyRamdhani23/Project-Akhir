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

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    lateinit var auth: FirebaseAuth
//    lateinit var database : FirebaseDatabase
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
//        mAuth = FirebaseAuth.getInstance()
//
//        mAuth.signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    // Sign in success, update UI with the signed-in user's information
//                    Toast.makeText(baseContext, "Authentication success.",
//                        Toast.LENGTH_SHORT).show()
//                } else {
//                    // If sign in fails, display a message to the user.
//                    Toast.makeText(baseContext, "Authentication failed.",
//                        Toast.LENGTH_SHORT).show()
//                }
//            }
    }


    private fun LoginFirebase(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    val db = Firebase.firestore
                    db.collection("users").get().addOnSuccessListener { result ->
                        for (document in result) {
                            if (document.data["email"] == email) {
                                Toast.makeText(this, "Berhasil Masuk", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, Home::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

//    private fun addToDatabase(nama: String?, email: String, noHp: String?, uid: String) {
//        val db = Firebase.firestore
//        val user = hashMapOf(
//            "nama" to  nama,
//            "email" to email,
//            "noHp" to noHp,
//            "uid" to uid,
//        )
//
//        db.collection("users").document(uid).set(user).addOnSuccessListener {
//            Log.d("TAG", "DocumentSnapshot successfully written!")
//        }.addOnFailureListener {
//            Log.w("TAG", "Error writing document", it)
//        }
//
//        database = FirebaseDatabase.getInstance()
//        val myRef: DatabaseReference = database.getReference("users")
//        myRef.child(uid).setValue(Users(uid, nama, email, noHp))
//
//    }



}