package com.example.blogandchat.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
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
import com.example.blogandchat.adapter.SearchUserAdapter
import com.example.blogandchat.databinding.FragmentChatBinding
import com.example.blogandchat.model.User
import com.example.blogandchat.model.UserMessageModel
import java.util.Locale


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
    private var friendList = mutableListOf<User>()
    lateinit var searchAdapter: SearchUserAdapter
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
        val adapter = ChatAdapter(requireContext(), list)
        val linearLayoutManagerHorizontal = LinearLayoutManager(requireContext())
        linearLayoutManagerHorizontal.orientation = RecyclerView.HORIZONTAL
        binding.recyclerViewChat.layoutManager = linearLayoutManagerHorizontal
        binding.recyclerViewChat.adapter = adapter

        val adapterUser = ChatAdapterUser(list1)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        binding.recyclerViewChat1.layoutManager = linearLayoutManager
        binding.recyclerViewChat1.adapter = adapterUser

        searchAdapter = SearchUserAdapter()
        binding.rcSearch.layoutManager = LinearLayoutManager(requireContext())
        binding.rcSearch.adapter = searchAdapter

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
        viewModel.listFriend.observe(viewLifecycleOwner) {
            friendList = it
            searchAdapter.submitList(friendList)
        }



        binding.searchChat.setOnCloseListener(object : SearchView.OnCloseListener{
            override fun onClose(): Boolean {
                searchAdapter.submitList(friendList)
                return false
            }

        })
        binding.searchChat.setOnSearchClickListener {
            binding.rcSearch.visibility = View.VISIBLE
        }
        binding.searchChat.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(msg: String): Boolean {
                filter(msg)
                return false
            }
        })

        binding.searchChat.setOnQueryTextFocusChangeListener(OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                binding.rcSearch.visibility = View.GONE
            }
            else{
                binding.rcSearch.visibility = View.VISIBLE

            }
        })


    }



    private fun filter(text: String) {
        val filteredlist: MutableList<User> = ArrayList()
        for (item in friendList) {
            if (item.name.lowercase(Locale.getDefault())
                    .contains(text.lowercase(Locale.getDefault()))
            ) {
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
            Toast.makeText(requireContext(), "No Data Found..", Toast.LENGTH_SHORT).show()
        } else {
            searchAdapter.submitList(filteredlist)
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
