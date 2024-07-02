package com.tugasakhir.udmrputra.data

data class DetailPengajuan(
    val pengajuanId: String,
    val nama: String,
    val jumlah: Long?,
    val hargaPasar: Long?,
    val hargaBeli: Long?,
    val catatan: String,
    val jenisPembayaran: String,
    val tanggal: String,
    val idPengiriman: String? = null,
    val gambar: String? = null,
)
