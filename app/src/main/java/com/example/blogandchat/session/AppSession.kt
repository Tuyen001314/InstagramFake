package com.example.blogandchat.session

import com.example.blogandchat.model.User

class AppSession {

    var userProfile = User()

    companion object {
        private var sInstance: AppSession? = null

        val instance: AppSession
            get() {
                if (sInstance == null) {
                    sInstance = AppSession()
                }
                return sInstance as AppSession
            }
    }
}