package com.tugasakhir.udmrputra.ui.sopir

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tugasakhir.udmrputra.databinding.ActivityHomeSupirActivityBinding

class HomeSupirActivity : AppCompatActivity() {

    private lateinit var binding : ActivityHomeSupirActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeSupirActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnToPengiriman.setOnClickListener {
            val intent = Intent(this, SupirActivity::class.java)
            startActivity(intent)
        }
    }

}