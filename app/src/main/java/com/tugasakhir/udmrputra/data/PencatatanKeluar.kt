package com.tugasakhir.udmrputra.data

data class PencatatanKeluar(
    val id: Int, // Update with the appropriate id logic if needed
    val catId: String,
    val barangId: String,
    val namaPetani: String,
    val jumlah: String, // or use Int if it should be an Integer
    val catatan: String,
    val tanggal: String,
    val hargaJual: String
)
