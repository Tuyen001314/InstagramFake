package com.example.blogandchat.model

import com.google.firebase.firestore.Exclude

open class PostId {
    @Exclude
    var postId: String? = null
    fun <T : PostId?> withId(id: String): T {
        postId = id
        return this as T
    }
}