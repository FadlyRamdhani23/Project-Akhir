package com.tugasakhir.udmrputra.ui.chat

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.MessageRoomModel
import com.tugasakhir.udmrputra.databinding.ChatMessageRecyclerBinding
import com.tugasakhir.udmrputra.databinding.ImageMessageBinding

class MessageRecyclerAdapter(
    private val options: FirebaseRecyclerOptions<MessageRoomModel>,
    private val currentUserName: String?
) : FirebaseRecyclerAdapter<MessageRoomModel, RecyclerView.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_TEXT) {
            val binding = ChatMessageRecyclerBinding.inflate(inflater, parent, false)
            MessageViewHolder(binding)
        } else {
            val binding = ImageMessageBinding.inflate(inflater, parent, false)
            ImageMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: MessageRoomModel) {
        if (getItemViewType(position) == VIEW_TYPE_TEXT) {
            (holder as MessageViewHolder).bind(model)
        } else {
            (holder as ImageMessageViewHolder).bind(model)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (options.snapshots[position].text != null) VIEW_TYPE_TEXT else VIEW_TYPE_IMAGE
    }

    inner class MessageViewHolder(
        private val binding: ChatMessageRecyclerBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: MessageRoomModel) {
            binding.apply {
                leftChatTextview.text = chat.message
                rightChatTextview.text = chat.message
                leftChatTimeview.text = chat.time
                rightChatTimeview.text = chat.time

                val isCurrentUser = chat.userId == currentUserName
                leftChatLayout.visibility = if (isCurrentUser) android.view.View.GONE else android.view.View.VISIBLE
                rightChatLayout.visibility = if (isCurrentUser) android.view.View.VISIBLE else android.view.View.GONE
                leftChatTimeview.visibility = if (isCurrentUser) android.view.View.GONE else android.view.View.VISIBLE
                rightChatTimeview.visibility = if (isCurrentUser) android.view.View.VISIBLE else android.view.View.GONE

                root.setOnClickListener {
                    val message = root.context.getString(R.string.message, chat.username)
                    Toast.makeText(root.context, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    inner class ImageMessageViewHolder(
        private val binding: ImageMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MessageRoomModel) {
            binding.apply {
                loadImageIntoView(messageImageViewLeft, item.imageUrl!!, false)
                loadImageIntoView(messageImageViewRight, item.imageUrl!!, false)

                val isCurrentUser = item.username == currentUserName
                messageImageViewLeft.visibility = if (isCurrentUser) android.view.View.GONE else android.view.View.VISIBLE
                messageImageViewRight.visibility = if (isCurrentUser) android.view.View.VISIBLE else android.view.View.GONE
            }
        }

        private fun loadImageIntoView(view: ImageView, url: String, isCircular: Boolean) {
            if (url.startsWith("gs://")) {
                val storageReference = Firebase.storage.getReferenceFromUrl(url)
                storageReference.downloadUrl
                    .addOnSuccessListener { uri ->
                        loadWithGlide(view, uri.toString(), isCircular)
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Getting download url was not successful.", e)
                    }
            } else {
                loadWithGlide(view, url, isCircular)
            }
        }
    }

    private fun loadWithGlide(view: ImageView, url: String, isCircular: Boolean) {
        var requestBuilder = Glide.with(view.context).load(url)
        if (isCircular) {
            requestBuilder = requestBuilder.transform(CircleCrop())
        }
        requestBuilder.into(view)
    }

    companion object {
        const val TAG = "MessageAdapter"
        const val VIEW_TYPE_TEXT = 1
        const val VIEW_TYPE_IMAGE = 2
    }
}
