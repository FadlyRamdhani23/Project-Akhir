package com.tugasakhir.udmrputra.data

import android.os.Parcel
import android.os.Parcelable

data class Barang(
    val id: String,
    val name: String,
    val catId: String,
    val jumlah: String,
    val gambar: String,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(catId)
        parcel.writeString(jumlah)
        parcel.writeString(gambar)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Barang> {
        override fun createFromParcel(parcel: Parcel): Barang {
            return Barang(parcel)
        }

        override fun newArray(size: Int): Array<Barang?> {
            return arrayOfNulls(size)
        }
    }
}