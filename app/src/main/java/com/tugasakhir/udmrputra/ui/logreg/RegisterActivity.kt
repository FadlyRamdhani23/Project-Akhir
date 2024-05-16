package com.tugasakhir.udmrputra.ui.logreg

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.tugasakhir.udmrputra.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener{
            registerUser()
        }
    }

    private fun registerUser(){
        val nama = binding.namaTextField.toString()
        val email = binding.emailTextField.toString()
        val password = binding.sandiTextField.toString()
        val konfirmasiPassword = binding.konfirmasiTextField.toString()
        val noHp = binding.nomorTextField.toString()

        if(nama.isEmpty() || email.isEmpty() || password.isEmpty() || konfirmasiPassword.isEmpty() || noHp.isEmpty()){
            Toast.makeText(this, "Pastikan form tidak ada yang kosong!", Toast.LENGTH_SHORT).show()
            return
        }
    }
}