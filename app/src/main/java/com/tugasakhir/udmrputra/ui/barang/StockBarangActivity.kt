package com.tugasakhir.udmrputra.ui.barang

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.databinding.ActivityBarangBinding
import com.tugasakhir.udmrputra.databinding.ActivityStockBarangBinding

class StockBarangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStockBarangBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BarangAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_barang)
    }
}