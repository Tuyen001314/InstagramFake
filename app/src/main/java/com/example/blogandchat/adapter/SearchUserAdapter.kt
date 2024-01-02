package com.example.blogandchat.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogandchat.R
import com.example.blogandchat.activity.SpecificChat
import com.example.blogandchat.model.User

class SearchUserAdapter :
    ListAdapter<User, SearchUserAdapter.ViewHolder>(object : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.name == newItem.name
        }

    }) {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val img: ImageView = itemView.findViewById(R.id.img_user)
        private val name: TextView = itemView.findViewById(R.id.tv_name)

        fun bindView(user: User) {
            Glide.with(itemView.rootView.context).load(user.image).into(img)
            name.text = user.name
            itemView.setOnClickListener { v ->
                val intent = Intent(v.context, SpecificChat::class.java)
                intent.putExtra("name", user.name)
                intent.putExtra("receiveruid", user.id)
                intent.putExtra("imageuri", user.image)
                intent.putExtra("publicKey", user.publicKey)
                v.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater =
            LayoutInflater.from(parent.context).inflate(R.layout.item_user_search, parent, false)
        return ViewHolder(layoutInflater)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(getItem(position))
    }
}