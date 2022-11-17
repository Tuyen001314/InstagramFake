package com.example.blogandchat.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Comments(
    val user: String = "",
    val comment: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null
) : CommentsId() , Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        //parcel.rea
    )
    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(user)
        writeString(comment)
        //writeString=(timestamp)
        //writeString(image)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Comments> {
        override fun createFromParcel(parcel: Parcel): Comments {
            return Comments(parcel)
        }

        override fun newArray(size: Int): Array<Comments?> {
            return arrayOfNulls(size)
        }
    }
}