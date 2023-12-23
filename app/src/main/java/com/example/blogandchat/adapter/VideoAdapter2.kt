package com.example.blogandchat.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.blogandchat.databinding.EachVideoBinding
import com.example.blogandchat.model.ExoPlayerItem
import com.example.blogandchat.model.Video
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import java.util.logging.Logger

class VideoAdapter2(
    var context: Context,
    var videos: ArrayList<Video>,
    var videoPreparedListener: OnVideoPreparedListener,
) : RecyclerView.Adapter<VideoAdapter2.VideoViewHolder>() {

    class VideoViewHolder(
        val binding: EachVideoBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun setVideoPath(url: String, videoPreparedListener: OnVideoPreparedListener) {

            val exoPlayer = ExoPlayer.Builder(binding.root.context).build()
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    Log.d("buituyen", error.message.toString())
                    Toast.makeText(binding.root.context, "Can't play this video", Toast.LENGTH_SHORT).show()
                }

                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    if (playbackState == Player.STATE_BUFFERING) {
                        binding.pbLoading.visibility = View.VISIBLE
                    } else if (playbackState == Player.STATE_READY) {
                        binding.pbLoading.visibility = View.GONE
                    }
                }
            })

            binding.playerView.player = exoPlayer

            exoPlayer.seekTo(0)
            exoPlayer.repeatMode = Player.REPEAT_MODE_ONE

            val dataSourceFactory = DefaultDataSource.Factory(binding.root.context)

            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                MediaItem.fromUri(Uri.parse(url))
            )

            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()

            if (absoluteAdapterPosition == 0) {
                exoPlayer.playWhenReady = true
                exoPlayer.play()
            }

            videoPreparedListener.onVideoPrepared(ExoPlayerItem(exoPlayer, absoluteAdapterPosition))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = EachVideoBinding.inflate(LayoutInflater.from(context), parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val model = videos[position]
        holder.setVideoPath(model.videoUrl, videoPreparedListener)
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    interface OnVideoPreparedListener {
        fun onVideoPrepared(exoPlayerItem: ExoPlayerItem)
    }
}