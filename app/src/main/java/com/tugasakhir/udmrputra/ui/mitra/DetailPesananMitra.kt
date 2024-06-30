package com.tugasakhir.udmrputra.ui.mitra

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.databinding.ActivityDetailPengajuanBinding
import com.tugasakhir.udmrputra.ui.chat.ChatActivity

class DetailPesananMitra : AppCompatActivity() {

    private lateinit var binding: ActivityDetailPengajuanBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPengajuanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pengajuanId = intent.getStringExtra("pengajuanId")
        binding.pengajuanId.text = intent.getStringExtra("pengajuanId")
        binding.userId.text = intent.getStringExtra("userId")

        binding.chatButton.setOnClickListener {
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
    }
}