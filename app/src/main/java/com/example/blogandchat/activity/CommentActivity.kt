package com.example.blogandchat.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blogandchat.R
import com.example.blogandchat.adapter.CommentAdapter
import com.example.blogandchat.model.Comments
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_comment.*

class CommentActivity : AppCompatActivity() {

    private lateinit var adapter: CommentAdapter
    private lateinit var postCommment: String
    private lateinit var idUserComment: String
    private lateinit var listenerRegistration: ListenerRegistration
    private var listComments: MutableList<Comments> = ArrayList()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        postCommment = intent.getStringExtra("id").toString()
        idUserComment = intent.getStringExtra("userId").toString()

        getDataComment()

        btn_add_omment.setOnClickListener {
            if (edt_comment.text != null) {
                val commentsMap: MutableMap<String, Any> = HashMap()
                commentsMap["user"] = idUserComment
                commentsMap["comment"] = edt_comment.text.toString()
                commentsMap["timestamp"] = FieldValue.serverTimestamp()
                if (postCommment != null) {
                    val ref = FirebaseFirestore.getInstance()
                        .collection("posts/$postCommment/comments").document()
                        .set(commentsMap)
                        .addOnSuccessListener {
                            val mIntent = intent
                            finish()
                            startActivity(mIntent)
                        }
                        .addOnFailureListener { Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show()}
                }
            } else {
                Toast.makeText(
                    this,
                    "ban can nhap comment truoc",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        adapter = CommentAdapter(this, listComments, postCommment);
        recycler_comments.layoutManager = LinearLayoutManager(this)
        recycler_comments.setHasFixedSize(true)
        recycler_comments.adapter = adapter

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getDataComment() {
        val query = FirebaseFirestore.getInstance().collection("posts/$postCommment/comments")
            .orderBy("timestamp", Query.Direction.DESCENDING)
        listenerRegistration = query.addSnapshotListener(
            EventListener<QuerySnapshot?> { value, _ ->
                for (doc in value!!.documentChanges) {
                    if (doc.type == DocumentChange.Type.ADDED) {
                        val commentsId = doc.document.id
                        val comments: Comments = doc.document.toObject(Comments::class.java).withId(commentsId)
                        listComments.add(comments)
                        adapter.notifyDataSetChanged()
                    } else {
                        adapter.notifyDataSetChanged()
                    }
                }
                listenerRegistration.remove()
            })
    }
}
//else {
//                            if (postCommment != null) {
//                                FirebaseFirestore.getInstance()
//                                    .collection("posts/$postCommment/comments")
//                                    .document(postCommment).delete()
//                            }
//        }
//    }
//}