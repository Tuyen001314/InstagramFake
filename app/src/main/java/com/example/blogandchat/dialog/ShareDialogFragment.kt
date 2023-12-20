package com.example.blogandchat.dialog

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.example.blogandchat.R
import com.example.blogandchat.adapter.ShareFriendAdapter
import com.example.blogandchat.model.User
import com.example.blogandchat.model.UserShare

class ShareDialogFragment(val onSend: (List<String>) -> Unit) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_dialog_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = view.findViewById<ListView>(R.id.lv_friends)
        val tvSend = view.findViewById<TextView>(R.id.tv_send)
        val tvCancel = view.findViewById<TextView>(R.id.tv_cancel)
        val friendList =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this.arguments?.getParcelableArrayList(FRIENDS, User::class.java) as ArrayList<User>
            } else {
                this.arguments?.getSerializable(FRIENDS) as ArrayList<User>
            }.map { user ->
                UserShare(
                    id = user.id,
                    name = user.name,
                    isPicked = false,
                    image = user.image,
                    email = user.email
                )
            }
        val adapter = ShareFriendAdapter(view.context, friendList)
        listView.adapter = adapter
        tvCancel.setOnClickListener { this.dismiss() }
        tvSend.setOnClickListener {
            val list = friendList.filter { it.isPicked }
            onSend.invoke(list.map { it.id })
            dismiss()
        }
    }

    override fun onStart() {
        val dialog: Dialog? = dialog
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog?.window?.setLayout(width, height)
        super.onStart()

    }

    companion object {
        private const val FRIENDS = "FRIENDS"

        fun newInstance(
            list: ArrayList<User>,
            onSend: (List<String>) -> Unit
        ) = ShareDialogFragment(onSend).apply {
            arguments = bundleOf(
                FRIENDS to list
            )
        }
    }
}