package com.tugasakhir.udmrputra.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Pengiriman(
    val id: Int,
    val name: String,
    val mitraLatitude: Double,
    val mitraLongitude: Double,
    val supirLatitude: Double,
    val supirLongitude: Double,
    val address: String,
    val phone: String,
    val status: Int
): Parcelable
