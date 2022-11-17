package com.example.blogandchat.activity

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.blogandchat.model.User
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class SetUpViewModel : ViewModel() {
    val uiState = MutableLiveData<SetUpUiState>()

    fun addUser(id: String, email: String, name: String) {
        if (uiState.value?.uri == null) {
            uiState.postValue(
                SetUpUiState(addError = true)
            )
            return
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        if (name.isNotEmpty()) {
            ref.putFile(uiState.value?.uri!!).addOnSuccessListener {

                ref.downloadUrl.addOnSuccessListener {
                    //progressBar.visibility = View.VISIBLE
                    val user = User(
                        id = id,
                        email = email,
                        name = name,
                        image = it.toString(),
                        status = "online"
                    )

                    val myRef = FirebaseDatabase.getInstance().getReference("/users/$id")
                    myRef.setValue(user).addOnSuccessListener {
                        uiState.postValue(
                            SetUpUiState(
                                uri = uiState.value!!.uri!!,
                                name = name,
                                addingUser = true,
                                addUserSuccess = true,
                                addError = false,
                                user = user
                            )
                        )
                    }
                }
            }
        } else {
            uiState.postValue(
                SetUpUiState(
                    addError = true,
                    //addUserSuccess = true,
                )
            )
        }
    }

    fun getImage(uri: Uri) {
        uiState.postValue(
            SetUpUiState(uri = uri)
        )
    }
}

data class SetUpUiState(
    val uri: Uri? = null,
    val name: String = "",
    val addingUser: Boolean = false,
    val addUserSuccess: Boolean = false,
    val addError: Boolean = false,
    val user: User? = null
)