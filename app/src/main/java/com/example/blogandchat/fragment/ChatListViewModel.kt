package com.example.blogandchat.fragment

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogandchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatListViewModel() : ViewModel() {
    private val _listUser = MutableLiveData<MutableList<User>>()
    val listUser: LiveData<MutableList<User>> = _listUser
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var job:Deferred<MutableList<User>>

    fun fetchListUser() {
        val id = firebaseAuth.uid
        viewModelScope.launch {
            firebaseFirestore.collection("users/$id/message")
                .get().addOnSuccessListener { documents ->
                 job = this.async {
                      val list = mutableListOf<User>()
                      for (document in documents) {
                          val idUserReceive = document.id
                          this.launch {
                              FirebaseFirestore.getInstance().collection("users")
                                  .document(idUserReceive)
                                  .get().addOnSuccessListener { document ->
                                      if (document != null) {
                                          document.toObject(User::class.java)?.let { list.add(it) }
                                      }
                                  }.await()
                          }
                      }
                      list
                  }
                }.await()

            _listUser.postValue(job.await())
        }

    }
}