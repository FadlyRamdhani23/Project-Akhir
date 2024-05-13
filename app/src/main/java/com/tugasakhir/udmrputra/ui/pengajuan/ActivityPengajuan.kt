package com.tugasakhir.udmrputra.ui.pengajuan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.databinding.ActivityPengajuanBinding

class ActivityPengajuan : AppCompatActivity() {

    private lateinit var binding: ActivityPengajuanBinding
    private var isCardViewExpanded = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPengajuanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabAddBarang.setOnClickListener {
            // Inflate layout untuk CardView
            val newCardView = LayoutInflater.from(this).inflate(R.layout.item_card_pengajuan, null, false)

            // Tambahkan CardView ke LinearLayout
            binding.cardViewPengajuan2.addView(newCardView)
        }



        binding.btnshow.setOnClickListener {
            if (isCardViewExpanded) {
                // Mengecilkan CardView
                val params = binding.cardViewPengajuan.layoutParams
                params.height = 200 // Anda dapat mengatur ini sesuai kebutuhan Anda
                binding.cardViewPengajuan.layoutParams = params
            } else {
                // Melebarkan CardView
                val params = binding.cardViewPengajuan.layoutParams
                params.height = LinearLayout.LayoutParams.MATCH_PARENT
                binding.cardViewPengajuan.layoutParams = params
            }
            isCardViewExpanded = !isCardViewExpanded
        }
    }
}