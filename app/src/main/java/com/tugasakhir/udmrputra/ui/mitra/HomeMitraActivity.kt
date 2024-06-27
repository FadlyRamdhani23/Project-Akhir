package com.tugasakhir.udmrputra.ui.mitra

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.tugasakhir.udmrputra.databinding.ActivityHomeMitraBinding
import com.tugasakhir.udmrputra.ui.pengajuan.ActivityPengajuan
import com.tugasakhir.udmrputra.ui.pengaturan.PengaturanFragment

class HomeMitraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeMitraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeMitraBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        binding.btnToPengaturan.setOnClickListener {
//            val intent = Intent(this, PengaturanFragment::class.java)
//            startActivity(intent)
//        }
        binding.btnToPengiriman.setOnClickListener {
            val intent = Intent(this, ProfilMitraActivity::class.java)
            startActivity(intent)
            binding.progressBar.visibility = View.VISIBLE
        }
        binding.btnToProfil.setOnClickListener {
            val intent = Intent(this, ActivityPengajuan::class.java)
            startActivity(intent)
        }
    }
    override fun onResume() {
        super.onResume()
        // Hide the ProgressBar when the activity resumes
        binding.progressBar.visibility = View.GONE
    }
}