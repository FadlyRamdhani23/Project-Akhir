package com.tugasakhir.udmrputra.ui.splashscreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.databinding.ActivityMainBinding
import com.tugasakhir.udmrputra.ui.Home
import com.tugasakhir.udmrputra.ui.logreg.LoginActivity
import com.tugasakhir.udmrputra.ui.mitra.HomeMitraActivity
import com.tugasakhir.udmrputra.ui.onboarding.OnboardingActivity
import com.tugasakhir.udmrputra.ui.sopir.HomeSupirActivity

class MainActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get().addOnSuccessListener {
                if (it.exists()) {
                    val role = it.getString("status")
                    if (role == "admin") {
                        val i = Intent(this, Home::class.java)
                        startActivity(i)
                        finish()
                    } else if (role == "supir") {
                        startActivity(Intent(this, HomeSupirActivity::class.java))
                        finish()
                    } else {
                        startActivity(Intent(this, HomeMitraActivity::class.java))
                        finish()
                    }
                }
            }
        } else {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        }
    }
}