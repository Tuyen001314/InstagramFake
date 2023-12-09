package com.example.blogandchat

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.blogandchat.utils.AppKey

class App : Application() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate() {
        super.onCreate()
        instance = this
        AppKey.generateKeyPair()
    }

    companion object {
        lateinit var instance: App
    }
}
