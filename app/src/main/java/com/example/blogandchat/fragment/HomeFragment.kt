package com.example.blogandchat.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blogandchat.PostDetailListener
import com.example.blogandchat.R
import com.example.blogandchat.activity.AddPostActivity
import com.example.blogandchat.activity.CommentActivity
import com.example.blogandchat.adapter.PostAdapter
import com.example.blogandchat.databinding.FragmentHomeBinding
import com.example.blogandchat.dialog.CommentDialogFragment
import com.example.blogandchat.dialog.ShareDialogFragment
import com.example.blogandchat.home.HomeViewModel
import com.example.blogandchat.model.Post
import com.example.blogandchat.model.PostDetailModel
import com.example.blogandchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.newCoroutineContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var adapter: PostAdapter
    private val postList: MutableList<Post> = ArrayList()
    private lateinit var listenerRegistration: ListenerRegistration
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    var urlShare = ""


    //private val viewModel by navGraphViewModels<HomeViewModel>(R.id.mobile_navigation)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        binding.floatingActionButton.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    context,
                    AddPostActivity::class.java
                )
            )
        })

        if (FirebaseAuth.getInstance().currentUser != null) {
            binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val isBottom = !recyclerView.canScrollVertically(1)
                    if (isBottom) {
                        Toast.makeText(context, "Reached Bottom", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            })
            val query: Query = FirebaseFirestore.getInstance().collection("posts")
                .orderBy("time", Query.Direction.DESCENDING)
            listenerRegistration = query.addSnapshotListener(
                EventListener<QuerySnapshot?> { value, _ ->
                    for (doc in value!!.documentChanges) {
                        if (doc.type == DocumentChange.Type.ADDED) {
                            val postId = doc.document.id
                            val post: Post = doc.document.toObject(Post::class.java).withId(postId)
                            postList.add(post)
                        }
                    }
                    viewModel.transferData(postList)
                    listenerRegistration.remove()
                })
        }

        adapter =
            PostAdapter(context = requireContext(), onClickImage = object : PostDetailListener {
                override fun click(id: String) {
                    val transaction = fragmentManager?.beginTransaction()
                    if (transaction != null) {
                        transaction.replace(R.id.home_fragment, ImageFragment(id), null)
                        transaction.addToBackStack(null)
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        transaction.commit()
                    }
                }

                override fun like(id: String) {
                    viewModel.likeImage(id)
                }

                override fun comment(postId: String, id: String) {
                    println(postId)
                    CommentDialogFragment.show(childFragmentManager, postId = postId, uId = id)

                }

                override fun share(url: String) {
                    urlShare = url
                    viewModel.getFriends()
                }

            }, firestore = firestore, auth = auth)

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter


        viewModel.postDetails.observe(viewLifecycleOwner)
        {
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        }
        viewModel.listFriend.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                val dialog = ShareDialogFragment.newInstance(it as ArrayList<User>, onSend = {
                    viewModel.sendToFriends(it,urlShare)
                })
                dialog.show(childFragmentManager, "SHARE_DIALOG_FRAGMENT")
            }

        }
//        if (viewModel.listState != null){
//            recyclerView.layoutManager?.onRestoreInstanceState(viewModel.listState)
//            viewModel.listState = null
//        }
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        viewModel.listState = recyclerView.layoutManager?.onSaveInstanceState()
//    }

}