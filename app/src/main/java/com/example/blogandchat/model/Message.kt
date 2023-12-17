package com.example.blogandchat.model

import android.os.Parcel
import android.os.Parcelable

data class Message(
    val currentTime: String = "",
    val timeStamp: Long? = 0,
    val senderId: String = "",
    val message: String = "",
    val type: Int? = 0

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(currentTime)
        if (timeStamp != null) {
            writeLong(timeStamp)
        }
        writeString(message)
        writeString(senderId)
        writeInt(type?:0)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Message> {
        override fun createFromParcel(parcel: Parcel): Message {
            return Message(parcel)
        }

        override fun newArray(size: Int): Array<Message?> {
            return arrayOfNulls(size)
        }
    }

}