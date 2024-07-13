package com.tugasakhir.udmrputra.ui.chat

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.tugasakhir.udmrputra.data.ChatRoomModel
import com.tugasakhir.udmrputra.data.MessageRoomModel
import com.tugasakhir.udmrputra.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var myAdapter: MessageRecyclerAdapter
    private val database = Firebase.database.reference
    private val openDocument = registerForActivityResult(MyOpenDocumentContract()) { uri ->
        uri?.let { onImageSelected(it) }
    }
    companion object {
        var isChatActivityActive = false
    }

    private lateinit var chatroomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isChatActivityActive = true
        val intent = intent
        val auth = Firebase.auth
        val user = auth.currentUser
        val uid = user?.uid

        chatroomId = intent.getStringExtra("chatroomId").toString()
        val db = FirebaseFirestore.getInstance()
        val chatRoomRef = db.collection("chatRoom").document(chatroomId)

        chatRoomRef.get().addOnSuccessListener {
            val chatRoom = ChatRoomModel(
                it.id,
                it.data?.get("userIdAdmin").toString(),
                it.data?.get("userIdMitra").toString(),
                it.data?.get("usernameAdmin").toString(),
                it.data?.get("usernameMitra").toString(),
                it.data?.get("userImageBuyer").toString(),
                it.data?.get("userImageSeller").toString(),
                it.data?.get("lastMessage").toString(),
                it.data?.get("lastMessageTimestamp").toString(),
            )
            if (chatRoom.userIdBuyer == uid) {
                binding.otherUsername.text = chatRoom.usernameSeller
                Glide.with(this)
                    .load(chatRoom.userImageSeller)
                    .circleCrop()
                    .into(binding.profilePicImageView)
            } else {
                binding.otherUsername.text = chatRoom.usernameBuyer
                Glide.with(this)
                    .load(chatRoom.userImageBuyer)
                    .circleCrop()
                    .into(binding.profilePicImageView)
            }
        }

        binding.messageSendBtn.setOnClickListener {
            val message = binding.chatMessageInput.text.toString()
            if (message.isEmpty()) {
                Log.d("ChatActivity", "Message is empty, not sending")
                return@setOnClickListener
            }

            val calendar = java.util.Calendar.getInstance()
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val minute = calendar.get(java.util.Calendar.MINUTE)
            val time = if (hour < 10 && minute < 10) {
                "0$hour.0$minute"
            } else if (hour < 10) {
                "0$hour.$minute"
            } else if (minute < 10) {
                "$hour.0$minute"
            } else {
                "$hour.$minute"
            }
            db.collection("users").document(uid.toString()).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username1 = document.getString("nama")
                        val messageRoom = MessageRoomModel(
                            text = message,
                            chatroomId = chatroomId,
                            userId = uid.toString(),
                            username = username1,
                            message = message,
                            imageUrl = null,
                            time = time
                        )

                        database.child("messageRoom").child(chatroomId).push().setValue(messageRoom)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("ChatActivity", "Message sent successfully")
                                    binding.chatMessageInput.text.clear()
                                } else {
                                    Log.e("ChatActivity", "Failed to send message", task.exception)
                                    Toast.makeText(this, "Failed to send message: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Log.d("ChatActivity", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("ChatActivity", "get failed with ", exception)
                }

        }

        binding.chatMessageInput.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                binding.messageSendBtn.performClick()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        val messageRef = database.child("messageRoom").child(chatroomId)
        val options = FirebaseRecyclerOptions.Builder<MessageRoomModel>()
            .setQuery(messageRef, MessageRoomModel::class.java)
            .build()
        db.collection("users").document(uid.toString()).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    myAdapter = MessageRecyclerAdapter(options, uid.toString())
                    val manager = LinearLayoutManager(this)
                    manager.stackFromEnd = true
                    binding.chatRecyclerView.layoutManager = manager
                    binding.chatRecyclerView.adapter = myAdapter

                    myAdapter.registerAdapterDataObserver(
                        MyScrollToBottomObserver(binding.chatRecyclerView, myAdapter, manager)
                    )
                    myAdapter.startListening()
                } else {
                    Log.d("ChatActivity", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("ChatActivity", "get failed with ", exception)
            }

        binding.addMessageImageView.setOnClickListener {
            openDocument.launch(arrayOf("image/*"))
        }
    }

    private fun onImageSelected(uri: Uri) {
        Log.d("ChatActivity", "Uri: $uri")
        val auth = Firebase.auth
        val user = auth.currentUser
        val username = user?.displayName

        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)
        val time = if (hour < 10 && minute < 10) {
            "0$hour.0$minute"
        } else if (hour < 10) {
            "0$hour.$minute"
        } else if (minute < 10) {
            "$hour.0$minute"
        } else {
            "$hour.$minute"
        }

        val tempMessage = MessageRoomModel(null, chatroomId,
            username.toString(), "$username send a photo", "https://www.google.com/images/spin-32.gif", time)
        database.child("messageRoom").child(chatroomId).push().setValue(tempMessage) { databaseError, databaseReference ->
            if (databaseError == null) {
                val key = databaseReference.key
                val storageReference = Firebase.storage.getReference("message").child(key!!).child(uri.lastPathSegment!!)
                putImageInStorage(storageReference, uri, key)
            } else {
                Log.w("ChatActivity", "Unable to write message to database.", databaseError.toException())
            }
        }
    }

    private fun putImageInStorage(storageReference: StorageReference, uri: Uri, key: String?) {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)
        val time = if (hour < 10 && minute < 10) {
            "0$hour.0$minute"
        } else if (hour < 10) {
            "0$hour.$minute"
        } else if (minute < 10) {
            "$hour.0$minute"
        } else {
            "$hour.$minute"
        }

        val auth = Firebase.auth
        val user = auth.currentUser
        val username = user?.displayName

        storageReference.putFile(uri)
            .addOnSuccessListener(this) { taskSnapshot ->
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { downloadUri ->
                    val friendlyMessage = MessageRoomModel(
                        text = null,
                        chatroomId = chatroomId,
                        userId = user!!.uid,
                        username = username.toString(),
                        message = "$username send a photo",
                        imageUrl = downloadUri.toString(),
                        time = time
                    )
                    database.child("messageRoom").child(chatroomId).child(key!!).setValue(friendlyMessage).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("ChatActivity", "Image message sent successfully")
                        } else {
                            Log.e("ChatActivity", "Failed to send image message", task.exception)
                        }
                    }
                }
            }
            .addOnFailureListener(this) { e ->
                Log.w("ChatActivity", "Image upload task was unsuccessful.", e)
            }
    }

    public override fun onPause() {
        if (::myAdapter.isInitialized) {
            myAdapter.stopListening()

        }
        isChatActivityActive = false

        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        if (::myAdapter.isInitialized) {
            myAdapter.startListening()
        }
        isChatActivityActive = true

    }
}
