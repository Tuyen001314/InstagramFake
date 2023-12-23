package com.example.blogandchat.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class DataMessageUserModel(
    val id: String? = "",
    val message: Message? = Message()
)
