package com.example.blogandchat.home

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogandchat.R
import com.example.blogandchat.model.Post
import com.example.blogandchat.model.PostDetailModel
import com.example.blogandchat.model.PostId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.HashMap

class HomeViewModel : ViewModel() {
    private val _postDetails = MutableLiveData(mutableListOf<PostDetailModel>())
    val postDetails: LiveData<MutableList<PostDetailModel>> = _postDetails
    private val fireStore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val idUser = FirebaseAuth.getInstance().uid

    fun transferData(list: MutableList<Post>) {
        viewModelScope.launch {
            val detailList = mutableListOf<PostDetailModel>()
            list.forEach {
                var isLiked = false
                var userName: String? = ""
                var imageUser: String? = ""
                var idUserPost: String? = ""
                fireStore.collection("users").document(it.user).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            userName = task.result.getString("name")
                            imageUser = task.result.getString("image")
                            idUserPost = task.result.getString("id")
                        }
                    }.await()

                if (idUser != null) {
                    fireStore.collection(
                        "posts/" +
                                it.postId + "/likes"
                    ).document(idUser).addSnapshotListener { snapshot, e ->
                        if (e == null) {
                            if (snapshot != null) {
                                isLiked = snapshot.exists()
                            }
                        }
                    }
                }
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
}
