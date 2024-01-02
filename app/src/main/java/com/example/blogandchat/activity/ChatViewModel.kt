package com.example.blogandchat.activity

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogandchat.App
import com.example.blogandchat.model.Message
import com.example.blogandchat.utils.AppKey
import com.example.blogandchat.utils.convertVideoToByteArray
import com.example.blogandchat.utils.optimizeAndConvertImageToByteArray
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class ChatViewModel : ViewModel() {
    private val calendar = Calendar.getInstance()
    private val simpleDateFormat = SimpleDateFormat("hh:mm a")
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    fun uploadImage(
        bitmap: Bitmap,
        senderRoom: String,
        mReceiverUid: String?,
        receiverRoom: String,
    ) {
        viewModelScope.launch {
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

//            firebaseAuth.uid?.let { it1 ->
//                FirebaseFirestore.getInstance().collection("users/${mReceiverUid}/message")
//                    .document(
//                        it1
//                    ).update(
//                        mapOf(
//                            "timeseen" to "1",
//                            "last_message" to message?.message
//                        )
//                    )
//            }

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


//            firebaseAuth.uid?.let { it1 ->
//                FirebaseFirestore.getInstance().collection("users/${mReceiverUid}/message")
//                    .document(
//                        it1
//                    ).update(
//                        mapOf(
//                            "timeseen" to "1",
//                            "last_message" to message?.message
//                        )
//                    )
//            }

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

    fun updateLastMessage(mReceiverUid: String?, lastMessage: Message) {
        mReceiverUid?.let {
            FirebaseFirestore.getInstance().collection("users/${firebaseAuth.uid}/message")
                .get()
                .addOnCompleteListener { _ ->
                    val timeRequest: MutableMap<String, Any> = HashMap()
                    timeRequest["id"] = mReceiverUid.toString()
                    timeRequest["timestamp"] = FieldValue.serverTimestamp()
                    timeRequest["timeseen"] = "0"
                    timeRequest["last_message"] =
                        if (lastMessage.type == 0) lastMessage?.toMap() else Message(
                            message = "",
                            type = lastMessage.type,
                            timeStamp = lastMessage.timeStamp,
                            currentTime = lastMessage.currentTime,
                            senderId = lastMessage.senderId
                        )
                    FirebaseFirestore.getInstance()
                        .collection("users/${firebaseAuth.uid}/message")
                        .document(mReceiverUid.toString())
                        .set(timeRequest)
                }
        }
    }

    fun uploadVideo(
        uri: Uri,
        senderRoom: String,
        receiverRoom: String,
    ) {
        CoroutineScope(IO).launch {
            val enterdMessage = convertVideoToByteArray(App.instance.applicationContext, uri)
            val date = Date()
            val currentTime = simpleDateFormat.format(calendar.time)
            val message = firebaseAuth.uid?.let { it1 ->
                Message(
                    currentTime = currentTime,
                    message = AppKey.encrypt(enterdMessage ?: byteArrayOf()),
                    senderId = it1,
                    timeStamp = date.time,
                    type = 2
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
}