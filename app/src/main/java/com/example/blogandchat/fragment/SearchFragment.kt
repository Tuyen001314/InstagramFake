package com.example.blogandchat.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.blogandchat.R
import com.example.blogandchat.adapter.VideoAdapter2
import com.example.blogandchat.databinding.FragmentSearchBinding
import com.example.blogandchat.model.ExoPlayerItem
import com.example.blogandchat.model.Video
import com.google.android.exoplayer2.ExoPlayer
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {

    private lateinit var adapter: VideoAdapter2

    private lateinit var player: ExoPlayer

    private val videos = ArrayList<Video>()
    private val exoPlayerItems = ArrayList<ExoPlayerItem>()

    private lateinit var binding: FragmentSearchBinding

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_search,container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       /* CoroutineScope(Dispatchers.IO).launch {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val videosRef = storageRef.child("videos") // Replace with the path to your videos folder
            videosRef.listAll()
                .addOnSuccessListener { listResult ->
                    val videoFiles = listResult.items
                    for (videoRef in videoFiles) {
                        videoRef.downloadUrl
                            .addOnSuccessListener { uri ->
                                val downloadUrl = uri.toString()
                                videos.add(Video(downloadUrl))
                                Log.d(
                                    "buituyen videos ",
                                    downloadUrl
                                )
                                Log.d("buituyen", "min addOnCompleteListener")

                                // Use the download URL as needed (e.g., display in a list or store in a database)
                            }
                            .addOnFailureListener { exception ->
                                // Handle any errors that occurred while retrieving the download URL
                                Log.d("buituyen", "error addOnCompleteListener")
                            }
                        *//*delay(10000)
                        Log.d("buituyen size", videos.size.toString())*//*
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle any errors that occurred while listing the video files
                    Log.d("buituyen", "max addOnCompleteListener")

                }.addOnCompleteListener {

                    Log.d("buituyen", "max addOnCompleteListener")
                }

            withContext(Dispatchers.Main) {
                delay(10000)
            }
        }*/


         videos.add(
             Video(
                 "https://www.shutterstock.com/shutterstock/videos/1100819565/preview/stock-footage--shanghai-china-cityscape-above-the-pudong-financial-district-hyper-lapse-video.webm"
             )
         )

         videos.add(
             Video(
                 "https://www.shutterstock.com/shutterstock/videos/1076037986/preview/stock-footage-aerial-view-of-sunrise-through-airliner-window-in-the-morning-aerial-view-of-cloudscape-in-dawn.webm"
             )
         )
         videos.add(
             Video(
                 "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
             )
         )
        videos.add(
            Video(
                "https://www.shutterstock.com/shutterstock/videos/1086628187/preview/stock-footage-aerial-footage-of-beautiful-mountain-and-river-natural-scenery-in-guilin-at-sunrise-china-lijiang.webm"
            )
        )
        videos.add(
            Video(
                "https://www.shutterstock.com/shutterstock/videos/1108575273/preview/stock-footage-aerial-shot-of-the-great-wall-of-china-at-sunrise.webm"
            )
        )

        adapter = VideoAdapter2(
            requireContext(),
            videos,
            object : VideoAdapter2.OnVideoPreparedListener {
                override fun onVideoPrepared(exoPlayerItem: ExoPlayerItem) {
                    exoPlayerItems.add(exoPlayerItem)
                }
            })

        binding.viewPager.adapter = adapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val previousIndex = exoPlayerItems.indexOfFirst { it.exoPlayer.isPlaying }
                if (previousIndex != -1) {
                    val player = exoPlayerItems[previousIndex].exoPlayer
                    player.pause()
                    player.playWhenReady = false
                }
                val newIndex = exoPlayerItems.indexOfFirst { it.position == position }
                if (newIndex != -1) {
                    val player = exoPlayerItems[newIndex].exoPlayer
                    player.playWhenReady = true
                    player.play()
                }
            }
        })
    }

    /*override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }*/

    override fun onPause() {
        super.onPause()

        val index = exoPlayerItems.indexOfFirst { it.position == binding.viewPager.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer
            player.pause()
            player.playWhenReady = false
        }
    }

    override fun onResume() {
        super.onResume()

        val index = exoPlayerItems.indexOfFirst { it.position == binding.viewPager.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer
            player.playWhenReady = true
            player.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (exoPlayerItems.isNotEmpty()) {
            for (item in exoPlayerItems) {
                val player = item.exoPlayer
                player.stop()
                player.clearMediaItems()
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
