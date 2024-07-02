package com.tugasakhir.udmrputra.ui.pengajuan

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tugasakhir.udmrputra.data.DetailPengajuan
import com.tugasakhir.udmrputra.databinding.ActivityDetailPengajuanBinding
import com.tugasakhir.udmrputra.ui.chat.ChatActivity
import java.text.NumberFormat
import java.util.Locale

class DetailPengajuanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailPengajuanBinding
    private lateinit var adapter: PengajuanAdapterDetailBarang

    private lateinit var pengajuanDetailAdapter: PengajuanAdapterDetailBarang
    private val pengajuanList = mutableListOf<DetailPengajuan>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPengajuanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pengajuanId = intent.getStringExtra("pengajuanId")
        binding.pengajuanId.text = intent.getStringExtra("pengajuanId")
        binding.userId.text = intent.getStringExtra("userId")

        setupRecyclerView()

        fetchPengajuanData(pengajuanId.toString())
        binding.chatButton.setOnClickListener {
            createOrFindChatRoom(pengajuanId.toString())
        }
        // Update your TextInputEditText to format currency input
        binding.totalHargaDeal.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                binding.totalHargaDeal.removeTextChangedListener(this)
                try {
                    val originalString = s.toString()

                    // Remove formatting characters
                    val cleanString = originalString.replace("[Rp,.]".toRegex(), "")

                    val longVal: Long = cleanString.toLong()

                    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                    formatter.maximumFractionDigits = 0
                    val formattedString = formatter.format(longVal)

                    // Update input field with formatted currency
                    binding.totalHargaDeal.setText(formattedString)
                    binding.totalHargaDeal.setSelection(formattedString.length)
                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace()
                }

                binding.totalHargaDeal.addTextChangedListener(this)
            }
        })

        // Add click listener for "Terima" button
        binding.btnTerima.setOnClickListener {
            saveTotalHarga(pengajuanId.toString())
        }
    }

    private fun fetchPengajuanData(pengajuanId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pengajuan").document(pengajuanId)
            .get()
            .addOnSuccessListener { result ->
                val namaPengaju = result.getString("namaPetani") ?: ""
                val tanggalPengajuan = result.getString("tanggalPengajuan") ?: ""
                val statusPengajuan = result.getString("status") ?: ""
                val address = result.getString("address") ?: ""
                val jenisPembayaran = result.getString("jenisPembayaran") ?: ""
                val idPengiriman = result.getString("idPengiriman") ?: ""
                val hargaDeal = result.getLong("totalHarga") ?: 0

                binding.tvNamaPengaju.text = namaPengaju
                binding.tvNamaMitra.text = namaPengaju
                binding.tvAlamatMitra.text = address
                binding.tvStatus.text = statusPengajuan

                if(idPengiriman == ""){
                    binding.cardViewPengiriman.visibility = android.view.View.GONE
                } else {
                    binding.cardViewPengiriman.visibility = android.view.View.VISIBLE
                    db.collection("pengiriman").document(idPengiriman).get()
                        .addOnSuccessListener { pengirimanResult ->
                            val namaSupir = pengirimanResult.getString("supir") ?: ""
                            binding.tvNamaSupir.text = "Mana supir : $namaSupir"
                        }
                }

                if (hargaDeal == 0L) {
                    Log.d("DetailPengajuanActivity", "Harga deal is 0")
                } else {
                    binding.totalHargaDeal.isFocusable = false
                    binding.totalHargaDeal.isFocusableInTouchMode = false
                    binding.totalHargaDeal.isClickable = false
                    binding.totalHargaDeal.setText(hargaDeal.toString())
                    binding.btnTerima.visibility = android.view.View.GONE
                    binding.btnTolak.visibility = android.view.View.GONE
                    binding.totalHargaDealLayout.hint = "Harga yang disepakati"

                }


                db.collection("pengajuan").document(pengajuanId).collection("barang")
                    .get()
                    .addOnSuccessListener { barangResult ->
                        for (barangDocument in barangResult) {
                            val barangId = barangDocument.getString("barangId") ?: ""
                            val namaBarang = barangDocument.getString("namaBarang") ?: ""
                            val hargaPasar: Long? = barangDocument.getLong("hargaPasar")
                            val hargaBeli: Long? = barangDocument.getLong("hargaBeli")
                            val jumlah: Long? = barangDocument.getLong("jumlahBarang")
                            val catatan = barangDocument.getString("catatan") ?: ""
                            val imageUrls =  barangDocument.getString("imageUrl") ?: ""

                            val detailPengajuan = DetailPengajuan(
                                pengajuanId,
                                namaBarang,
                                jumlah,
                                hargaPasar,
                                hargaBeli,
                                catatan,
                                jenisPembayaran,
                                tanggalPengajuan,
                                idPengiriman,
                                imageUrls,
                            )
                            pengajuanList.add(detailPengajuan)
                        }
                        pengajuanDetailAdapter.notifyDataSetChanged()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Failed to fetch pengajuan details",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun setupRecyclerView() {
        pengajuanDetailAdapter = PengajuanAdapterDetailBarang(this,pengajuanList)
        binding.recylerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pengajuanDetailAdapter
        }
    }


    private fun createOrFindChatRoom(pengajuanId: String) {
        val db = Firebase.firestore
        val auth = Firebase.auth
        val user = auth.currentUser
        val uid = user?.uid
        val username = "Admin"

        db.collection("pengajuan").document(pengajuanId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val mitraId = document.getString("userId")
                    val sellerName = document.getString("namaPetani")
                    val chatRoom = hashMapOf(
                        "userIdAdmin" to uid,
                        "userIdMitra" to mitraId,
                        "usernameAdmin" to username,
                        "usernameMitra" to sellerName,
                        "lastMessage" to "",
                        "lastMessageTimestamp" to "",
                    )

                    db.collection("chatRoom").get()
                        .addOnSuccessListener { documents ->
                            var isExist = false
                            var chatroomId = ""
                            for (document in documents) {
                                if (document.data["userIdAdmin"] == uid && document.data["userIdMitra"] == mitraId) {
                                    isExist = true
                                    chatroomId = document.id
                                    break
                                }
                            }

                            if (isExist) {
                                startChatActivity(chatroomId)
                            } else {
                                db.collection("chatRoom").add(chatRoom)
                                    .addOnSuccessListener { documentReference ->
                                        startChatActivity(documentReference.id)
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            this,
                                            "Chat room failed to create",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Failed to fetch pengajuan details",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun startChatActivity(chatroomId: String) {
        val intent = android.content.Intent(this, ChatActivity::class.java)
        intent.putExtra("chatroomId", chatroomId)
        startActivity(intent)
    }

    private fun saveTotalHarga(pengajuanId: String) {
        val totalHargaDeal = binding.totalHargaDeal.text.toString().replace("[Rp,.]".toRegex(), "").toLongOrNull()
        if (totalHargaDeal != null) {
            val db = FirebaseFirestore.getInstance()
            val updateData = hashMapOf(
                "totalHarga" to totalHargaDeal,
                "status" to "approved"
            )
            db.collection("pengajuan").document(pengajuanId)
                .update(updateData as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Total harga berhasil disimpan", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menyimpan total harga", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Total harga tidak valid", Toast.LENGTH_SHORT).show()
        }
    }
}
