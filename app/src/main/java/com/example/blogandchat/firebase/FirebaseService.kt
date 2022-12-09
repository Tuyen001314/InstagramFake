package com.example.blogandchat.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.LightingColorFilter
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.blogandchat.R
import com.example.blogandchat.fragment.ChatFragment
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class FirebaseService : FirebaseMessagingService() {

    val CHANNEL_ID = "my_notification_chanel"
    companion object {
        var sharedPref: SharedPreferences? = null
        var token: String?
            get() {
            return sharedPref?.getString("token", "")
        }
        set(value) {
            sharedPref?.edit()?.putString("token", value)?.apply()
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        token = p0
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        val intent = Intent(this, ChatFragment::class.java)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = Random.nextInt()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotification(notificationManager)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(p0.data["title"])
            .setContentTitle(p0.data["message"])
            .setSmallIcon(R.drawable.mesenger)
            .setAutoCancel(true)
            .setContentText(pendingIntent.toString())
            .build()


        notificationManager.notify(notificationId, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(notificationManager: NotificationManager) {
        val channelName = "ChannelFirebaseChat"
        val channel = NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            description = "MY FIREBASE CHAT"
            enableLights(true)

        }
    }
}
