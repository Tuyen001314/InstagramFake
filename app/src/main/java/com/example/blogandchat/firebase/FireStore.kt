package com.example.blogandchat.firebase

import android.media.session.MediaSessionManager
import android.util.Log
import com.example.blogandchat.activity.SetUpActivity
import com.example.blogandchat.activity.SignInActivity
import com.example.blogandchat.activity.SignUpActivity
import com.example.blogandchat.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.example.blogandchat.model.User

class FireStore {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SetUpActivity, userInfo: User) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }.addOnFailureListener {
                //e -> Log.e(activity.javaClass.simpleName, )
            }
    }

//    fun signInUser(activity: SignInActivity) {
//        mFireStore.collection(Constants.USERS)
//            .document(getCurrentUserId())
//            .get()
//            .addOnSuccessListener { document ->
//                val loggedInUser = document.toObject(User::class.java)
//                if (loggedInUser != null) {
//                    activity.signInSuccess(loggedInUser)
//                }
//            }.addOnFailureListener {
//
//            }
//    }

    fun getCurrentUserId(): String {
        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""
        if (currentUser != null) {
            currentUserId = currentUser.uid
        }
        return currentUserId
    }
}