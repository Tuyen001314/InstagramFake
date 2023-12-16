package com.example.blogandchat.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogandchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.HashMap

class FavoriteViewModel() : ViewModel() {
    private val fireStore = FirebaseFirestore.getInstance()
    private val id = FirebaseAuth.getInstance().uid
    private val _listFavorite = MutableLiveData<MutableList<User>>()
    val listFavorite: MutableLiveData<MutableList<User>> = _listFavorite

    private val _listSuggest = MutableLiveData<MutableList<User>>()
    val listSuggest: MutableLiveData<MutableList<User>> = _listSuggest
    lateinit var job1: Deferred<MutableList<User>>
    lateinit var job2: Deferred<MutableList<User>>

    fun initialize(){
        val id = FirebaseAuth.getInstance().uid
        viewModelScope.launch {
            fireStore.collection("users/$id/receive_request")
                .get().addOnSuccessListener { documents ->
                    val list = mutableListOf<User>()
                    job1 = this.async {
                        for (document in documents) {
                            val idUserReceive = document.id
                           this.launch{
                                fireStore.collection("users").document(idUserReceive)
                                    .get().addOnSuccessListener { document ->
                                        if (document != null) {
                                            list.add(document.toObject(User::class.java)!!)
                                        }
                                    }.await()
                            }

                        }
                        list
                    }
                }.await()



            fireStore.collection("users").whereNotEqualTo("id", id)
                .get().addOnSuccessListener { documents ->
                    job2 = this.async {
                        val listSuggestions = mutableListOf<User>()
                        for (document in documents) {
                            val idUserReceive = document.id
                           this.launch {
                                fireStore.collection("users/$id/following")
                                    .document(idUserReceive)
                                    .get().addOnSuccessListener { doc ->
                                        if (!doc.exists()) {
                                            listSuggestions.add(document.toObject(User::class.java))
                                        }
                                    }.await()
                            }
                        }
                        listSuggestions
                    }
                }.await()

            _listFavorite.postValue(job1.await())
            _listSuggest.postValue(job2.await())
        }

    }

    fun acceptFollow(user: User){
       fireStore.collection("users/$id/friends").get()
            .addOnCompleteListener { _ ->
                val timeRequest: MutableMap<String, Any> = HashMap()
                timeRequest["id"] = user.id
                timeRequest["timestamp"] = FieldValue.serverTimestamp()
                FirebaseFirestore.getInstance().collection("users/$id/friends")
                    .document(user.id)
                    .set(timeRequest)
            }

        fireStore.collection("users/$id/receive_request")
            .document(user.id).delete()
    }

    fun follow(user: User){
        fireStore.collection("users/$id/following").get()
            .addOnCompleteListener { _ ->
                val timeRequest: MutableMap<String, Any> = HashMap()
                timeRequest["id"] = user.id
                timeRequest["timestamp"] = FieldValue.serverTimestamp()
                FirebaseFirestore.getInstance().collection("users/$id/following")
                    .document(user.id)
                    .set(timeRequest)
            }

        fireStore.collection("users/${user.id}/follower").get()
            .addOnCompleteListener { _ ->
                val timeRequest: MutableMap<String, Any> = HashMap()
                timeRequest["id"] = id.toString()
                timeRequest["timestamp"] = FieldValue.serverTimestamp()
                FirebaseFirestore.getInstance().collection("users/${user.id}/follower")
                    .document(id.toString())
                    .set(timeRequest)
            }
    }
}