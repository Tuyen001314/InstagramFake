package com.example.blogandchat.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class SignUpViewModel : ViewModel() {
    val uiState = MutableLiveData<SignUpUiState>()

    fun userSignUp(email: String, pass: String) {
        if (email.length >= 10 && pass.length >= 6) {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { taskId ->
                    if (taskId.isSuccessful) {
                        uiState.postValue(
                            SignUpUiState(
                                email = email,
                                password = pass,
                                result = true,
                                check = true,
                                id = taskId.result!!.user!!.uid,
                            ),
                        )
                    } else {
                        uiState.postValue(
                            SignUpUiState(
                                result = false,
                            ),
                        )
                    }
                }
        } else {
            uiState.postValue(
                SignUpUiState(check = false),
            )
        }
    }
}

data class SignUpUiState(
    val email: String? = null,
    val password: String? = null,
    val result: Boolean? = false,
    val check: Boolean? = false,
    val id: String? = null,
)
