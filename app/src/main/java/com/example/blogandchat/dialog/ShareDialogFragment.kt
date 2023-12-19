package com.example.blogandchat.dialog

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.example.blogandchat.R
import com.example.blogandchat.adapter.ShareFriendAdapter
import com.example.blogandchat.model.User

class ShareDialogFragment() : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dialog_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = view.findViewById<ListView>(R.id.lv_friends)
        val friendList: ArrayList<User> = this.arguments.getParcelableArrayList(FRIENDS)
        val adapter = ShareFriendAdapter(view.context, friendList)
        listView.adapter = adapter
    }

    companion object {
        private const val FRIENDS = "FRIENDS"

        fun newInstance(
            list :ArrayList<User>
        ) = ShareDialogFragment().apply {
            arguments = bundleOf(
                FRIENDS to list
            )
        }
    }
}