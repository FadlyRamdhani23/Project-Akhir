package com.tugasakhir.udmrputra.ui.sopir

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.tugasakhir.udmrputra.databinding.ActivityHomeSupirActivityBinding

class HomeSupirActivity : AppCompatActivity() {

    private lateinit var binding : ActivityHomeSupirActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeSupirActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnToPengiriman.setOnClickListener {
            val intent = Intent(this, DaftarPengirimanToday::class.java)
            startActivity(intent)
            binding.progressBarPengiriman.visibility = View.VISIBLE
        }
        binding.btnToProfil.setOnClickListener {
            val intent = Intent(this, ProfilSupirActivity::class.java)
            startActivity(intent)
            binding.progressBarProfile.visibility = View.VISIBLE
        }
        binding.btnToPengaturan.setOnClickListener {
            val intent = Intent(this, RiwayatPengirimanSupirActivity::class.java)
            startActivity(intent)
            binding.progressBarRiwayat.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        // Hide the ProgressBar when the activity resumes
        binding.progressBarPengiriman.visibility = View.GONE
        binding.progressBarProfile.visibility = View.GONE
        binding.progressBarRiwayat.visibility = View.GONE
    }

}