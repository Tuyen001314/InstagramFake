package com.example.blogandchat.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogandchat.model.Message
import com.example.blogandchat.utils.AppKey
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class ChatViewModel : ViewModel() {
    val calendar = Calendar.getInstance()
    val simpleDateFormat = SimpleDateFormat("hh:mm a")
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()


    fun uploadImage(
        bitmap: Bitmap,
        senderRoom: String,
        mReceiverUid: String?,
        receiverRoom: String,
    ) {
        CoroutineScope(IO).launch {
//            val stream = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val enterdMessage = optimizeAndConvertImageToByteArray(bitmap)
            val date = Date()
            val currentTime = simpleDateFormat.format(calendar.time)
            val message = firebaseAuth.uid?.let { it1 ->
                Message(
                    currentTime = currentTime,
                    message = AppKey.encrypt(enterdMessage ?: byteArrayOf()),
                    senderId = it1,
                    timeStamp = date.time,
                    type = 1
                )
            }

            firebaseAuth.uid?.let { it1 ->
                FirebaseFirestore.getInstance().collection("users/${mReceiverUid}/message")
                    .document(
                        it1
                    ).update(
                        mapOf(
                            "timeseen" to "1"
                        )
                    )
            }

            firebaseDatabase.reference.child("chats")
                .child(senderRoom)
                .child("messages")
                .push().setValue(message).addOnCompleteListener(OnCompleteListener<Void?> {
                    firebaseDatabase.reference
                        .child("chats")
                        .child(receiverRoom)
                        .child("messages")
                        .push()
                        .setValue(message).addOnCompleteListener(OnCompleteListener<Void?> { })
                })

        }

    }

    fun sendMessageNormal(
        senderRoom: String,
        enterdMessage: String,
        mReceiverUid: String?,
        receiverRoom: String,
    ) {
        viewModelScope.launch {

            val date = Date()
            val currentTime = simpleDateFormat.format(calendar.time)
            val message = firebaseAuth.uid?.let { it1 ->
                Message(
                    currentTime = currentTime, message = AppKey.encrypt(enterdMessage),
                    senderId = it1, timeStamp = date.time, type = 0
                )
            }

            firebaseAuth.uid?.let { it1 ->
                FirebaseFirestore.getInstance().collection("users/${mReceiverUid}/message")
                    .document(
                        it1
                    ).update(
                        mapOf(
                            "timeseen" to "1"
                        )
                    )
            }

            firebaseDatabase.reference.child("chats")
                .child(senderRoom)
                .child("messages")
                .push().setValue(message).addOnCompleteListener(OnCompleteListener<Void?> {
                    firebaseDatabase.reference
                        .child("chats")
                        .child(receiverRoom)
                        .child("messages")
                        .push()
                        .setValue(message).addOnCompleteListener(OnCompleteListener<Void?> { })
                })
        }

    }

    fun optimizeAndConvertImageToByteArray(bitmap: Bitmap): ByteArray? {
        // Kích thước tối đa mong muốn của ảnh
        val maxWidth = 800
        val maxHeight = 800

        // Tính toán kích thước mới dựa trên tỉ lệ khung hình
        var width = bitmap.width
        var height = bitmap.height
        val ratio = width.toFloat() / height
        if (width > maxWidth || height > maxHeight) {
            if (ratio > 1) {
                width = maxWidth
                height = (width / ratio).toInt()
            } else {
                height = maxHeight
                width = (height * ratio).toInt()
            }
        }

        // Thay đổi kích thước ảnh
        val newBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

        // Chuyển đổi ảnh thành byte array
        val baos = ByteArrayOutputStream()
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val byteArray = baos.toByteArray()

        // Giải phóng bộ nhớ của bitmap
        newBitmap.recycle()
        return byteArray
    }
}