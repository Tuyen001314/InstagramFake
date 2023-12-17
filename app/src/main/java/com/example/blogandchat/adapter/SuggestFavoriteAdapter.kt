package com.example.blogandchat.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogandchat.FavoriteCallback
import com.example.blogandchat.R
import com.example.blogandchat.activity.OtherUserProfile
import com.example.blogandchat.activity.SpecificChat
import com.example.blogandchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.util.HashMap

class SuggestFavoriteAdapter(
    val context: Context,
    val listUser: MutableList<User>,
    val favoriteCallback: FavoriteCallback
) :
    RecyclerView.Adapter<SuggestFavoriteAdapter.ViewHolder?>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_follow, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val user: User = listUser[position]

        Glide.with(context).load(user.image).into(holder.avatar)
        holder.nameOfUser.text = user.name


        holder.follow.setOnClickListener {
            favoriteCallback.accept(user)
            listUser.remove(user)

            holder.itemView.visibility = View.INVISIBLE
        }

        holder.nameOfUser.setOnClickListener {
            val intent = Intent(context, OtherUserProfile::class.java)
            intent.putExtra("id", user.id)
            context.startActivity(intent)
        }

        holder.erase.setOnClickListener {
            favoriteCallback.delete(user)
            listUser.remove(user)
            holder.itemView.visibility = View.INVISIBLE
        }

    }


    override fun getItemCount(): Int {
        return listUser.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameOfUser: TextView = itemView.findViewById(R.id.nameOfUserFollow)
        val avatar: CircleImageView = itemView.findViewById(R.id.cardviewOfUserFollow)
        val follow: Button = itemView.findViewById(R.id.buttonFollow)
        val erase: ImageView = itemView.findViewById(R.id.eraseFollow)
    }

}