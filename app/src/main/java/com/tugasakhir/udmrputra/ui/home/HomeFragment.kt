package com.tugasakhir.udmrputra.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.tugasakhir.udmrputra.data.JumlahBarang
import com.tugasakhir.udmrputra.databinding.FragmentHomeBinding
import com.tugasakhir.udmrputra.ui.barang.BarangActivity
import com.tugasakhir.udmrputra.ui.dashboard.sopir.DaftarSopir
import com.tugasakhir.udmrputra.ui.mitra.DaftarMitra
import com.tugasakhir.udmrputra.ui.pengajuan.ActivityPengajuan

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: View
    private lateinit var imageView: View
    private lateinit var pieChartView: View

    private val binding get() = _binding!!

    private var jumlahBarang = JumlahBarang()
    private lateinit var firestoreListener: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        progressBar = binding.progressBar
        imageView = binding.imageVaccine
        pieChartView = binding.pieChartView



        progressBar.visibility = View.VISIBLE // Show progress bar while loading data

        user?.let {
            val email = user.email
            val uid = user.uid
            Log.d("HomeFragment", "Email: $email")
            Log.d("HomeFragment", "UID: $uid")
        }
        Log.d("HomeFragment", "User: $user")

        setupPieChart()
        loadBarangData()

        binding.pieChartView.setOnClickListener {
            val intent = Intent(activity, BarangActivity::class.java)
            startActivity(intent)
        }

        binding.toDaftarMitra.setOnClickListener {
            val intent = Intent(activity, DaftarMitra::class.java)
            startActivity(intent)
        }
        binding.pengajuan.setOnClickListener {
            val intent = Intent(activity, ActivityPengajuan::class.java)
            startActivity(intent)
        }

        binding.toDaftarSopir.setOnClickListener {
            val intent = Intent(activity, DaftarSopir::class.java)
            startActivity(intent)
        }

        return root
    }

    private fun setupPieChart() {
        val pieChartView = binding.pieChartView
        pieChartView.setData(jumlahBarang.jumlahBuah, jumlahBarang.jumlahSayur)
    }

    private fun loadBarangData() {
        // Reset the values before loading data
        jumlahBarang = JumlahBarang()

        val db = FirebaseFirestore.getInstance()
        firestoreListener = db.collection("barang")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("HomeFragment", "Listen failed.", e)
                    progressBar.visibility = View.GONE
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    jumlahBarang = JumlahBarang() // Reset values each time data changes
                    for (document in snapshots) {
                        val catId = document.getString("catId").toString()
                        val jumlah = document.getDouble("jumlah") ?: 0.0 // Use getDouble

                        db.collection("kategori").document(catId)
                            .get()
                            .addOnSuccessListener { documentSnapshot ->
                                val catName = documentSnapshot.getString("nama").toString()
                                when (catName.lowercase()) {
                                    "sayur" -> jumlahBarang.jumlahSayur += jumlah.toFloat()
                                    "buah" -> jumlahBarang.jumlahBuah += jumlah.toFloat()
                                }
                                setupPieChart() // Update PieChart every time data changes
                            }
                            .addOnFailureListener { e ->
                                Log.w("HomeFragment", "Error getting category name", e)
                            }
                    }
                    Log.d("HomeFragment", "Data displayed successfully")
                    progressBar.visibility = View.GONE
                    imageView.visibility = View.VISIBLE
                    pieChartView.visibility = View.VISIBLE
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        firestoreListener.remove() // Remove Firestore listener when the view is destroyed
    }
}
