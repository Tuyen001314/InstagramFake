package com.example.blogandchat.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Post(
    val user: String = "",
    val caption: String = "",
    val image: String = "",
    val time: Timestamp? = null

): PostId(), Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,

    )

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(user)
        writeString(caption)
        writeString(image)

    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Post> {
        override fun createFromParcel(parcel: Parcel): Post {
            return Post(parcel)
        }

        override fun newArray(size: Int): Array<Post?> {
            return arrayOfNulls(size)
        }
    }
}