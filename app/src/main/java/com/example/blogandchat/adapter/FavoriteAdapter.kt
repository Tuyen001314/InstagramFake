package com.example.blogandchat.adapter

import android.content.Context
import android.content.Intent
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
import com.example.blogandchat.model.User
import de.hdodenhof.circleimageview.CircleImageView

class FavoriteAdapter(
    val context: Context,
    val listUser: MutableList<User>,
    val favoriteCallback: FavoriteCallback
) :
    RecyclerView.Adapter<FavoriteAdapter.ViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val user: User = listUser[position]

        Glide.with(context).load(user.image).into(holder.avatar)
        holder.nameOfUser.text = user.name

        
        holder.accept.setOnClickListener {
            favoriteCallback.accept(user)
        }

        holder.nameOfUser.setOnClickListener {
            val intent = Intent(context, OtherUserProfile::class.java)
            intent.putExtra("id", user.id)
            context.startActivity(intent)
        }

        holder.erase.setOnClickListener {
            favoriteCallback.delete(user)
        }

    }


    override fun getItemCount(): Int {
        return listUser.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameOfUser: TextView = itemView.findViewById(R.id.nameOfUser1)
        val avatar: CircleImageView = itemView.findViewById(R.id.cardviewOfUser1)
        val accept: TextView = itemView.findViewById(R.id.buttonAccept)
        val erase: ImageView = itemView.findViewById(R.id.eraseRequest)
    }

}