package com.tugasakhir.udmrputra.ui.mitra

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.midtrans.sdk.corekit.core.PaymentMethod
import com.midtrans.sdk.uikit.api.model.*
import com.midtrans.sdk.uikit.external.UiKitApi
import com.tugasakhir.udmrputra.R
import com.tugasakhir.udmrputra.data.Pengajuan
import com.tugasakhir.udmrputra.ui.chat.ChatActivity
import com.tugasakhir.udmrputra.ui.pengiriman.MapsActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*

class PesananMitraAdapter(
    private val pengajuanList: List<Pengajuan>,
    private val activity: Activity, // Pass the Activity context here
    private val paymentLauncher: ActivityResultLauncher<Intent>
) : RecyclerView.Adapter<PesananMitraAdapter.PesananViewHolder>() {

    init {
        // Initialize UiKitApi
        buildUiKit(activity)
    }

    class PesananViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tanggalPesanan: TextView = view.findViewById(R.id.tv_tanggal)
        val barang: TextView = view.findViewById(R.id.tv_item_nama)
        val status: TextView = view.findViewById(R.id.tv_status)
        val jumlahBarang: TextView = view.findViewById(R.id.tv_jumlah)
        val totalHarga: TextView = view.findViewById(R.id.tv_total_harga)
        val llTrackPengiriman: LinearLayout = view.findViewById(R.id.ll_track_pengiriman)
        val viewTrackPengiriman: View = view.findViewById(R.id.view_track_pengiriman)
        val btnTerima: TextView = view.findViewById(R.id.btn_terima)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PesananViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_pesanan_mitra, parent, false)
        return PesananViewHolder(view)
    }

    override fun onBindViewHolder(holder: PesananViewHolder, position: Int) {
        val pengajuan = pengajuanList[position]
        holder.tanggalPesanan.text = pengajuan.tanggalPengajuan
        holder.barang.text = pengajuan.listBarang.joinToString(", ")
        holder.status.text = pengajuan.statusPengajuan
        holder.jumlahBarang.text = "${pengajuan.listBarang.size} Barang"
        when (pengajuan.statusPengajuan) {
            "Penawaran" -> {
                holder.totalHarga.text = "Harga belum disetujui"
            }
            "Ditolak" -> {
                holder.totalHarga.text = "Harga tidak disetujui"
            }
            else -> {
                val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                holder.totalHarga.text = numberFormat.format(pengajuan.totalHarga)
            }
        }

        if (pengajuan.statusPengajuan == "pengiriman" || pengajuan.statusPengajuan == "Dikemas") {
            holder.llTrackPengiriman.visibility = View.VISIBLE
            holder.viewTrackPengiriman.visibility = View.VISIBLE
        } else {
            holder.llTrackPengiriman.visibility = View.GONE
            holder.viewTrackPengiriman.visibility = View.GONE
        }

        holder.llTrackPengiriman.setOnClickListener {
            val intent = Intent(holder.itemView.context, MapsActivity::class.java).apply {
                putExtra("PENGIRIMAN_ID", pengajuan.idPengiriman)
            }
            holder.itemView.context.startActivity(intent)
        }

        holder.btnTerima.apply {
            when (pengajuan.statusPengajuan) {
                "pengiriman" -> {
                    text = "Diterima"
                    isEnabled = true
                    visibility = View.VISIBLE
                    setOnClickListener {
                        updateStatus(pengajuan, holder, "selesai")
                    }
                }
                "Penawaran" -> {
                    text = "Chat Admin"
                    isEnabled = true
                    visibility = View.VISIBLE
                    setOnClickListener {
                        createOrFindChatRoom(holder.itemView.context)
                    }
                }
                "Bayar" -> {
                    text = "Bayar"
                    isEnabled = true
                    visibility = View.VISIBLE
                    setOnClickListener {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("transaksi")
                            .whereEqualTo("order_id", pengajuan.id)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (documents.isEmpty) {
                                    // If order_id does not exist, proceed with payment
                                    val transactionDetails = SnapTransactionDetail(
                                        orderId = pengajuan.id,
                                        grossAmount = pengajuan.totalHarga?.toDouble() ?: 0.0
                                    )

                                    val customerDetails = CustomerDetails(
                                        firstName = pengajuan.userId,
                                        customerIdentifier = "mail@mail.com",
                                        email = "mail@mail.com",
                                        phone = "085310102020"
                                    )
                                    val itemDetails = listOf(
                                        ItemDetails(
                                            pengajuan.id,
                                            pengajuan.totalHarga?.toDouble() ?: 0.0,
                                            1,
                                            pengajuan.listBarang.joinToString(", ")
                                        )
                                    )

                                    UiKitApi.getDefaultInstance().startPaymentUiFlow(
                                        activity = activity,
                                        launcher = paymentLauncher,
                                        transactionDetails = transactionDetails,
                                        customerDetails = customerDetails,
                                        itemDetails = itemDetails,
                                    )
                                } else {
                                    // If order_id exists, navigate to DetailPembayaran
                                    val intent = Intent(activity, DetailPembayaran::class.java).apply {
                                        putExtra("order_id", pengajuan.id)
                                    }
                                    activity.startActivity(intent)
                                }
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(
                                    activity,
                                    "Error checking transaction: ${exception.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.e("PesananMitraAdapter", "Error checking transaction", exception)
                            }
                    }
                }
                "selesai" -> {
                    text = "Selesai"
                    isEnabled = false
                    visibility = View.VISIBLE
                }
                "Disetujui" -> {
                    text = "Diterima"
                    isEnabled = false
                    visibility = View.VISIBLE
                }
                else -> {
                    visibility = View.GONE
                }
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailPesananMitra::class.java).apply {
                putExtra("pengajuanId", pengajuan.id)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = pengajuanList.size

    private fun createOrFindChatRoom(context: Context) {
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
                var chatroomId = ""
                for (document in documents) {
                    if (document.data["userIdAdmin"] == adminId && document.data["userIdMitra"] == uid) {
                        isExist = true
                        chatroomId = document.id
                        break
                    }
                }
                if (isExist) {
                    val intent = Intent(context, ChatActivity::class.java).apply {
                        putExtra("chatroomId", chatroomId)
                    }
                    context.startActivity(intent)
                } else {
                    db.collection("chatRoom").add(chatRoom)
                        .addOnSuccessListener {
                            val intent = Intent(context, ChatActivity::class.java).apply {
                                putExtra("chatroomId", it.id)
                            }
                            context.startActivity(intent)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Chat room failed to create", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error creating chat room", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateStatus(
        pengajuan: Pengajuan,
        holder: PesananViewHolder,
        status: String
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pengajuan").document(pengajuan.id)
            .update("status", status)
            .addOnSuccessListener {
                holder.status.text = status
            }
            .addOnFailureListener {
                Toast.makeText(holder.itemView.context, "Error updating status", Toast.LENGTH_SHORT).show()
            }
        db.collection("pengiriman").document(pengajuan.idPengiriman.toString())
            .update("status", status)
            .addOnSuccessListener {
                holder.status.text = status
            }
            .addOnFailureListener {
                Toast.makeText(holder.itemView.context, "Error updating delivery status", Toast.LENGTH_SHORT).show()
            }
    }

    private fun buildUiKit(context: Context) {
        UiKitApi.Builder()
            .withContext(context.applicationContext)
            .withMerchantClientKey("G741518400")
            .withMerchantUrl("https://udmrputra.azurewebsites.net/serc.php/")
            .enableLog(true)
            .withColorTheme(CustomColorTheme("#FFE51255", "#B61548", "#FFE51255"))
            .build()
        uiKitCustomSetting()
    }

    private fun uiKitCustomSetting() {
        val uIKitCustomSetting = UiKitApi.getDefaultInstance().uiKitSetting
        uIKitCustomSetting.saveCardChecked = true
    }
}
