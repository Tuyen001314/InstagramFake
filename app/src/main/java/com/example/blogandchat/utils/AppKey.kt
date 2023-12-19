package com.example.blogandchat.utils

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.annotation.RequiresApi
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStore.PrivateKeyEntry
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.spec.SecretKeySpec

object AppKey {
    private const val KEY_ALIAS = "eckeypair"
    private lateinit var secretKey: SecretKeySpec
    private val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")

    @RequiresApi(Build.VERSION_CODES.S)
    fun generateKeyPair() {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return
        }
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            "AndroidKeyStore",
        )
        keyPairGenerator.initialize(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_AGREE_KEY,
            )
                .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                .build(),
        )
        keyPairGenerator.generateKeyPair()
    }

    fun getPublicKey(): String {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            if (keyStore.containsAlias(KEY_ALIAS)) {
                val privateKey = keyStore.getEntry(KEY_ALIAS, null) as PrivateKeyEntry
                return Base64.encodeToString(
                    privateKey.certificate.publicKey.encoded,
                    Base64.DEFAULT
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun calculateKey(publicKey: String) {
        val publicKeySpec = X509EncodedKeySpec(Base64.decode(publicKey, Base64.DEFAULT))
        val keyFactory = KeyFactory.getInstance("EC")
        val publicKey1 = keyFactory.generatePublic(publicKeySpec)

        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val privateKey = keyStore.getEntry(KEY_ALIAS, null) as PrivateKeyEntry

        // Create a shared secret based on our private key and the other party's public key.

        // Create a shared secret based on our private key and the other party's public key.
        val keyAgreement = KeyAgreement.getInstance("ECDH", "AndroidKeyStore")
        keyAgreement.init(privateKey.privateKey)
        keyAgreement.doPhase(publicKey1, true)
        val sharedSecret = keyAgreement.generateSecret()
        secretKey = SecretKeySpec(sharedSecret, "AES")
    }

    fun encrypt(plainText: String): String {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    fun encrypt(byteArray: ByteArray): String {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(byteArray)
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    fun decrypt(data: String?): String? {
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val encryptedBytes: ByteArray = Base64.decode(data, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun decryptByteArray(data:String):ByteArray{
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val encryptedBytes: ByteArray = Base64.decode(data, Base64.DEFAULT)
            return cipher.doFinal(encryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return byteArrayOf()
    }
}