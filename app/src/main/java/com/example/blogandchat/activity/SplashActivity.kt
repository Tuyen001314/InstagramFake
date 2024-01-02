package com.example.blogandchat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.content.ContextCompat
import com.example.blogandchat.R
import com.example.blogandchat.firebase.FireStore
import com.example.blogandchat.utils.AppKey

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        setContentView(R.layout.activity_splash)


        Handler().postDelayed({
            val currentUserId = FireStore().getCurrentUserId()
            if (currentUserId.isNotEmpty()) {
                FireStore().updatePublicKeyUser(AppKey.getPublicKey())
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, SignInActivity::class.java))
            }
            finish()
        }, 2000)
    }
}