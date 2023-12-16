package com.example.blogandchat

import com.example.blogandchat.model.User

interface FavoriteCallback {
    fun accept(user: User)
    fun delete(user: User)
}