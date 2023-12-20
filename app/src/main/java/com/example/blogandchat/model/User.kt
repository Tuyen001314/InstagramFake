package com.example.blogandchat.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val image: String = "",
    val status: String = "",
    val publicKey: String? = "",
): Parcelable,Serializable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(name)
        writeString(email)
        writeString(image)
        writeString(status)
        writeString(publicKey)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}