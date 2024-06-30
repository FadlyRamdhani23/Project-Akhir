package com.tugasakhir.udmrputra.ui.pengajuan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tugasakhir.udmrputra.data.Barang
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



class PengajuanViewModel : ViewModel() {

    private val _barangList = MutableLiveData<List<Barang>>()
    val barangList: LiveData<List<Barang>> = _barangList

    private val _isBarangDataLoaded = MutableLiveData<Boolean>()
    val isBarangDataLoaded: LiveData<Boolean> = _isBarangDataLoaded

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val categoryMap = mutableMapOf<String, String>()

    private val _isSubmitSuccessful = MutableLiveData<Boolean>()
    val isSubmitSuccessful: LiveData<Boolean> = _isSubmitSuccessful

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        _isBarangDataLoaded.value = false
        loadBarangData()
    }

    fun loadBarangData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("barang")
            .get()
            .addOnSuccessListener { result ->
                val barangList = mutableListOf<Barang>()
                for (document in result) {
                    val barangId = document.id
                    val catId = document.data["catId"].toString()
                    val namaBarang = document.data["nama"].toString()
                    val jumlahBarang = document.data["jumlah"].toString()

                    val imageUrls = if (document.get("gambar") is List<*>) {
                        (document.get("gambar") as? List<String>) ?: emptyList()
                    } else {
                        emptyList()
                    }

                    db.collection("kategori").document(catId)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            val catName = documentSnapshot.getString("nama").toString()

                            val data = Barang(
                                barangId,
                                namaBarang,
                                catName,
                                jumlahBarang,
                                imageUrls.getOrNull(0) ?: ""
                            )
                            barangList.add(data)
                            _barangList.value = barangList
                            _isBarangDataLoaded.value = true
                        }
                        .addOnFailureListener {
                            _isBarangDataLoaded.value = true
                        }
                }
            }
            .addOnFailureListener {
                _isBarangDataLoaded.value = true
            }
    }

    fun filterBarangList(query: String): List<Barang> {
        return _barangList.value?.filter { it.name.contains(query, ignoreCase = true) } ?: emptyList()
    }

    fun submitForm(
        mainNamaPetani: String,
        mainNamaBarang: String,
        mainJumlahBarang: String,
        mainHargaPasar: String,
        mainHargaBeli: String,
        mainCatatan: String,
        mainJenisPembayaran: String,
        additionalPengajuanDataList: List<Map<String, Any>>
    ) {
        _isLoading.value = true

        val mainBarangId = categoryMap.entries.firstOrNull { it.value == mainNamaBarang }?.key
        val mainBarangImageUrl = _barangList.value?.firstOrNull { it.id == mainBarangId }?.gambar

        if (mainBarangId == null) {
            _toastMessage.value = "Invalid item selected"
            _isLoading.value = false
            return
        }

        val userId = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).get()
            .addOnSuccessListener { userDocument ->
                val mainAlamat = userDocument.getString("address") ?: ""
                val mainLatitude = userDocument.getDouble("latitude") ?: 0.0
                val mainLongitude = userDocument.getDouble("longitude") ?: 0.0
                val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

                val mainPengajuanData = hashMapOf(
                    "namaPetani" to mainNamaPetani,
                    "userId" to userId,
                    "status" to "pending",
                    "jenisPembayaran" to mainJenisPembayaran,
                    "tanggalPengajuan" to currentDate,
                    "address" to mainAlamat,
                    "latitude" to mainLatitude,
                    "longitude" to mainLongitude
                )

                db.collection("pengajuan")
                    .add(mainPengajuanData)
                    .addOnSuccessListener { documentReference ->
                        val pengajuanId = documentReference.id

                        val mainBarangData = hashMapOf(
                            "namaBarang" to mainNamaBarang,
                            "jumlahBarang" to mainJumlahBarang,
                            "hargaPasar" to mainHargaPasar,
                            "hargaBeli" to mainHargaBeli,
                            "catatan" to mainCatatan,
                            "barangId" to mainBarangId,
                            "pengajuanId" to pengajuanId,
                            "imageUrl" to mainBarangImageUrl
                        )
                        db.collection("pengajuan").document(pengajuanId).collection("barang")
                            .add(mainBarangData)

                        for (additionalPengajuanData in additionalPengajuanDataList) {
                            val additionalData = additionalPengajuanData.toMutableMap()
                            additionalData["pengajuanId"] = pengajuanId
                            db.collection("pengajuan").document(pengajuanId).collection("barang")
                                .add(additionalData)
                        }

                        _toastMessage.value = "Pengajuan berhasil disimpan"
                        _isSubmitSuccessful.value = true
                        _isLoading.value = false
                    }
                    .addOnFailureListener {
                        _toastMessage.value = "Pengajuan gagal disimpan"
                        _isSubmitSuccessful.value = false
                        _isLoading.value = false
                    }
            }
            .addOnFailureListener {
                _toastMessage.value = "Gagal mengambil data pengguna"
                _isLoading.value = false
            }
    }

    fun updateCategoryMap(key: String, value: String) {
        categoryMap[key] = value
    }
}
