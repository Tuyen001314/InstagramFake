package com.example.blogandchat.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogandchat.R
import com.example.blogandchat.model.Comments
import com.example.blogandchat.model.User
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.comment_post.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CommentAdapter() : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private lateinit var context: Context
    private lateinit var listComments: MutableList<Comments>

    constructor(context: Context, listComments: MutableList<Comments>) : this() {
        this.listComments = listComments
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_post, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = listComments[position]
        holder.commentOfUser.text = comment.comment
        val calendar = Calendar.getInstance().time
        //val simpleDateFormat = SimpleDateFormat("hh:mm a")
        val currentTime: Long = calendar.time
        val milliseconds: Long = comment.timestamp!!.toDate().time

        val a = currentTime - milliseconds

        val minutes = TimeUnit.MILLISECONDS.toMinutes(a)

        if(minutes/60 <= 24) {
            if(minutes <= 0) holder.timeComment.text = "vài giây trước"
            else {
                if(minutes < 60) holder.timeComment.text = "$minutes phút trước"
                else
                holder.timeComment.text = "${minutes/60} giờ trước"
            }
        }
        else {
            val date = DateFormat.format("MM/dd/yyyy", Date(milliseconds)).toString()
            holder.timeComment.text = date
        }
        FirebaseFirestore.getInstance().collection("users").whereEqualTo("id", comment.user).get().addOnSuccessListener { documents ->
            for (document in documents) {
                // val firebaseModel =
                val user = (document.toObject(User::class.java))
                Glide.with(context).load(user.image).into(holder.imageUserComment)
                holder.nameUserComment.text = user.name
                break
            }
        }
    }

    override fun getItemCount(): Int {
        return listComments.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageUserComment: CircleImageView = itemView.circleImageView_comment
        val nameUserComment: TextView = itemView.tv_user_comment
        val commentOfUser: TextView = itemView.comment_tv
        val timeComment: TextView = itemView.timeComment
    }

}