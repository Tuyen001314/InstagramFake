package com.example.blogandchat.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
    private val listFavorite: MutableList<User> = ArrayList()
    private val listFavoriteSuggest: MutableList<User> = ArrayList()
    private lateinit var listenerRegistration: ListenerRegistration
    private lateinit var linearLayoutManager: LinearLayoutManager

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


        val id = FirebaseAuth.getInstance().uid
        FirebaseFirestore.getInstance().collection("users/$id/receive_request")
            .get().addOnSuccessListener { documents ->
                for (document in documents) {
                    // val firebaseModel =
                    val idUserReceive = document.id
                    FirebaseFirestore.getInstance().collection("users").document(idUserReceive)
                        .get().addOnSuccessListener { document ->
                            if (document != null) {
                                document.toObject(User::class.java)?.let { listFavorite.add(it) }
                                adapter.notifyDataSetChanged()
                            }
                        }
                        .addOnFailureListener {
                        }
                }
            }

        adapter = activity?.let { FavoriteAdapter(it, listFavorite) }!!
        listRequest.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        listRequest.layoutManager = linearLayoutManager
        listRequest.adapter = adapter
        adapter.notifyDataSetChanged()

        FirebaseFirestore.getInstance().collection("users").whereNotEqualTo("id", id)
            .get().addOnSuccessListener { documents ->
                for (document in documents) {
                    // val firebaseModel =
                    val idUserReceive = document.id
                    // listFavoriteSuggest.add(document.toObject(User::class.java))
                    // adapterSuggest.notifyDataSetChanged()
                    FirebaseFirestore.getInstance().collection("users/$id/following")
                        .document(idUserReceive)
                        .get().addOnSuccessListener { doc ->
                            if (!doc.exists()) {
                                listFavoriteSuggest.add(document.toObject(User::class.java))
                                adapterSuggest.notifyDataSetChanged()
                            }
                        }
                        .addOnFailureListener {
                        }
                }
            }

        adapterSuggest = activity?.let { SuggestFavoriteAdapter(it, listFavoriteSuggest) }!!
        listSuggest.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        listSuggest.layoutManager = linearLayoutManager
        listSuggest.adapter = adapterSuggest
        adapterSuggest.notifyDataSetChanged()
    }
}
