package com.example.blogandchat.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogandchat.model.Message
import com.example.blogandchat.model.Post
import com.example.blogandchat.model.PostDetailModel
import com.example.blogandchat.model.User
import com.example.blogandchat.utils.AppKey
import com.example.blogandchat.utils.byteArrayToString
import com.example.blogandchat.utils.generateRandomIV
import com.example.blogandchat.utils.optimizeAndConvertImageToByteArray
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class HomeViewModel : ViewModel() {
    private val _postDetails = MutableLiveData(mutableListOf<PostDetailModel>())
    val postDetails: LiveData<MutableList<PostDetailModel>> = _postDetails
    private val _listFriend = MutableLiveData(mutableListOf<User>())
    val listFriend: LiveData<MutableList<User>> = _listFriend
    private val fireStore = FirebaseFirestore.getInstance()
    private val idUser = FirebaseAuth.getInstance().uid

    private val calendar = Calendar.getInstance()
    private val simpleDateFormat = SimpleDateFormat("hh:mm a")
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    fun transferData(list: MutableList<Post>) {
        viewModelScope.launch {
            val detailList = mutableListOf<PostDetailModel>()
            list.forEach {
                var isLiked = false
                var userName: String? = ""
                var imageUser: String? = ""
                var idUserPost: String? = ""
                var postID: String? = ""
                fireStore.collection("users").document(it.user).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            postID = task.result.id
                            userName = task.result.getString("name")
                            imageUser = task.result.getString("image")
                            idUserPost = task.result.getString("id")

                            if (idUser != null) {
                                fireStore.collection(
                                    "posts/" +
                                            postID + "/likes"
                                ).document(idUser).addSnapshotListener { snapshot, e ->
                                    if (e == null) {
                                        if (snapshot != null) {
                                            isLiked = snapshot.exists()
                                        }
                                    }
                                }
                            }
                        }
                    }.await()

                val postDetail = PostDetailModel(
                    image = it.image,
                    caption = it.caption,
                    time = it.time,
                    userName = userName.toString(),
                    imageUser = imageUser.toString(),
                    idUser = idUserPost.toString(),
                    isYour = idUserPost == idUser,
                    isLiked = isLiked

                )
                postDetail.postId = postID
                detailList.add(postDetail)
            }
            _postDetails.postValue(detailList)
        }
    }

    fun likeImage(postId: String) {
        FirebaseAuth.getInstance().uid?.let { it1 ->
            FirebaseFirestore.getInstance().collection(
                "posts/" +
                        postId + "/likes"
            ).document(it1).get().addOnCompleteListener { task ->

                if (!task.result.exists()) {
                    val likesMap: MutableMap<String, Any> = HashMap()
                    likesMap["timestamp"] = FieldValue.serverTimestamp()
                    if (idUser != null) {
                        fireStore.collection("posts/$postId/likes").document(idUser)
                            .set(likesMap)
                    }
                } else {
                    if (idUser != null) {
                        fireStore.collection("posts/$postId/likes").document(idUser).delete()
                    }
                }
            }
        }
    }

    fun getFriends() {
        viewModelScope.launch {
            val job = viewModelScope.async {
                val listFriend = mutableListOf<User>()
                val data = fireStore.collection("users/$idUser/friends").get().await()
                for (item in data) {
                    fireStore.collection("users").document(item.id).get().addOnSuccessListener {
                        val user = it.toObject(User::class.java)
                        user?.let { it1 -> listFriend.add(it1) }
                    }.await()
                }
                listFriend
            }
            _listFriend.postValue(job.await())
        }
    }

    fun sendToFriends(ids: List<String>, url: String) {
        println(ids.toString())
        CoroutineScope(IO).launch {
            val job = CoroutineScope(IO).async {
                urlToBitmap(url)
            }
            val bitmap = job.await()
            if (bitmap != null) {
                for (id in ids) {
                    val senderRoom = firebaseAuth.uid + id
                    val receiverRoom = id + firebaseAuth.uid
                    val publicKey = _listFriend.value?.find { it.id == id }?.publicKey ?: ""
                    uploadImage(bitmap, senderRoom, id, receiverRoom, publicKey)
                }
            }
        }

    }

    fun uploadImage(
        bitmap: Bitmap,
        senderRoom: String,
        mReceiverUid: String?,
        receiverRoom: String,
        publicKey: String,
    ) {
        CoroutineScope(IO).launch {
            AppKey.calculateKey(publicKey)
            val iv = generateRandomIV()
            val enterdMessage = optimizeAndConvertImageToByteArray(bitmap)
            val date = Date()
            val currentTime = simpleDateFormat.format(calendar.time)
            val message = firebaseAuth.uid?.let { it1 ->
                Message(
                    currentTime = currentTime,
                    message = AppKey.encrypt(enterdMessage ?: byteArrayOf(), iv),
                    senderId = it1,
                    timeStamp = date.time,
                    type = 1,
                    iv = iv.byteArrayToString()
                )
            }

            fireStore.collection("users/${firebaseAuth.uid}/message")
                .get()
                .addOnCompleteListener { _ ->
                    val timeRequest: MutableMap<String, Any> = HashMap()
                    timeRequest["id"] = mReceiverUid.toString()
                    timeRequest["timestamp"] = FieldValue.serverTimestamp()
                    timeRequest["timeseen"] = "0"
                    timeRequest["last_message"] = message?.toMap() ?: ""
                    fireStore
                        .collection("users/${firebaseAuth.uid}/message")
                        .document(mReceiverUid.toString())
                        .set(timeRequest)

                    fireStore
                        .collection("users/${mReceiverUid}/message")
                        .document("${firebaseAuth.uid}")
                        .set(timeRequest)
                }

//            firebaseAuth.uid?.let { it1 ->
//                FirebaseFirestore.getInstance().collection("users/${mReceiverUid}/message")
//                    .document(
//                        it1
//                    ).update(
//                        mapOf(
//                            "timeseen" to "1"
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

    suspend fun urlToBitmap(url: String): Bitmap? {
        var bitmap: Bitmap? = null
        var inputStream: InputStream? = null
        try {
            val imageUrl = URL(url)
            inputStream = withContext(IO) {
                imageUrl.openStream()
            }
            bitmap = BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return bitmap
    }
}
