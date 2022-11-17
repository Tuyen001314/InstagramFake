package com.example.blogandchat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.blogandchat.R
import com.example.blogandchat.model.Message
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.receive_chat_layout.view.*
import kotlinx.android.synthetic.main.send_chat_layout.view.*

class MessageAdapter(): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var context: Context
    private lateinit var listMessage: MutableList<Message>

    companion object {
        const val VIEW_TYPE_ONE = 1
        const val VIEW_TYPE_TWO = 2
    }

    constructor(context: Context, listMessage: MutableList<Message>) : this() {
        this.context = context
        this.listMessage = listMessage
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(p0.context)
        return when (p1) {
            VIEW_TYPE_ONE -> {
                val view = inflater.inflate(R.layout.send_chat_layout, p0, false)
                SenderViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.receive_chat_layout, p0, false)
                ReceiverViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message: Message = listMessage[position]
        if (holder.javaClass == SenderViewHolder::class.java) {
            val viewHolder = holder as SenderViewHolder
            viewHolder.tvMessage.text = message.message
            viewHolder.timeOfMessage.text = message.currentTime
        } else {
            val viewHolder = holder as ReceiverViewHolder
            viewHolder.tvMessage.text = message.message
            viewHolder.timeOfMessage.text = message.currentTime
        }
    }

    override fun getItemCount(): Int {
        return listMessage.size
    }

    override fun getItemViewType(position: Int): Int {
        val message: Message = listMessage[position]
        return if (FirebaseAuth.getInstance().currentUser!!.uid == message.senderId) {
            VIEW_TYPE_ONE
        } else {
            VIEW_TYPE_TWO
        }
    }


    inner class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.send_message
        var timeOfMessage: TextView = itemView.time_message_send
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.receive_message
        var timeOfMessage: TextView = itemView.time_message_receive
    }


}