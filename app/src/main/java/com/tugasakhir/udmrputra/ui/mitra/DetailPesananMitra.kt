package com.tugasakhir.udmrputra.ui.mitra

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tugasakhir.udmrputra.data.DetailPengajuan
import com.tugasakhir.udmrputra.databinding.ActivityDetailPesananMitraBinding
import com.tugasakhir.udmrputra.ui.chat.ChatActivity
import com.tugasakhir.udmrputra.ui.pengajuan.PengajuanAdapterDetailBarang

class DetailPesananMitra : AppCompatActivity() {

    private lateinit var binding: ActivityDetailPesananMitraBinding
    private lateinit var adapter: PengajuanAdapterDetailBarang
    private lateinit var pengajuanDetailAdapter: PengajuanAdapterDetailBarang
    private val pengajuanList = mutableListOf<DetailPengajuan>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPesananMitraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pengajuanId = intent.getStringExtra("pengajuanId")
        binding.pengajuanId.text = intent.getStringExtra("pengajuanId")
        binding.userId.text = intent.getStringExtra("userId")

        setupRecyclerView()

        fetchPengajuanData(pengajuanId.toString())

        binding.chatButton.setOnClickListener {
            createOrFindChatRoom()
        }

    }
    private fun createOrFindChatRoom() {
        val db = Firebase.firestore
        val auth = Firebase.auth
        val user = auth.currentUser
        val uid = user?.uid
        val username = user?.displayName
        val adminId = "7FnEe053S1PssDMv0RpGz9R3LHA2"
        val adminName = "Admin"

        val chatRoom = hashMapOf(
            "userIdAdmin" to adminId,
            "userIdMitra" to uid,
            "usernameAdmin" to adminName,
            "usernameMitra" to username,
            "lastMessage" to "",
            "lastMessageTimestamp" to "",
        )
        db.collection("chatRoom").get()
            .addOnSuccessListener { documents ->
                var isExist = false
                val chatroomId = ""
                for (document in documents) {
                    if (document.data["userIdAdmin"] == adminId && document.data["userIdMitra"] == uid) {
                        isExist = true
                        break
                    }
                }
                if (isExist) {
                    db.collection("chatRoom")
                        .whereEqualTo("userIdAdmin", adminId)
                        .whereEqualTo("userIdMitra", uid)
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                val chatroomId = document.id
                                val intent = android.content.Intent(
                                    this,
                                    ChatActivity::class.java
                                )
                                intent.putExtra(
                                    "chatroomId",
                                    chatroomId
                                )
                                startActivity(intent)
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Chat room failed to create",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    db.collection("chatRoom").add(chatRoom)
                        .addOnSuccessListener {
                            val intent = android.content.Intent(
                                this,
                                ChatActivity::class.java
                            )
                            intent.putExtra("chatroomId", it.id)
                            startActivity(intent)
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

                binding.tvNamaPengaju.text = namaPengaju
                binding.tvNamaMitra.text = namaPengaju
                binding.tvAlamatMitra.text = address
                binding.tvStatus.text = statusPengajuan

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
                                idPengiriman,
                                tanggalPengajuan,
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
        binding.rvListBarang.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pengajuanDetailAdapter
        }
    }

}