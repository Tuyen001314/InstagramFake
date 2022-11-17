package com.example.blogandchat.model

import com.google.firebase.firestore.Exclude

open class CommentsId {
    @Exclude
    var CommentsId: String? = null
    fun <T : CommentsId?> withId(id: String): T {
        CommentsId = id
        return this as T
    }
}