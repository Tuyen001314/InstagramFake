package com.example.blogandchat.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.blogandchat.OnClickImage
import com.example.blogandchat.R
import com.example.blogandchat.adapter.ProfileAdapter
import com.example.blogandchat.fragment.ImageFragment
import com.example.blogandchat.model.Post
import com.example.blogandchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import kotlinx.android.synthetic.main.activity_other_user_profile.*
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {
    private lateinit var user: User
    private var listPostUser: MutableList<Post> = ArrayList();
    private lateinit var adapter: ProfileAdapter
    private lateinit var listenerRegistration: ListenerRegistration


    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val docRef = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().uid.toString())

        docRef.get().addOnSuccessListener { documentSnapshot ->
            user = documentSnapshot.toObject<com.example.blogandchat.model.User>()!!

            edt_name_enter.text = user.name
            Glide.with(this).load(user.image).into(circleImageView)
        }

        btn_edit.setOnClickListener {
            val intent: Intent = Intent(this, SetUpActivity::class.java)
            intent.putExtra("email", user.email)
            intent.putExtra("id", user.id)
            startActivity(intent)
            //startActivity(Intent(this, SetUpActivity::class.java));
        }

        val id = FirebaseAuth.getInstance().uid
        FirebaseFirestore.getInstance().collection("users/$id/following")
            .get().addOnSuccessListener { documents ->
                val tmp = documents.size()
                followingMyProfile.text = "Following: $tmp"
            }

        FirebaseFirestore.getInstance().collection("users/$id/follower")
            .get().addOnSuccessListener { documents ->
                val tmp = documents.size()
                followerMyProfile.text = "Follower: $tmp"
            }

        adapter = ProfileAdapter(this, listPostUser, object : OnClickImage {
            override fun click(id: String) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.setting_activity, ImageFragment(id), null)
                    .commit()
            }

        })

        val query: Query = FirebaseFirestore.getInstance().collection("posts").orderBy("time", Query.Direction.DESCENDING)
        listenerRegistration = query.addSnapshotListener(
            EventListener<QuerySnapshot?> { value, _ ->
                for (doc in value!!.documentChanges) {
                    if (doc.type == DocumentChange.Type.ADDED) {
                        val postId = doc.document.id
                        val post: Post = doc.document.toObject(Post::class.java).withId(postId)
                        if(post.user == FirebaseAuth.getInstance().uid) {
                            listPostUser.add(post)
                        }
                        adapter.notifyDataSetChanged()
                    } else {
                        adapter.notifyDataSetChanged()
                    }
                }
                listenerRegistration.remove()
            })


        recyclerViewProfile.setHasFixedSize(true)
        recyclerViewProfile.layoutManager = GridLayoutManager(this, 3);
        recyclerViewProfile.adapter = adapter

    }
}