package com.example.blogandchat.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blogandchat.R
import com.example.blogandchat.adapter.ChatAdapter
import com.example.blogandchat.adapter.ChatAdapterUser
import com.example.blogandchat.databinding.FragmentChatBinding
import com.example.blogandchat.model.Message
import com.example.blogandchat.model.User
import com.example.blogandchat.model.UserMessageModel
import com.example.blogandchat.utils.AppKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

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
    private val viewModel: ChatListViewModel by viewModels()
    private var list: MutableList<User> = ArrayList()
    private var list1 = mutableListOf<UserMessageModel>()
    private lateinit var binding: FragmentChatBinding

    // private lateinit var listenerRegistration: ListenerRegistration
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchListUser()
        val searchChat: SearchView = view.findViewById(R.id.searchChat)


        val adapter = ChatAdapter(requireContext(), list)
        binding.recyclerViewChat.setHasFixedSize(true)
        val linearLayoutManagerHorizontal = LinearLayoutManager(requireContext())
        linearLayoutManagerHorizontal.orientation = RecyclerView.HORIZONTAL
        binding.recyclerViewChat.layoutManager = linearLayoutManagerHorizontal
        binding.recyclerViewChat.adapter = adapter

       val  adapterUser = ChatAdapterUser(list1)
        binding.recyclerViewChat1.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        binding.recyclerViewChat1.layoutManager = linearLayoutManager
        binding.recyclerViewChat1.adapter = adapterUser

        viewModel.pairLiveData.observe(viewLifecycleOwner) {
            list.clear()
            list1.clear()
            it.forEach { userMessage ->
                list.add(userMessage.user)
            }
            list1.addAll(it)
            adapter.notifyDataSetChanged()
            adapterUser.notifyDataSetChanged()
        }



        searchChat.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
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
            Toast.makeText(requireContext(), "No Data Found..", Toast.LENGTH_SHORT).show()
            //adapterUser.filterList(filteredlist)
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
          //  adapterUser.filterList(filteredlist)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.pairLiveData.removeObservers(viewLifecycleOwner)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
