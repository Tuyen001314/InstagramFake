package com.example.blogandchat.activity

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class UpdateViewModel : ViewModel() {
    val uiState = MutableLiveData<UpdateUiState>()

    fun changeProfile(id: String, name: String) {

        val postRef = FirebaseStorage.getInstance().reference.child("images")
            .child(FieldValue.serverTimestamp().toString() + ".jpg")

        postRef.putFile(uiState.value!!.uri!!).addOnSuccessListener {
            postRef.downloadUrl.addOnSuccessListener {
                //progressBar.visibility = View.VISIBLE
                if (name.isNotEmpty()) {
                    FirebaseFirestore.getInstance().collection("users").document(id).update(
                        mapOf(
                            "image" to it.toString(),
                            "name" to name,
                            "status" to "online"
                        )
                    ).addOnSuccessListener {
                        uiState.postValue(
                            UpdateUiState(
                                uri = uiState.value!!.uri!!,
                                name = name,
                                addingUser = true,
                                addUserSuccess = true
                            )
                        )
                    }
                } else {
                    FirebaseFirestore.getInstance().collection("users").document(id).update(
                        mapOf(
                            "image" to it.toString(),
                            "status" to "online"
                        )
                    ).addOnSuccessListener {
                        uiState.postValue(
                            UpdateUiState(
                                uri = uiState.value!!.uri!!,
                                addingUser = true,
                                addUserSuccess = true
                            )
                        )
                    }
                }
            }
        }
    }

    fun getImage(uri: Uri) {
        uiState.postValue(
            UpdateUiState(uri = uri)
        )
    }

    fun changeName(id: String, name: String, image: String) {
        FirebaseFirestore.getInstance().collection("users").document(id).update(
            mapOf(
                "name" to name
            )
        ).addOnSuccessListener {
            uiState.postValue(
                UpdateUiState(
                    name = name,
                    uri = image.toUri(),
                    addingUser = true,
                    addUserSuccess = true
                )
            )
        }
    }
}

data class UpdateUiState(
    val uri: Uri? = null,
    val name: String = "",
    val addingUser: Boolean = false,
    val addUserSuccess: Boolean = false
)