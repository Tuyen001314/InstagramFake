package com.example.blogandchat

interface PostDetailListener {
    fun click(id: String)
    fun like(id:String)
    fun comment(postId: String, id: String)
}