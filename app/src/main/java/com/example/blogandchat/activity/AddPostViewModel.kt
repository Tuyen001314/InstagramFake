package com.example.blogandchat.activity

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AddPostViewModel : ViewModel() {
    val uiState = MutableLiveData<AddPostUiState>()

    fun addPost(caption: String) {
        if (uiState.value?.uri == null) {
            uiState.postValue(
                AddPostUiState(addError = false),
            )
            return
        }
        val filename = UUID.randomUUID().toString()

        val postRef = FirebaseStorage.getInstance().reference.child("post_images")
            .child(FieldValue.serverTimestamp().toString() + ".jpg")

        postRef.putFile(uiState.value!!.uri!!).addOnSuccessListener {
            postRef.downloadUrl.addOnSuccessListener {
                val id = FirebaseAuth.getInstance().uid.toString()
                val postMap = HashMap<String, Any>()
                postMap["image"] = it.toString()
                postMap["user"] = id
                postMap["caption"] = caption
                postMap["time"] = FieldValue.serverTimestamp()
                val ref = FirebaseFirestore.getInstance().collection("posts").add(postMap)
                    .addOnSuccessListener { documentReference ->
                        uiState.postValue(
                            AddPostUiState(
                                uri = null,
                                addingPost = false,
                                addPostSuccess = true,
                                addError = true,
                            ),
                        )
                    }
                    .addOnFailureListener { e ->
                        uiState.postValue(
                            AddPostUiState(
                                uri = null,
                                addingPost = false,
                                addPostSuccess = false,
                                addError = true,
                            ),
                        )
                    }
            }
        }
    }

    fun getImage(uri: Uri) {
        uiState.postValue(
            AddPostUiState(uri = uri),
        )
    }
}

data class AddPostUiState(
    val uri: Uri? = null,
    val caption: String = "",
    val addingPost: Boolean = false,
    val addPostSuccess: Boolean = false,
    val addError: Boolean = true,
)
