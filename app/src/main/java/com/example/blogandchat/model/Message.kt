package com.example.blogandchat.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(
    var currentTime: String = "",
    var timeStamp: Long? = 0,
    var senderId: String = "",
    var message: String = "",
    var type: Int? = 0,
    var iv: String = "",
    ) : Parcelable {
    fun toMap(): Map<String, Any> {
        val messageMap = HashMap<String, Any>()
        messageMap["currentTime"] = currentTime
        messageMap["timeStamp"] = timeStamp ?: 0L
        messageMap["senderId"] = senderId
        messageMap["message"] = message
        messageMap["type"] = type ?: 0
        messageMap["iv"] = iv
        return messageMap
    }

}