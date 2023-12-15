package com.example.blogandchat.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogandchat.OnClickImage
import com.example.blogandchat.R
import com.example.blogandchat.activity.CommentActivity
import com.example.blogandchat.activity.OtherUserProfile
import com.example.blogandchat.activity.SettingActivity
import com.example.blogandchat.dialog.CommentDialogFragment
import com.example.blogandchat.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import java.util.concurrent.TimeUnit


class PostAdapter() : RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    lateinit var listPost: MutableList<Post>
    lateinit var context: Context
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var onClickImage: OnClickImage

    private lateinit var onClickComment: (id: String, postId: String) -> Unit

    constructor(
        context: Context,
        listPost: MutableList<Post>,
        onClickImage: OnClickImage
    ) : this() {
        this.listPost = listPost
        this.context = context
        this.onClickImage = onClickImage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.each_post, parent, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        return ViewHolder(view, onClickImage)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post: Post = listPost[position]
        Glide.with(context).load(post.image).into(holder.postPic)

        holder.bindData(post.image)

        holder.setPostCaption(post.caption)

        val calendar = Calendar.getInstance().time
        //val simpleDateFormat = SimpleDateFormat("hh:mm a")
        val currentTime: Long = calendar.time
        val milliseconds: Long = post.time!!.toDate().time

        val a = currentTime - milliseconds

        val minutes = TimeUnit.MILLISECONDS.toMinutes(a)

        if (minutes / 60 <= 24) {
            if (minutes <= 0) holder.setPostDate("vài giây trước")
            else {
                if (minutes < 60) holder.setPostDate("$minutes phút trước")
                else
                    holder.setPostDate("${minutes / 60} giờ trước")
            }
        } else {
            val date = DateFormat.format("MM/dd/yyyy", Date(milliseconds)).toString()
            holder.setPostDate(date)
        }

        val userId: String = post.user
        firestore.collection("users").document(userId).get().addOnCompleteListener { task ->

            if (task.isSuccessful) {
                val userName: String? = task.result.getString("name");
                val image: String? = task.result.getString("image");
                val idUser = task.result.getString("id");

                holder.setPostUsername(userName)
                Glide.with(context).load(image).into(holder.profilePic)
                holder.userName.setOnClickListener {
                    if(idUser != FirebaseAuth.getInstance().uid) {
                        val intent = Intent(context, OtherUserProfile::class.java);
                        intent.putExtra("id", idUser)
                        context.startActivity(intent)
                    }
                    else {
                        context.startActivity(Intent(context, SettingActivity::class.java))
                    }
                }
            } else {
                Toast.makeText(context, task.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }

        val postId = post.postId
        val idUser = FirebaseAuth.getInstance().uid

        if (post.user == idUser) {
            holder.deletePost.visibility = View.VISIBLE
            holder.deletePost.setOnClickListener {
                val dialogBuilder = AlertDialog.Builder(context)

                // set message of alert dialog
                dialogBuilder.setMessage("Bạn có muốn xóa bài đăng này không ?")
                    // if the dialog is cancelable
                    .setCancelable(false)
                    // positive button text and action
                    .setPositiveButton("Có", DialogInterface.OnClickListener { dialog, id ->
                        FirebaseFirestore.getInstance().collection("posts")
                            .document("$postId").delete()
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
        else {
            holder.deletePost.visibility = View.INVISIBLE
        }

        if (idUser != null) {
            firestore.collection(
                "posts/" +
                        postId + "/likes"
            ).document(idUser).addSnapshotListener { snapshot, e ->
                if (e == null) {
                    if (snapshot != null) {
                        if (snapshot.exists()) {
                            holder.likeImage.setImageDrawable(context.getDrawable(R.drawable.affer_liked))
                        } else {
                            holder.likeImage.setImageDrawable(context.getDrawable(R.drawable.heart_1))
                        }
                    }
                }
            }
        }

        holder.commentPost.setOnClickListener {
            if (postId != null) {
                FirebaseAuth.getInstance().uid?.let { it1 -> onClickComment.invoke(postId, it1) }
            }
        }

        holder.likeImage.setOnClickListener {
            val ref = FirebaseAuth.getInstance().uid?.let { it1 ->
                FirebaseFirestore.getInstance().collection(
                    "posts/" +
                            postId + "/likes"
                ).document(it1).get().addOnCompleteListener { task ->

                    if (!task.result.exists()) {
                        val likesMap: MutableMap<String, Any> = HashMap()
                        likesMap["timestamp"] = FieldValue.serverTimestamp()
                        if (idUser != null) {
                            firestore.collection("posts/$postId/likes").document(idUser)
                                .set(likesMap)
                        }
                    } else {
                        if (idUser != null) {
                            firestore.collection("posts/$postId/likes").document(idUser).delete()
                        }
                    }

                    FirebaseAuth.getInstance().uid?.let { it2 ->
                        firestore.collection(
                            "posts/" +
                                    postId + "/likes"
                        ).document(it2).addSnapshotListener { snapshot, e ->
                            if (e == null) {
                                if (snapshot != null) {
                                    if (snapshot.exists()) {
                                        holder.likeImage.setImageDrawable(context.getDrawable(R.drawable.affer_liked))
                                    } else {
                                        holder.likeImage.setImageDrawable(context.getDrawable(R.drawable.heart_1))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun onCLickComment(onClickComment: (String, String) -> Unit) {
        this.onClickComment = onClickComment
    }

    override fun getItemCount(): Int {
        return listPost.size
    }


    class ViewHolder(itemView: View, private val onClickImage: OnClickImage) :
        RecyclerView.ViewHolder(itemView) {

        val likeImage: ImageView = itemView.findViewById(R.id.img_view_like)
        val caption: TextView = itemView.findViewById(R.id.tv_caption)
        var userName: TextView = itemView.findViewById(R.id.tv_username)
        val date: TextView = itemView.findViewById(R.id.tv_date)
        val postPic: ImageView = itemView.findViewById(R.id.user_post)
        val profilePic: CircleImageView = itemView.findViewById(R.id.profile_pic)
        val deletePost: ImageView = itemView.findViewById(R.id.image_button_delete)
        val commentPost: ImageView = itemView.findViewById(R.id.img_view_comment)

//        fun setPostLikes(count: Int) {
//            val postLikes = mView.tv_count_like
//            postLikes.text = "$count Likes"
//        }

        fun bindData(id: String) {
            postPic.setOnClickListener {
                onClickImage.click(id)
                //Log.d("image", id.toString() + "")
            }
        }

        fun setPostUsername(username: String?) {
            this.userName.text = username
        }

        fun setPostDate(date: String?) {
          this.date.text = date
        }

        fun setPostCaption(caption: String?) {
           this.caption.text = caption
        }
    }
}