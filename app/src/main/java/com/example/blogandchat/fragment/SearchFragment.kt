package com.example.blogandchat.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.blogandchat.R
import com.example.blogandchat.activity.AddPostActivity
import com.example.blogandchat.adapter.VideoAdapter2
import com.example.blogandchat.databinding.FragmentSearchBinding
import com.example.blogandchat.model.ExoPlayerItem
import com.example.blogandchat.model.Video
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.ref.WeakReference

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
    private var isDataFetched = false

    private val videos = ArrayList<Video>()
    private val exoPlayerItems = ArrayList<ExoPlayerItem>()

    private lateinit var binding: FragmentSearchBinding

    val viewPagerListener = object : ViewPager2.OnPageChangeCallback() {
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
    }

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*val video1 = Video("https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4")
        FirebaseDatabase.getInstance().reference.child("videos").push().setValue(video1).addOnCompleteListener {
            Log.d("buituyen", "success")
        }
        val video2 = Video("https://firebasestorage.googleapis.com/v0/b/blogandchat-254f3.appspot.com/o/videos%2FStories%20%E2%80%A2%20Instagram%20-%20Google%20Chrome%202023-12-17%2015-50-20.mp4?alt=media&token=f5bf605b-c365-4b2b-8766-96441f43a9bb")
        FirebaseDatabase.getInstance().reference.child("videos").push().setValue(video2).addOnCompleteListener {
            Log.d("buituyen", "success")
        }

        val options: FirebaseRecyclerOptions<Video> =
            FirebaseRecyclerOptions.Builder<Video>()
                .setQuery(
                    FirebaseDatabase.getInstance().reference.child("videos"),
                    Video::class.java,
                )
                .build()

        Log.d("buituyen", options.snapshots.size.toString())*/

        /*videos.add(
            Video(
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            )
        )

        videos.add(
            Video(
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
            )
        )

        videos.add(
            Video(
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
            )
        )*/

        binding.floatingActionButton.setOnClickListener(View.OnClickListener {
            val intent = Intent(
                context,
                AddPostActivity::class.java
            )
            intent.putExtra("TYPE", "VIDEO")
            isDataFetched = false
            startActivity(
                intent
            )
        })


        val videoRef =
            FirebaseStorage.getInstance().reference.child("videos") // Change "videos/video1" to the path where your video data is stored

        CoroutineScope(Dispatchers.Main).launch {
            if (!isDataFetched) {

                val def = async {
                    videoRef.listAll()
                        .addOnSuccessListener { listResult ->
                            // The video list has been successfully fetched
                            for (video in listResult.items) {
                                // Access each video item
                                val videoName = video.name
                                val videoUrlTask =
                                    video.downloadUrl // Get the download URL of the video

                                videoUrlTask.addOnSuccessListener { uri ->
                                    videos.add(Video(uri.toString()))
                                    // Use the video URL to display the video or perform any other necessary operations
                                }.addOnFailureListener { exception ->
                                    // Handle any errors that occurred while getting the download URL
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            // Handle any errors that occurred during the fetch
                        }
                }
                def.await()
                delay(5000)
            }

            adapter =
                VideoAdapter2(
                    requireContext(),
                    videos,
                    object : VideoAdapter2.OnVideoPreparedListener {
                        override fun onVideoPrepared(exoPlayerItem: ExoPlayerItem) {
                            exoPlayerItems.add(exoPlayerItem)
                        }
                    })

            binding.viewPager.adapter = adapter
            binding.viewPager.registerOnPageChangeCallback(viewPagerListener)
        }

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
        val index = exoPlayerItems.indexOfFirst { it.position == binding.viewPager.currentItem }
        if (index != -1) {
            val player = exoPlayerItems[index].exoPlayer
            player.pause()
            player.playWhenReady = false
        }
        super.onPause()
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
        binding.viewPager.unregisterOnPageChangeCallback(viewPagerListener)
        if (exoPlayerItems.isNotEmpty()) {
            for (item in exoPlayerItems) {
                val player = item.exoPlayer
                player.stop()
                player.clearMediaItems()
            }
        }
        exoPlayerItems.clear()
        super.onDestroy()
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
