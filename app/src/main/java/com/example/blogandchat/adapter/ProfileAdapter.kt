package com.example.blogandchat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogandchat.OnClickImage
import com.example.blogandchat.R
import com.example.blogandchat.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.each_post_user.view.*
import java.security.spec.PSSParameterSpec

class ProfileAdapter(): RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {
    lateinit var listPostUser: MutableList<Post>
    lateinit var context: Context
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var onClickImage: OnClickImage

    constructor(context: Context, listPostUser: MutableList<Post>, onClickImage: OnClickImage) : this() {
        this.listPostUser = listPostUser
        this.context = context
        this.onClickImage = onClickImage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.each_post_user, parent, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        return ProfileAdapter.ViewHolder(view, onClickImage)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post: Post = listPostUser[position]
        Glide.with(context).load(post.image).into(holder.imagePost)
        holder.bindData(post.image)
    }

    override fun getItemCount(): Int {
        return listPostUser.size
    }

    class ViewHolder(itemView: View, private val onClickImage: OnClickImage) : RecyclerView.ViewHolder(itemView) {
        val imagePost: ImageView = itemView.user_post_image

        fun bindData(id: String) {
            imagePost.setOnClickListener {
                onClickImage.click(id)
                //Log.d("image", id.toString() + "")
            }
        }
    }
}