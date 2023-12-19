package com.example.blogandchat.model

import android.os.Parcel
import android.os.Parcelable

data class UserShare(
    var isPicked : Boolean = false
) : User()
