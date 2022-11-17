package com.example.blogandchat.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blogandchat.OnClickImage
import com.example.blogandchat.R
import com.example.blogandchat.activity.AddPostActivity
import com.example.blogandchat.activity.MainActivity
import com.example.blogandchat.adapter.PostAdapter
import com.example.blogandchat.home.HomeViewModel
import com.example.blogandchat.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.nav_header_layout.*

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

    private val viewModel by navGraphViewModels<HomeViewModel>(R.id.home_fragment)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        floatingActionButton.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    context,
                    AddPostActivity::class.java
                )
            )
        })

        if (FirebaseAuth.getInstance().currentUser != null) {
            view.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                            adapter.notifyDataSetChanged()
                        } else {
                            adapter.notifyDataSetChanged()
                        }
                    }
                    listenerRegistration.remove()
                })
        }

        adapter = activity?.let {
            PostAdapter(it, postList, object : OnClickImage {
                override fun click(id: String) {
                    //val fragmentManager: FragmentManager(
                    val transaction = fragmentManager?.beginTransaction()
                    if (transaction != null) {
                        transaction.replace(R.id.home_fragment, ImageFragment(id), null)
                        transaction.addToBackStack(null)
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        transaction.commit()
                    }

                    //Toast.makeText(context, "click image", Toast.LENGTH_SHORT).show()
                }
            })
        }!!
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

//        if(viewModel.listState != null)
//        {
//            recyclerView.layoutManager?.onRestoreInstanceState(viewModel.listState)
//            viewModel.listState = null
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //viewModel.listState = recyclerView.layoutManager?.onSaveInstanceState()
    }
}