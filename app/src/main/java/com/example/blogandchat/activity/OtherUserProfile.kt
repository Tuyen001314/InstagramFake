package com.example.blogandchat.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.blogandchat.OnClickImage
import com.example.blogandchat.R
import com.example.blogandchat.adapter.ProfileAdapter
import com.example.blogandchat.fragment.ImageFragment
import com.example.blogandchat.model.Post
import com.example.blogandchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import kotlinx.android.synthetic.main.activity_other_user_profile.*
import kotlinx.android.synthetic.main.activity_setting.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class OtherUserProfile : AppCompatActivity() {

    private var listPostUser: MutableList<Post> = ArrayList();
    private lateinit var adapter: ProfileAdapter
    private lateinit var listenerRegistration: ListenerRegistration
    private var check: Boolean = false
    private var checkRequest: Boolean = false
    @SuppressLint("ResourceAsColor", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_user_profile)


        val id = intent.getStringExtra("id")

        FirebaseFirestore.getInstance().collection("users/$id/following")
            .get().addOnSuccessListener { documents ->
                val tmp = documents.size()
                followingCount.text = "$tmp"
            }

        FirebaseFirestore.getInstance().collection("users/$id/follower")
            .get().addOnSuccessListener { documents ->
                val tmp = documents.size()
                followerCount.text = "$tmp"
            }


        FirebaseFirestore.getInstance().collection("users").whereEqualTo("id", id).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // val firebaseModel =
                    val user = (document.toObject(User::class.java))
                    Glide.with(this).load(user.image).into(circleImageViewOther)
                    nameUserOther.text = user.name
                    break
                }
            }

        adapter = ProfileAdapter(this, listPostUser, object : OnClickImage {
            override fun click(id: String) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.other_activity, ImageFragment(id), null)
                    .commit()
            }

        })

        val query: Query = FirebaseFirestore.getInstance().collection("posts")
            .orderBy("time", Query.Direction.DESCENDING)
        listenerRegistration = query.addSnapshotListener(
            EventListener<QuerySnapshot?> { value, _ ->
                for (doc in value!!.documentChanges) {
                    if (doc.type == DocumentChange.Type.ADDED) {
                        val postId = doc.document.id
                        val post: Post = doc.document.toObject(Post::class.java).withId(postId)
                        if (post.user == id) {
                            listPostUser.add(post)
                        }
                        adapter.notifyDataSetChanged()
                    } else {
                        adapter.notifyDataSetChanged()
                    }
                }
                postCount.text = "${listPostUser.size}"
                listenerRegistration.remove()
            })


        recyclerViewOtherProfile.setHasFixedSize(true)
        recyclerViewOtherProfile.layoutManager = GridLayoutManager(this, 3);
        recyclerViewOtherProfile.adapter = adapter


        val idCurrent = FirebaseAuth.getInstance().uid

        if (id != null) {
            FirebaseFirestore.getInstance().collection("users/$idCurrent/following").document(id)
                .addSnapshotListener { snapshot, e ->
                    if (e == null) {
                        if (snapshot != null) {
                            check = snapshot.exists()
                            if (check) {
                                buttonFollow.setTextColor(Color.BLACK)
                                buttonFollow.setBackgroundColor(Color.WHITE)
                                buttonFollow.text = "Followed"
                            } else {
                                buttonFollow.setTextColor(Color.WHITE)
                                buttonFollow.setBackgroundColor(androidx.cardview.R.color.cardview_dark_background)
                                buttonFollow.text = "Follow"
                            }
                        }
                    }
                }
        }

        buttonFollow.setOnClickListener {
            if (!check) {
                FirebaseFirestore.getInstance().collection("users/$idCurrent/following").get()
                    .addOnCompleteListener { _ ->
                        val followerTime: MutableMap<String, Any> = HashMap()
                        followerTime["id"] = id.toString()
                        followerTime["timestamp"] = FieldValue.serverTimestamp()
                        if (id != null) {
                            FirebaseFirestore.getInstance().collection("users/$idCurrent/following")
                                .document(id)
                                .set(followerTime)
                        }
                    }
                //check = true

                FirebaseFirestore.getInstance().collection("users/$id/follower").get()
                    .addOnCompleteListener { _ ->
                        val followerTime: MutableMap<String, Any> = HashMap()
                        followerTime["id"] = idCurrent.toString()
                        followerTime["timestamp"] = FieldValue.serverTimestamp()
                        if (idCurrent != null) {
                            FirebaseFirestore.getInstance().collection("users/$id/follower")
                                .document(idCurrent)
                                .set(followerTime)
                        }
                    }
            } else {
                if (id != null) {
                    FirebaseFirestore.getInstance().collection("users/$idCurrent/following")
                        .document(id).delete()
                }
                if (idCurrent != null) {
                    FirebaseFirestore.getInstance().collection("users/$id/follower")
                        .document(idCurrent).delete()
                }
                //check = false
            }
        }


        if (id != null) {
            FirebaseFirestore.getInstance().collection("users/$idCurrent/send_request").document(id)
                .addSnapshotListener { snapshot, e ->
                    if (e == null) {
                        if (snapshot != null) {
                            checkRequest = snapshot.exists()
                            if (!checkRequest) {
                                addFriend.setImageResource(R.drawable.ic_baseline_person_add_alt_1_24)
                                //cardViewAddFriend.setBackgroundColor(Color.WHITE)
                            } else {
                                addFriend.setImageResource(R.drawable.ic_baseline_person_add_disabled_24)
                                //cardViewAddFriend.setBackgroundColor(Color.BLACK)
                            }
                        }
                    }
                }
        }

        addFriend.setOnClickListener {

            if(!checkRequest) {
                FirebaseFirestore.getInstance().collection("users/$idCurrent/send_request").get()
                    .addOnCompleteListener { _ ->
                        val timeRequest: MutableMap<String, Any> = HashMap()
                        timeRequest["id"] = id.toString()
                        timeRequest["timestamp"] = FieldValue.serverTimestamp()
                        if (id != null) {
                            FirebaseFirestore.getInstance()
                                .collection("users/$idCurrent/send_request")
                                .document(id)
                                .set(timeRequest)
                        }
                    }

                FirebaseFirestore.getInstance().collection("users/$id/receive_request").get()
                    .addOnCompleteListener { _ ->
                        val timeRequest: MutableMap<String, Any> = HashMap()
                        timeRequest["id"] = idCurrent.toString()
                        timeRequest["timestamp"] = FieldValue.serverTimestamp()
                        if (idCurrent != null) {
                            FirebaseFirestore.getInstance().collection("users/$id/receive_request")
                                .document(idCurrent)
                                .set(timeRequest)
                        }
                    }
            }
            else {
                if (id != null) {
                    FirebaseFirestore.getInstance().collection("users/$idCurrent/send_request")
                        .document(id).delete()
                }
                if (idCurrent != null) {
                    FirebaseFirestore.getInstance().collection("users/$id/receive_request")
                        .document(idCurrent).delete()
                }
            }
        }

        buttonMessage.setOnClickListener {
            val intent = Intent(this, SpecificChat::class.java)

            if (id != null) {
                FirebaseFirestore.getInstance().collection("users").document(id)
                    .get().addOnSuccessListener { document ->
                        if (document != null) {
                            val user = document.toObject(User::class.java)
                            if (user != null) {
                                FirebaseFirestore.getInstance().collection("users/$idCurrent/message").get()
                                    .addOnCompleteListener { _ ->
                                        val timeRequest: MutableMap<String, Any> = HashMap()
                                        timeRequest["id"] = id.toString()
                                        timeRequest["timestamp"] = FieldValue.serverTimestamp()
                                        FirebaseFirestore.getInstance().collection("users/$idCurrent/message")
                                            .document(id)
                                            .set(timeRequest)
                                    }
                                intent.putExtra("name", user.name)
                                intent.putExtra("receiveruid", user.id)
                                intent.putExtra("imageuri", user.image)
                                startActivity(intent)
                            }
                        }
                    }
                    .addOnFailureListener {
                    }
            }

        }
    }
}