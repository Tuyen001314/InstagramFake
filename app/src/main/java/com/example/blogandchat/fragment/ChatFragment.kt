package com.example.blogandchat.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blogandchat.R
import com.example.blogandchat.adapter.ChatAdapter
import com.example.blogandchat.adapter.ChatAdapterUser
import com.example.blogandchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_chat.view.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatFragment : Fragment() {

    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mImageViewOfUser: ImageView
    private lateinit var firebaseModel: User
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerView1: RecyclerView
    private var list: MutableList<User> = ArrayList()
    private lateinit var adapter: ChatAdapter
    private lateinit var adapterUser: ChatAdapterUser

    //private lateinit var listenerRegistration: ListenerRegistration
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.recyclerViewChat
        recyclerView1 = view.recyclerViewChat1

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()


        val id = firebaseAuth.uid;
        firebaseFirestore.collection("users/$id/message")
            .get().addOnSuccessListener { documents ->
                for (document in documents) {
                    // val firebaseModel =
                    val idUserReceive = document.id
                    FirebaseFirestore.getInstance().collection("users").document(idUserReceive)
                        .get().addOnSuccessListener { document ->
                            if (document != null) {
                                document.toObject(User::class.java)?.let { list.add(it) }
                                adapter.notifyDataSetChanged()
                            }
                        }
                        .addOnFailureListener {
                        }
                }
            }


        adapter = activity?.let { ChatAdapter(it, list) }!!
        recyclerView.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.HORIZONTAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()

        adapterUser = activity?.let { ChatAdapterUser(it, list) }!!
        recyclerView1.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        recyclerView1.layoutManager = linearLayoutManager
        recyclerView1.adapter = adapterUser
        adapterUser.notifyDataSetChanged()

        searchChat.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(msg: String): Boolean {
                // inside on query text change method we are
                // calling a method to filter our recycler view.
                filter(msg)
                return false
            }
        })

    }

    private fun filter(text: String) {
        // creating a new array list to filter our data.
        val filteredlist: MutableList<User> = ArrayList()

        // running a for loop to compare elements.
        for (item in list) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.name.toLowerCase().contains(text.toLowerCase())) {
                // if the item is matched we are
                // adding it to our filtered list.
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            Toast.makeText(context, "No Data Found..", Toast.LENGTH_SHORT).show()
            adapterUser.filterList(filteredlist)
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            adapterUser.filterList(filteredlist)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        list.clear()
    }
}