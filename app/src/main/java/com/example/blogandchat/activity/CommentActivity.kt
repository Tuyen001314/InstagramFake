package com.example.blogandchat.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blogandchat.R
import com.example.blogandchat.adapter.CommentAdapter
import com.example.blogandchat.databinding.ActivityCommentBinding
import com.example.blogandchat.model.Comments
import com.google.firebase.firestore.*

class CommentActivity : AppCompatActivity() {

    private lateinit var adapter: CommentAdapter
    private lateinit var postCommment: String
    private lateinit var idUserComment: String
    private lateinit var listenerRegistration: ListenerRegistration
    private var listComments: MutableList<Comments> = ArrayList()
    lateinit var binding: ActivityCommentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_comment)

        postCommment = intent.getStringExtra("id").toString()
        idUserComment = intent.getStringExtra("userId").toString()

        getDataComment()

        binding.btnAddOmment.setOnClickListener {
            if (binding.edtComment.text != null) {
                val commentsMap: MutableMap<String, Any> = HashMap()
                commentsMap["user"] = idUserComment
                commentsMap["comment"] = binding.edtComment.text.toString()
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
                        .addOnFailureListener {
                            Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(
                    this,
                    "ban can nhap comment truoc",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
        adapter = CommentAdapter(this, listComments, postCommment)
        binding.recyclerComments.layoutManager = LinearLayoutManager(this)
        binding.recyclerComments.setHasFixedSize(true)
        binding.recyclerComments.adapter = adapter
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
                        val comments: Comments =
                            doc.document.toObject(Comments::class.java).withId(commentsId)
                        listComments.add(comments)
                        adapter.notifyDataSetChanged()
                    } else {
                        adapter.notifyDataSetChanged()
                    }
                }
                listenerRegistration.remove()
            },
        )
    }
}
// else {
//                            if (postCommment != null) {
//                                FirebaseFirestore.getInstance()
//                                    .collection("posts/$postCommment/comments")
//                                    .document(postCommment).delete()
//                            }
//        }
//    }
// }
