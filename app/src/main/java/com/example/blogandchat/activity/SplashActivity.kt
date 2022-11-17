package com.example.blogandchat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.blogandchat.R
import com.example.blogandchat.firebase.FireStore

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        Handler().postDelayed({
            var currentUserId = FireStore().getCurrentUserId()
            if (currentUserId.isNotEmpty()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, SignInActivity::class.java))
            }
            finish()
        }, 2000)
    }
}