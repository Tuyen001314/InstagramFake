package com.example.blogandchat.adapter


import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.example.blogandchat.R
import com.example.blogandchat.model.Video
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions


class VideoAdapter(options: FirebaseRecyclerOptions<Video>) : FirebaseRecyclerAdapter<Video, VideoAdapter.ViewHolder>(
    options
) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.each_video, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Video) {
        holder.setData(model)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val video: VideoView = itemView.findViewById(R.id.videoView)

        fun setData(obj: Video) {
            video.setVideoPath(obj.videoUrl)
//            title.setText(obj.getTitle())
//            desc.setText(obj.getDesc())

            video.setOnPreparedListener(OnPreparedListener { mediaPlayer ->
//                pbar.setVisibility(View.GONE)
                mediaPlayer.start()
            })

            video.setOnCompletionListener(OnCompletionListener { mediaPlayer -> mediaPlayer.start() })
        }
    }
}