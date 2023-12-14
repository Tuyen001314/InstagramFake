package com.example.blogandchat.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogandchat.R
import com.example.blogandchat.adapter.MessageAdapter
import com.example.blogandchat.databinding.ActivitySpecificChatBinding
import com.example.blogandchat.model.Message
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class SpecificChat : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private var messageList: MutableList<Message> = ArrayList()
    private lateinit var messagesAdapter: MessageAdapter
    private lateinit var binding: ActivitySpecificChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_specific_chat)


        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        binding.recycleChat.layoutManager = linearLayoutManager

        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("hh:mm a")
        val mSenderUid = firebaseAuth.uid
        val mReceiverUid = intent.getStringExtra("receiveruid")
        val mReceiverName = intent.getStringExtra("name")
        val senderRoom = mSenderUid + mReceiverUid
        val receiverRoom = mReceiverUid + mSenderUid

        val databaseReference: DatabaseReference =
            firebaseDatabase.reference.child("chats").child(senderRoom)
                .child("messages")

        val postListener = object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                //val post = dataSnapshot.value
//                if (post != null) {
//                    messageList.add(post)
//                }
                messageList.clear()
                for (snapshot1 in snapshot.children) {
                    val message: Message? = snapshot1.getValue(Message::class.java)
                    if (message != null) {
                        messageList.add(message)
                        //Log.d("hhhhh", "Value is: $message");
                    }
                }

                messagesAdapter.notifyDataSetChanged()
                // ...
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        messagesAdapter = MessageAdapter(this@SpecificChat, messageList)

        databaseReference.addValueEventListener(postListener)

        binding.recycleChat.adapter = messagesAdapter

        binding.imageBtnChat.setOnClickListener(View.OnClickListener { finish() })

        binding.tvUsernameChat.text = mReceiverName

        val uri = intent.getStringExtra("imageuri")
        if (uri!!.isEmpty()) {
            Toast.makeText(applicationContext, "null is received", Toast.LENGTH_SHORT).show()
        } else {
            Glide.with(this).load(uri).into(binding.imageUserChat)
        }

        binding.imageBtnChat.setOnClickListener(View.OnClickListener {
            val enterdMessage = binding.edtChat.text.toString()
            if (enterdMessage.isEmpty()) {
                Toast.makeText(applicationContext, "Enter meassage first", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val date = Date()
                val currentTime = simpleDateFormat.format(calendar.time)
                val message = firebaseAuth.uid?.let { it1 ->
                    Message(currentTime = currentTime, message = enterdMessage,
                        senderId = it1, timeStamp = date.time)
                }

                FirebaseAuth.getInstance().uid?.let { it1 ->
                    FirebaseFirestore.getInstance().collection("users/${mReceiverUid}/message").document(
                        it1
                    ).update(mapOf(
                        "timeseen" to "1"
                    ))
                }

                val firebaseDatabase = FirebaseDatabase.getInstance()
                firebaseDatabase.reference.child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .push().setValue(message).addOnCompleteListener(OnCompleteListener<Void?> {
                        firebaseDatabase.reference
                            .child("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .push()
                            .setValue(message).addOnCompleteListener(OnCompleteListener<Void?> { })

//                        if (mReceiverUid != null) {
//                            PushNotification(NotificationData("abc", enterdMessage).toString(), mReceiverUid).also {
//                                sendNotification((it))
//                            }
//                        }
                    })

                binding.edtChat.text = null
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onStart() {
        super.onStart()
        messagesAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onStop() {
        super.onStop()
        messagesAdapter.notifyDataSetChanged()
    }

//    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
//        try {
//            val response = RetrofitInstance.api.postNotification(notification)
//            if (response.isSuccessful) {
//                //Toast.makeText(spe)
//            }
//        } catch (e: Exception) {
//
//        }
//    }
}