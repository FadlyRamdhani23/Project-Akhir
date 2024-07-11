package com.tugasakhir.udmrputra.ui.mitra

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

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
            .addSnapshotListener { result, error ->
                if (error != null) {
                    Toast.makeText(
                        this,
                        "Failed to fetch pengajuan details",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                result?.let {
                    val namaPengaju = it.getString("namaPetani") ?: ""
                    val tanggalPengajuanTimestamp = it.get("tanggalPengajuan") // Read without casting
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                    val tanggalPengajuan = if (tanggalPengajuanTimestamp is com.google.firebase.Timestamp) {
                        dateFormat.format(tanggalPengajuanTimestamp.toDate())
                    } else {
                        // Handle the case where tanggalPengajuan is not a Timestamp
                        ""
                    }
                    val statusPengajuan = it.getString("status") ?: ""
                    val address = it.getString("address") ?: ""
                    val jenisPembayaran = it.getString("jenisPembayaran") ?: ""
                    val idPengiriman = it.getString("idPengiriman") ?: ""
                    val hargaDeal = it.getLong("totalHarga") ?: 0
                    binding.tvNamaPengaju.text = namaPengaju
                    binding.tvNamaMitra.text = namaPengaju
                    binding.tvAlamatMitra.text = address
                    binding.tvStatus.text = statusPengajuan

                    db.collection("pengajuan").document(pengajuanId).collection("barang")
                        .addSnapshotListener { barangResult, error ->
                            if (error != null) {
                                Toast.makeText(
                                    this,
                                    "Failed to fetch barang details",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@addSnapshotListener
                            }

                            barangResult?.let { snapshot ->
                                pengajuanList.clear()
                                for (barangDocument in snapshot) {
                                    val barangId = barangDocument.getString("barangId") ?: ""
                                    val namaBarang = barangDocument.getString("namaBarang") ?: ""
                                    val hargaPasar: Long? = barangDocument.getLong("hargaPasar")
                                    val hargaBeli: Long? = barangDocument.getLong("hargaBeli")
                                    val jumlah: Long? = barangDocument.getLong("jumlahBarang")
                                    val catatan = barangDocument.getString("catatan") ?: ""
                                    val imageUrls = barangDocument.getString("imageUrl") ?: ""
                                    binding.totalHargaDeal.setText(hargaDeal.toString())

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
                }
            }
    }


    private fun setupRecyclerView() {
        pengajuanDetailAdapter = PengajuanAdapterDetailBarang(this, pengajuanList)
        binding.rvListBarang.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pengajuanDetailAdapter
        }
    }

}