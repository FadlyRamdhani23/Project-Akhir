package com.tugasakhir.udmrputra.data

import android.os.Parcel
import android.os.Parcelable

data class Pencatatan(
    val id: String? = "",
    val catId: String? = "",
    val barangId: String? = "",
    val namaPetani: String? = "",
    val jumlah: String? = "",
    val gambar: String? = "",
    val catatan: String? = "",
    val tanggal: String? = "",
    val hargaBeli: String? = ""
) : Parcelable {
    constructor() : this("", "", "", "", "", "", "", "", "")

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(catId)
        parcel.writeString(barangId)
        parcel.writeString(namaPetani)
        parcel.writeString(jumlah)
        parcel.writeString(gambar)
        parcel.writeString(catatan)
        parcel.writeString(tanggal)
        parcel.writeString(hargaBeli)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Pencatatan> {
        override fun createFromParcel(parcel: Parcel): Pencatatan {
            return Pencatatan(parcel)
        }

        override fun newArray(size: Int): Array<Pencatatan?> {
            return arrayOfNulls(size)
        }
    }
}
