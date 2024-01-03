package com.example.blogandchat.fragment

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogandchat.model.DataMessageUserModel
import com.example.blogandchat.model.Message
import com.example.blogandchat.model.User
import com.example.blogandchat.model.UserMessageModel
import com.example.blogandchat.utils.AppKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.system.measureTimeMillis

class ChatListViewModel() : ViewModel() {
    private val _listUser = MutableLiveData<MutableList<User>>()
    val listUser: LiveData<MutableList<User>> = _listUser
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseFirestore = FirebaseFirestore.getInstance()
    private val idUser = FirebaseAuth.getInstance().uid
    private val _pairLiveData = MutableLiveData<MutableList<UserMessageModel>>()
    val pairLiveData: LiveData<MutableList<UserMessageModel>> = _pairLiveData
    private val _listFriend = MutableLiveData<MutableList<User>>()
    val listFriend: LiveData<MutableList<User>> = _listFriend

    init {
        getFriends()
    }

    fun fetchListUser() {
        val id = firebaseAuth.uid
        viewModelScope.launch {
            try {
                val data = firebaseFirestore.collection("users/$id/message").get().await()

                val list = CoroutineScope(IO).async {
                    val deferredList = data.documents.map { document ->
                        val idUserReceive = document.id
                        async {
                            firebaseFirestore.collection("users")
                                .document(idUserReceive)
                                .get().await().toObject(User::class.java)
                        }
                    }

                    deferredList.awaitAll().filterNotNull()
                }

                val listMes = CoroutineScope(IO).async {
                    val deferredList = data.documents.map { documentSnapshot ->
                        val idUserReceive = documentSnapshot.id
                        async {
                            var lastMessage: DataMessageUserModel? = DataMessageUserModel()
                            val userMessageDocument =
                                firebaseFirestore.collection("users/$id/message")
                                    .document(idUserReceive)
                                    .get().await()

                            if (userMessageDocument.exists()) {
                                // Lấy giá trị của trường "last_message"
                                val idMessage = userMessageDocument["id"].toString()
                                val lastMessageMap =
                                    userMessageDocument["last_message"] as? Map<String, Any>

                                // Kiểm tra và xử lý giá trị "last_message"
                                if (lastMessageMap != null) {
                                    // Đây là giá trị của trường "last_message"
                                    val message = Message(
                                        currentTime = lastMessageMap["currentTime"].toString(),
                                        timeStamp = (lastMessageMap["timeStamp"] as? Number)?.toLong()
                                            ?: 0,
                                        senderId = lastMessageMap["senderId"].toString(),
                                        message = lastMessageMap["message"].toString(),
                                        type = (lastMessageMap["type"] as? Number)?.toInt() ?: 0,
                                        iv = lastMessageMap["iv"].toString()
                                    )
                                    lastMessage = DataMessageUserModel(idMessage, message)
                                }
                            }
                            lastMessage
                        }
                    }
                    deferredList.awaitAll().filterNotNull()
                }

                val users = list.await()
                val messages = listMes.await()
//                val pairs = mutableListOf<UserMessageModel>()
//
//                users.forEach { user ->
//                    val messageMatching =
//                        messages.find { it.id == user.id || it.message?.senderId == user.id }
//                    messageMatching?.let {
//                        AppKey.calculateKey(user.publicKey.toString())
//
//                        val messageDecrypt = if (it.message?.type == 0) {
//                            AppKey.decrypt(
//                                messageMatching.message?.message,
//                                messageMatching.message?.iv.toString()
//                            )
//                                ?.let {
//                                    Message(
//                                        message = it,
//                                        senderId = messageMatching.message?.senderId.toString(),
//                                        currentTime = messageMatching.message?.currentTime.toString(),
//                                        timeStamp = messageMatching.message?.timeStamp,
//                                        type = messageMatching.message?.type,
//                                    )
//                                }
//                        } else {
//                            Message(
//                                message = "",
//                                senderId = messageMatching.message?.senderId.toString(),
//                                currentTime = messageMatching.message?.currentTime.toString(),
//                                timeStamp = messageMatching.message?.timeStamp,
//                                type = messageMatching.message?.type,
//                            )
//
//                        }
//                        pairs.add(UserMessageModel(user, messageDecrypt ?: Message()))
//                    }
//                }
                flow { emit(mappingMessengerUser(users, messages)) }
                    .onStart {  }
                    .onCompletion {  }
                    .collect{
                        _pairLiveData.postValue(it)

                    }

            } catch (e: Exception) {
                // Xử lý các ngoại lệ ở đây
                e.printStackTrace()
            }
        }


    }

    fun getFriends() {
        viewModelScope.launch {
            val job = viewModelScope.async {
                val listFriend = mutableListOf<User>()
                val data = firebaseFirestore.collection("users/$idUser/friends").get().await()
                for (item in data) {
                    firebaseFirestore.collection("users").document(item.id).get()
                        .addOnSuccessListener {
                            val user = it.toObject(User::class.java)
                            user?.let { it1 -> listFriend.add(it1) }
                        }.await()
                }
                listFriend
            }
            _listFriend.postValue(job.await())
        }
    }

    suspend fun mappingMessengerUser(
        users: List<User>,
        messages: List<DataMessageUserModel>,
    ): MutableList<UserMessageModel> {
        val pairs = mutableListOf<UserMessageModel>()

        users.forEach { user ->
            val messageMatching =
                messages.find { it.id == user.id || it.message?.senderId == user.id }
            messageMatching?.let {
                AppKey.calculateKey(user.publicKey.toString())

                val messageDecrypt = if (it.message?.type == 0) {
                    AppKey.decrypt(
                        messageMatching.message?.message,
                        messageMatching.message?.iv.toString()
                    )
                        ?.let {
                            Message(
                                message = it,
                                senderId = messageMatching.message?.senderId.toString(),
                                currentTime = messageMatching.message?.currentTime.toString(),
                                timeStamp = messageMatching.message?.timeStamp,
                                type = messageMatching.message?.type,
                            )
                        }
                } else {
                    Message(
                        message = "",
                        senderId = messageMatching.message?.senderId.toString(),
                        currentTime = messageMatching.message?.currentTime.toString(),
                        timeStamp = messageMatching.message?.timeStamp,
                        type = messageMatching.message?.type,
                    )

                }
                pairs.add(UserMessageModel(user, messageDecrypt ?: Message()))
            }
        }
        return pairs
    }
}
