package com.example.blogandchat.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogandchat.R
import com.example.blogandchat.activity.SpecificChat
import com.example.blogandchat.model.Message
import com.example.blogandchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.ServerTimestamp
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapterUser() : RecyclerView.Adapter<ChatAdapterUser.NoteViewHolder?>() {
    private lateinit var listUser: MutableList<User>
    private lateinit var context: Context
    private lateinit var message: Message
    private lateinit var time: ServerTimestamp

    constructor(context: Context, listUser: MutableList<User>) : this() {
        this.listUser = listUser
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_chat_layout_user, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {

        val firebaseModel: User = listUser[position]

        Glide.with(context).load(firebaseModel.image).into(holder.avatar)
        holder.nameOfUser.text = firebaseModel.name
        if (firebaseModel.status == "online") {
            holder.statusOfUser.visibility = View.VISIBLE
        } else {
            holder.statusOfUser.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener(View.OnClickListener { v ->

            val intent = Intent(v.context, SpecificChat::class.java)
            intent.putExtra("name", firebaseModel.name)
            intent.putExtra("receiveruid", firebaseModel.id)
            intent.putExtra("imageuri", firebaseModel.image)
            v.context.startActivity(intent)
        })


        val senderRoom = FirebaseAuth.getInstance().uid + firebaseModel.id

        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("chats").child(senderRoom)
                .child("messages")

        val postListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {

                //messageList.clear()
                for (snapshot1 in snapshot.children) {
                    message = snapshot1.getValue(Message::class.java)!!
                    if (message != null) {
                        //messageList.add(message)
                        //Log.d("hhhhh", "Value is: $message");
                    }
                }

                if (message.senderId == FirebaseAuth.getInstance().uid) {
                    holder.lastMessage.text = "Bạn: " + message.message

                } else {
                    holder.lastMessage.text = message.message
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        databaseReference.addValueEventListener(postListener)

    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filterlist: MutableList<User>) {
        // below line is to add our filtered
        // list in our course array list.
        listUser = filterlist
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return listUser.size
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameOfUser: TextView = itemView.findViewById(R.id.nameOfUser1)
        val statusOfUser: CircleImageView = itemView.findViewById(R.id.viewStatus)
        val avatar: CircleImageView = itemView.findViewById(R.id.cardviewOfUser1)
        val lastMessage: TextView = itemView.findViewById(R.id.statusOfUser)

    }

}