package com.example.blogandchat.model

import android.os.Parcel
import android.os.Parcelable

data class UserShare(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val image: String = "",
    val status: String = "",
    var isPicked: Boolean = false
)
