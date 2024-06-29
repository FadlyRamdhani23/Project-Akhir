package com.tugasakhir.udmrputra.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Pengiriman(
    val id: String,
    val latitudeTujuan: Double,
    val LongitudeTujuan: Double,
    val supirLatitude: Double,
    val supirLongitude: Double,
    val supir : String,
    val supirId : String,
    val address: String,
    val status: String,
): Parcelable
