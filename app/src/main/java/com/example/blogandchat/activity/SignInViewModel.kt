package com.example.blogandchat.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class SignInViewModel : ViewModel() {
    val uiState = MutableLiveData<SignInUiState>()

    fun userSignIn(email: String, pass: String) {
        if (email.isNotEmpty() && pass.isNotEmpty()) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { taskId ->
                    if (taskId.isSuccessful) {
                        uiState.postValue(
                            SignInUiState(
                                email = email,
                                password = pass,
                                result = true,
                                check = true
                            )
                        )
                    } else {
                        uiState.postValue(
                            SignInUiState(
                                email = email,
                                password = pass,
                                result = false,
                                check = true
                            )
                        )
                    }
                }
        } else {
            uiState.postValue(
                SignInUiState(check = false)
            )
        }
    }

}

data class SignInUiState(
    val email: String? = null,
    val password: String? = null,
    val result: Boolean? = false,
    val check: Boolean? = false
)