package com.tugasakhir.udmrputra.ui.mitra

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tugasakhir.udmrputra.databinding.ActivityHomeMitraBinding

class HomeMitraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeMitraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeMitraBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}