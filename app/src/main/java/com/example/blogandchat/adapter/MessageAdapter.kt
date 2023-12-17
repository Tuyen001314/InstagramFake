package com.example.blogandchat.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.createBitmap
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogandchat.R
import com.example.blogandchat.model.Message
import com.example.blogandchat.utils.AppKey
import com.google.firebase.auth.FirebaseAuth
import java.util.Base64

class MessageAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
            AppKey.decrypt(message.message)?.let {
                if (message.type != 1) {
                    viewHolder.tvMessage.text = it
                    viewHolder.timeOfMessage.text = message.currentTime
                } else {
                    val bitmap =
                        BitmapFactory.decodeByteArray(it.toByteArray(), 0, it.toByteArray().size)
                    Glide.with(context).load(bitmap).into(viewHolder.img)
                    viewHolder.img.visibility = View.GONE
                    viewHolder.timeOfMessage.visibility = View.GONE
                }

            }

        } else {
            val viewHolder = holder as ReceiverViewHolder
            AppKey.decrypt(message.message)?.let {
                if (message.type != -1) {
                    viewHolder.tvMessage.text = it
                    viewHolder.timeOfMessage.text = message.currentTime
                } else {
                    val bitmap =
                        BitmapFactory.decodeByteArray(it.toByteArray(), 0, it.toByteArray().size)
                    Glide.with(context).load(bitmap).into(viewHolder.img)
                    viewHolder.img.visibility = View.GONE
                    viewHolder.timeOfMessage.visibility = View.GONE
                }

            }
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
        val tvMessage: TextView = itemView.findViewById(R.id.send_message)
        var timeOfMessage: TextView = itemView.findViewById(R.id.time_message_send)
        val img: ImageView = itemView.findViewById(R.id.img_mess)
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.receive_message)
        var timeOfMessage: TextView = itemView.findViewById(R.id.time_message_receive)
        val img: ImageView = itemView.findViewById(R.id.img_mess_receive)

    }


}