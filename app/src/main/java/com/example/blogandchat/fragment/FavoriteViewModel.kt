package com.example.blogandchat.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogandchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FavoriteViewModel() : ViewModel() {
    private val fireStore = FirebaseFirestore.getInstance()
    private val _listFavorite = MutableLiveData<MutableList<User>>()
    val listFavorite: MutableLiveData<MutableList<User>> = _listFavorite

    private val _listSuggest = MutableLiveData<MutableList<User>>()
    val listSuggest: MutableLiveData<MutableList<User>> = _listSuggest

    init {
        val id = FirebaseAuth.getInstance().uid
        viewModelScope.launch {
            val list = mutableListOf<User>()
            fireStore.collection("users/$id/receive_request")
                .get().addOnSuccessListener { documents ->
                    for (document in documents) {
                        val idUserReceive = document.id
                        var user = User()
                        this.launch {
                            fireStore.collection("users").document(idUserReceive)
                                .get().addOnSuccessListener { document ->
                                    if (document != null) {
                                        user = document.toObject(User::class.java)!!
                                        list.add(user)
                                    }
                                }.await()
                        }

                    }
                    _listFavorite.postValue(list)
                }



            val listSuggestions = mutableListOf<User>()
            fireStore.collection("users").whereNotEqualTo("id", id)
                .get().addOnSuccessListener { documents ->
                    for (document in documents) {
                        val idUserReceive = document.id
                        var user = User()
                        this.launch {
                            fireStore.collection("users/$id/following")
                                .document(idUserReceive)
                                .get().addOnSuccessListener { doc ->
                                    if (!doc.exists()) {
                                        user = document.toObject(User::class.java)
                                        listSuggestions.add(user)
                                    }
                                }.await()
                        }
                    }
                    _listSuggest.postValue(listSuggestions)
                }


        }

    }
}