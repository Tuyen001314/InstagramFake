package com.example.blogandchat.adapter

import android.annotation.SuppressLint
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
import com.example.blogandchat.model.Message
import com.example.blogandchat.model.User
import com.example.blogandchat.model.UserMessageModel
import com.example.blogandchat.utils.AppKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.ServerTimestamp
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapterUser(
    val  listUser: MutableList<UserMessageModel>
) : RecyclerView.Adapter<ChatAdapterUser.NoteViewHolder?>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_chat_layout_user, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bindView(listUser[position].user, listUser[position].message)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filterlist: MutableList<User>) {
        // below line is to add our filtered
        // list in our course array list.
        //listUser = filterlist
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return listUser.size
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val parentView: ConstraintLayout = itemView.findViewById(R.id.user_parent_view)
        val nameOfUser: TextView = itemView.findViewById(R.id.nameOfUser1)
        val statusOfUser: CircleImageView = itemView.findViewById(R.id.viewStatus)
        val avatar: CircleImageView = itemView.findViewById(R.id.cardviewOfUser1)
        val lastMessage: TextView = itemView.findViewById(R.id.statusOfUser)


        fun bindView(user: User, message: Message) {
            AppKey.calculateKey(user.publicKey.toString())
            Glide.with(parentView.context).load(user.image).into(avatar)
            if (message.type != 1) {
                if (message.senderId == FirebaseAuth.getInstance().uid) {
                        lastMessage.text = "Bạn: " + message.message
                } else {
                   lastMessage.text = message.message
                }
            } else {
                if (message.senderId == FirebaseAuth.getInstance().uid) {
                    lastMessage.text = "Bạn: Hình ảnh"
                } else {
                    lastMessage.text = "Hình ảnh"
                }
            }
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