package com.example.blogandchat.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogandchat.R
import com.example.blogandchat.activity.SpecificChat
import com.example.blogandchat.model.User
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter() : RecyclerView.Adapter<ChatAdapter.NoteViewHolder?>() {
    private lateinit var listUser: MutableList<User>
    private lateinit var context: Context

    constructor(context: Context, listUser: MutableList<User>) : this() {
        this.listUser = listUser
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_chat_layout, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bindView(listUser[position])
    }


    override fun getItemCount(): Int {
        return listUser.size
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val parentView: ConstraintLayout = itemView.findViewById(R.id.parent_view)
        val nameOfUser: TextView = itemView.findViewById(R.id.nameOfUser)
        val statusOfUser: CircleImageView = itemView.findViewById(R.id.circleImageViewStatus)
        val avatar: CircleImageView = itemView.findViewById(R.id.cardviewOfUser)

        fun bindView(user: User) {
            Glide.with(itemView.context).load(user.image).into(avatar)
            nameOfUser.text = user.name
            if (user.status == "online") {
                statusOfUser.visibility = View.VISIBLE
            } else {
                statusOfUser.visibility = View.INVISIBLE
            }
            parentView.setOnClickListener(View.OnClickListener { v ->
                val intent = Intent(v.context, SpecificChat::class.java)
                intent.putExtra("name", user.name)
                intent.putExtra("receiveruid", user.id)
                intent.putExtra("imageuri", user.image)
                intent.putExtra("publicKey", user.publicKey)
                v.context.startActivity(intent)
            })
        }
    }

}