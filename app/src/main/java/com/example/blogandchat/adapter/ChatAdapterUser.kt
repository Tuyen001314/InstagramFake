package com.example.blogandchat.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogandchat.R
import com.example.blogandchat.activity.SpecificChat
import com.example.blogandchat.model.User
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.view_chat_layout.view.*
import kotlinx.android.synthetic.main.view_chat_layout.view.cardviewOfUser
import kotlinx.android.synthetic.main.view_chat_layout.view.circleImageViewStatus
import kotlinx.android.synthetic.main.view_chat_layout.view.nameOfUser
import kotlinx.android.synthetic.main.view_chat_layout_user.view.*

class ChatAdapterUser() : RecyclerView.Adapter<ChatAdapterUser.NoteViewHolder?>() {
    private lateinit var listUser: MutableList<User>
    private lateinit var context: Context

    constructor(context: Context, listUser: MutableList<User>) : this() {
        this.listUser = listUser
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_chat_layout_user, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {

        val firebaseModel: User = listUser[position]

        Glide.with(context).load(firebaseModel.image).into(holder.avatar)
        holder.nameOfUser.text = firebaseModel.name
        if (firebaseModel.status == "online") {
            holder.statusOfUser.visibility = View.VISIBLE
        } else {
            holder.statusOfUser.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener(View.OnClickListener { v ->
            val intent = Intent(v.context, SpecificChat::class.java)
            intent.putExtra("name", firebaseModel.name)
            intent.putExtra("receiveruid", firebaseModel.id)
            intent.putExtra("imageuri", firebaseModel.image)
            v.context.startActivity(intent)
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filterlist: MutableList<User>) {
        // below line is to add our filtered
        // list in our course array list.
        listUser = filterlist
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return listUser.size
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameOfUser: TextView = itemView.nameOfUser1
        val statusOfUser: CircleImageView = itemView.viewStatus
        val avatar: CircleImageView = itemView.cardviewOfUser1

    }

}