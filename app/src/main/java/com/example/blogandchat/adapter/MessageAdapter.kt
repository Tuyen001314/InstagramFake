package com.example.blogandchat.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.blogandchat.R
import com.example.blogandchat.model.Message
import com.example.blogandchat.utils.AppKey
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var context: Context
    private lateinit var listMessage: MutableList<Message>

    companion object {
        const val VIEW_TYPE_ONE = 1
        const val VIEW_TYPE_TWO = 2
        const val IMAGE_SENDER = 3
        const val IMAGE_RECEIVER = 4
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

            VIEW_TYPE_TWO -> {
                val view = inflater.inflate(R.layout.receive_chat_layout, p0, false)
                ReceiverViewHolder(view)
            }

            IMAGE_SENDER -> {
                val view = inflater.inflate(R.layout.send_image_layout, p0, false)
                ImageSenderViewHolder(view)
            }

            IMAGE_RECEIVER -> {
                val view = inflater.inflate(R.layout.receive_image_layout, p0, false)
                ImageReceiverViewHolder(view)
            }

            else -> {
                val view = inflater.inflate(R.layout.empty, p0, false)
                EmptyView(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message: Message = listMessage[position]
        if (holder.javaClass == SenderViewHolder::class.java) {
            val viewHolder = holder as SenderViewHolder
            AppKey.decrypt(message.message)?.let {
                viewHolder.tvMessage.text = it
                viewHolder.timeOfMessage.text = message.currentTime
            }
        } else if (holder is ReceiverViewHolder) {
            AppKey.decrypt(message.message)?.let {
                holder.tvMessage.text = it
                holder.timeOfMessage.text = message.currentTime
            }
        } else if (holder is ImageSenderViewHolder) {
            val byteArray = AppKey.decryptByteArray(message.message)
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            Glide.with(context)
                .load(bitmap)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
                    ) {
                        holder.img.setImageDrawable(resource)
                    }


                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Do nothing
                    }
                })
        } else if (holder is ImageReceiverViewHolder) {
            val byteArray = AppKey.decryptByteArray(message.message)
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            Glide.with(context)
                .load(bitmap)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
                    ) {
                        holder.img.setImageDrawable(resource)
                    }


                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Do nothing
                    }
                })

        } else {

        }
    }

    override fun getItemCount(): Int {
        return listMessage.size
    }

    override fun getItemViewType(position: Int): Int {
        val message: Message = listMessage[position]
        return if (FirebaseAuth.getInstance().currentUser!!.uid == message.senderId) {
            if (message.type != 1)
                VIEW_TYPE_ONE else IMAGE_SENDER
        } else {
            if (message.type != 1)
                VIEW_TYPE_TWO else IMAGE_RECEIVER
        }
    }


    inner class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.send_message)
        var timeOfMessage: TextView = itemView.findViewById(R.id.time_message_send)
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.receive_message)
        var timeOfMessage: TextView = itemView.findViewById(R.id.time_message_receive)
    }

    inner class ImageSenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.img_mess)
    }

    inner class ImageReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.img_mess_receive)
    }

    inner class EmptyView(itemView: View) : RecyclerView.ViewHolder(itemView)

}