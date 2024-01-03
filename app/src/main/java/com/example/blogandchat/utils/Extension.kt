package com.example.blogandchat.utils

import android.util.Base64

fun ByteArray.byteArrayToString(): String{
    return Base64.encodeToString(this,Base64.DEFAULT)
}