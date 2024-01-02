package com.example.blogandchat.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.blogandchat.R
import com.example.blogandchat.model.ExoPlayerItem
import com.example.blogandchat.model.Message
import com.example.blogandchat.utils.AppKey
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.ByteArrayDataSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.firebase.auth.FirebaseAuth
import java.io.File

class MessageAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var context: Context
    private lateinit var listMessage: MutableList<Message>

    companion object {
        const val VIEW_TYPE_ONE = 1
        const val VIEW_TYPE_TWO = 2
        const val IMAGE_SENDER = 3
        const val IMAGE_RECEIVER = 4
        const val VIDEO_SENDER = 5
        const val VIDEO_RECEIVER = 6
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

            VIDEO_SENDER -> {
                val view = inflater.inflate(R.layout.send_video_layout, p0, false)
                VideoSenderViewHolder(view)
            }

            VIDEO_RECEIVER -> {
                val view = inflater.inflate(R.layout.receive_video_layout, p0, false)
                VideoReceiverViewHolder(view)
            }


            else -> {
                val view = inflater.inflate(R.layout.empty, p0, false)
                EmptyView(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message: Message = listMessage[position]
        when (holder) {
            is SenderViewHolder -> {
                holder.tvMessage.text = message.message
                holder.timeOfMessage.text = message.currentTime
            }

            is ReceiverViewHolder -> {
                holder.tvMessage.text = message.message
                holder.timeOfMessage.text = message.currentTime
            }

            is ImageSenderViewHolder -> {
                val byteArray = Base64.decode(message.message,Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                Glide.with(context)
                    .load(bitmap)
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: com.bumptech.glide.request.transition.Transition<in Drawable>?,
                        ) {
                            holder.img.setImageDrawable(resource)
                        }


                        override fun onLoadCleared(placeholder: Drawable?) {
                            // Do nothing
                        }
                    })
            }

            is ImageReceiverViewHolder -> {
                val byteArray = Base64.decode(message.message,Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                Glide.with(context)
                    .load(bitmap)
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: com.bumptech.glide.request.transition.Transition<in Drawable>?,
                        ) {
                            holder.img.setImageDrawable(resource)
                        }


                        override fun onLoadCleared(placeholder: Drawable?) {
                            // Do nothing
                        }
                    })
            }

            is VideoSenderViewHolder -> {
                holder.bindView(message)
            }

            is VideoReceiverViewHolder -> {
                holder.bindView(message)
            }
        }
    }

    override fun getItemCount(): Int {
        return listMessage.size
    }

    override fun getItemViewType(position: Int): Int {
        val message: Message = listMessage[position]
        return if (FirebaseAuth.getInstance().currentUser!!.uid == message.senderId) {
            if (message.type == 0) VIEW_TYPE_ONE else if (message.type == 1) IMAGE_SENDER else VIDEO_SENDER
        } else {
            if (message.type == 0) VIEW_TYPE_TWO else if (message.type == 1) IMAGE_RECEIVER else VIDEO_RECEIVER
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

    inner class VideoReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerView: StyledPlayerView = itemView.findViewById(R.id.video_mess_receive)
        val pbLoading: ProgressBar = itemView.findViewById(R.id.pbLoading)
        fun bindView(message: Message) {
            val byteArray = Base64.decode(message.message,Base64.DEFAULT)
            val tempFile = File.createTempFile("tempVideo", ".mp4", itemView.context.cacheDir)
            tempFile.writeBytes(byteArray)

            val exoPlayer = ExoPlayer.Builder(itemView.rootView.context).build()
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    Toast.makeText(
                        itemView.rootView.context,
                        "Can't play this video",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    if (playbackState == Player.STATE_BUFFERING) {
                        pbLoading.visibility = View.VISIBLE
                    } else if (playbackState == Player.STATE_READY) {
                        pbLoading.visibility = View.GONE
                    }
                }
            })

            playerView.player = exoPlayer

            exoPlayer.seekTo(0)
            exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

            val dataSourceFactory = DefaultDataSource.Factory(itemView.rootView.context)

            val mediaSource = Uri.fromFile(tempFile)?.let { MediaItem.fromUri(it) }?.let {
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                    it
                )
            }

            mediaSource?.let { exoPlayer.setMediaSource(it) }
            exoPlayer.prepare()

            if (absoluteAdapterPosition == 0) {
                exoPlayer.playWhenReady = true
                exoPlayer.play()
            }
        }
    }

    inner class VideoSenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerView: StyledPlayerView = itemView.findViewById(R.id.video_mess_send)
        val pbLoading: ProgressBar = itemView.findViewById(R.id.pbLoading)
        fun bindView(message: Message) {
            val byteArray = Base64.decode(message.message,Base64.DEFAULT)
            val tempFile = File.createTempFile("tempVideo", ".mp4", itemView.context.cacheDir)
            tempFile.writeBytes(byteArray)

            val exoPlayer = ExoPlayer.Builder(itemView.rootView.context).build()
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    Toast.makeText(
                        itemView.rootView.context,
                        "Can't play this video",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    if (playbackState == Player.STATE_BUFFERING) {
                        pbLoading.visibility = View.VISIBLE
                    } else if (playbackState == Player.STATE_READY) {
                        pbLoading.visibility = View.GONE
                    }
                }
            })

            playerView.player = exoPlayer

            exoPlayer.seekTo(0)
            exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

            val dataSourceFactory = DefaultDataSource.Factory(itemView.rootView.context)

            val mediaSource = Uri.fromFile(tempFile)?.let { MediaItem.fromUri(it) }?.let {
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                    it
                )
            }

            mediaSource?.let { exoPlayer.setMediaSource(it) }
            exoPlayer.prepare()

            if (absoluteAdapterPosition == 0) {
                exoPlayer.playWhenReady = true
                exoPlayer.play()
            }
        }

    }

    inner class EmptyView(itemView: View) : RecyclerView.ViewHolder(itemView)

}