package com.example.blogandchat.Preferences

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.blogandchat.App

object Preference {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val preferences =
        EncryptedSharedPreferences.create(
            "app_preference",
            masterKeyAlias,
            App.instance,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )

    fun getInstance(): SharedPreferences {
        return preferences
    }
}
