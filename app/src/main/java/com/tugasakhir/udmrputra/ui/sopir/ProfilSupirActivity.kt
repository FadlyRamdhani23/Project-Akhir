package com.tugasakhir.udmrputra.ui.sopir

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tugasakhir.udmrputra.databinding.ProfileSupirActivityBinding

class ProfilSupirActivity : AppCompatActivity() {

    private lateinit var binding :ProfileSupirActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProfileSupirActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}