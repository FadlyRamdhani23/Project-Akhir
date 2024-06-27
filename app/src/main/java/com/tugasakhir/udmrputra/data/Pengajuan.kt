package com.tugasakhir.udmrputra.data

data class Pengajuan(
    val id : String,
    val userId: String,
    val tanggalPengajuan: String,
    val barangAjuan: String,
    val listBarang: List<String>,
    val jenisPembayaran: String,
    val statusPengajuan: String
)
