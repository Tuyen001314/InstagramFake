package com.example.blogandchat.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogandchat.PostDetailListener
import com.example.blogandchat.R
import com.example.blogandchat.activity.CommentActivity
import com.example.blogandchat.activity.OtherUserProfile
import com.example.blogandchat.activity.SettingActivity
import com.example.blogandchat.model.PostDetailModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*


class PostAdapter(val onClickImage: PostDetailListener, val context: Context) :
    ListAdapter<PostDetailModel, PostAdapter.ViewHolder>(object :
        DiffUtil.ItemCallback<PostDetailModel>() {
        override fun areItemsTheSame(oldItem: PostDetailModel, newItem: PostDetailModel): Boolean {
            return oldItem.postId == newItem.postId
        }

        override fun areContentsTheSame(
            oldItem: PostDetailModel,
            newItem: PostDetailModel,
        ): Boolean {
            return oldItem.caption == newItem.caption && oldItem.isLiked == newItem.isLiked && oldItem.image == newItem.image
        }

    }) {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.each_post, parent, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        return ViewHolder(view, onClickImage)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post: PostDetailModel = getItem(position)
        Glide.with(context).load(post.image).into(holder.postPic)

        holder.bindData(post.image)

        holder.setPostCaption(post.caption)

        holder.setPostDate(post.timeForShow)
        holder.setPostUsername(post.userName)
        Glide.with(context).load(post.imageUser).into(holder.profilePic)
        holder.userName.setOnClickListener {
            if (!post.isYour) {
                val intent = Intent(context, OtherUserProfile::class.java);
                intent.putExtra("id", post.idUser)
                context.startActivity(intent)
            } else {
                context.startActivity(Intent(context, SettingActivity::class.java))
            }
        }

        if (post.isYour) {
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
                            .document("${post.postId}").delete()
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

        } else {
            holder.deletePost.visibility = View.INVISIBLE
        }

        if (post.isLiked) {
            holder.likeImage.setImageDrawable(context.getDrawable(R.drawable.affer_liked))
        } else {
            holder.likeImage.setImageDrawable(context.getDrawable(R.drawable.heart_1))
        }

        holder.commentPost.setOnClickListener {
            val intent = Intent(context, CommentActivity::class.java)
            intent.putExtra("id", post.postId)
            intent.putExtra("userId", FirebaseAuth.getInstance().uid)
            context.startActivity(intent)
        }


        holder.likeImage.setOnClickListener {
            if (!post.isLiked) {
                post.isLiked = true
                holder.likeImage.setImageDrawable(context.getDrawable(R.drawable.affer_liked))
            } else {
                post.isLiked = false
                holder.likeImage.setImageDrawable(context.getDrawable(R.drawable.heart_1))
            }
            onClickImage.like(post.postId.toString())

        }
    }


    class ViewHolder(itemView: View, private val onClickImage: PostDetailListener) :
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