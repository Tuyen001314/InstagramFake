package com.example.blogandchat.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blogandchat.R
import com.example.blogandchat.adapter.FavoriteAdapter
import com.example.blogandchat.adapter.SuggestFavoriteAdapter
import com.example.blogandchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FavoriteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FavoriteFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private lateinit var adapter: FavoriteAdapter
    private lateinit var adapterSuggest: SuggestFavoriteAdapter
    private val listFavorite = mutableListOf<User>()
    private val listFavoriteSuggest = mutableListOf<User>()
    private lateinit var listenerRegistration: ListenerRegistration
    private lateinit var linearLayoutManager: LinearLayoutManager
    private val viewModel: FavoriteViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listSuggest = view.findViewById<RecyclerView>(R.id.listSuggest)
        val listRequest = view.findViewById<RecyclerView>(R.id.listRequest)
        adapter = activity?.let { FavoriteAdapter(it, listFavorite) }!!
        listRequest.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.HORIZONTAL
        listRequest.layoutManager = linearLayoutManager
        listRequest.adapter = adapter

        adapterSuggest = activity?.let { SuggestFavoriteAdapter(it, listFavoriteSuggest) }!!
        listSuggest.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        listSuggest.layoutManager = linearLayoutManager
        listSuggest.adapter = adapterSuggest

        viewModel.listFavorite.observe(viewLifecycleOwner){
            println(it.toString())
            listFavorite.clear()
            listFavorite.addAll(it)
            adapter.notifyDataSetChanged()
        }

        viewModel.listSuggest.observe(viewLifecycleOwner){
            println(it.toString())
            listFavoriteSuggest.clear()
            listFavoriteSuggest.addAll(it)
            adapterSuggest.notifyDataSetChanged()
        }

    }
}
