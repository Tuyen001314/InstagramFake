package com.example.blogandchat.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogandchat.R
import com.example.blogandchat.model.Comments
import com.example.blogandchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.comment_post.view.*
import java.util.*
import java.util.concurrent.TimeUnit

class CommentAdapter() : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    private lateinit var context: Context
    private lateinit var listComments: MutableList<Comments>
    private lateinit var postComments: String

    constructor(
        context: Context,
        listComments: MutableList<Comments>,
        postComments: String
    ) : this() {
        this.listComments = listComments
        this.context = context
        this.postComments = postComments
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_post, parent, false)
        return ViewHolder(view, postComments)
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

        if (minutes / 60 <= 24) {
            if (minutes <= 0) holder.timeComment.text = "vài giây trước"
            else {
                if (minutes < 60) holder.timeComment.text = "$minutes phút trước"
                else
                    holder.timeComment.text = "${minutes / 60} giờ trước"
            }
        } else {
            val date = DateFormat.format("MM/dd/yyyy", Date(milliseconds)).toString()
            holder.timeComment.text = date
        }

        FirebaseFirestore.getInstance().collection("users").whereEqualTo("id", comment.user).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val user = (document.toObject(User::class.java))
                    Glide.with(context).load(user.image).into(holder.imageUserComment)
                    holder.nameUserComment.text = user.name
                    break
                }
            }

        if (FirebaseAuth.getInstance().uid == comment.user) {
            holder.eraseComment.visibility = View.VISIBLE
        }

        holder.eraseComment.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(context)

            // set message of alert dialog
            dialogBuilder.setMessage("Bạn có muốn xóa bài đăng này không ?")
                // if the dialog is cancelable
                .setCancelable(false)
                // positive button text and action
                .setPositiveButton("Có", DialogInterface.OnClickListener { dialog, id ->
                    FirebaseFirestore.getInstance().collection("posts/$postComments/comments")
                        .document("${comment.CommentsId}").delete()
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Xóa thành công",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "$e",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                })
                // negative button text and action
                .setNegativeButton("Không", DialogInterface.OnClickListener { dialog, id ->
                    dialog.cancel()
                })

            // create dialog box
            val alert = dialogBuilder.create()
            // set title for alert dialog box
            alert.setTitle("")
            // show alert dialog
            alert.show()
        }
    }


    override fun getItemCount(): Int {
        return listComments.size
    }

    class ViewHolder(itemView: View, private val postComments: String) :
        RecyclerView.ViewHolder(itemView) {
        val imageUserComment: CircleImageView = itemView.circleImageView_comment
        val nameUserComment: TextView = itemView.tv_user_comment
        val commentOfUser: TextView = itemView.comment_tv
        val timeComment: TextView = itemView.timeComment
        val eraseComment: ImageButton = itemView.imag_btn_delete_comment


    }
}

