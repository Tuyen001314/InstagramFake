package com.example.blogandchat.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.blogandchat.R
import com.example.blogandchat.model.User
import com.example.blogandchat.model.UserShare


class ShareFriendAdapter(val context: Context, private val list: MutableList<UserShare>, val onShareFriend: ShareFriendCallBack) : BaseAdapter() {
    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(p0: Int): Any {
        return list[p0]
    }

    override fun getItemId(p0: Int): Long {
            return  p0.toLong()
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val viewHolder: ViewHolder

        if (view == null) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
            viewHolder = ViewHolder()
            viewHolder.name = view.findViewById(R.id.tv_name)
            viewHolder.button = view.findViewById(R.id.bt_pick)
            viewHolder.image = view.findViewById(R.id.cardviewOfUserFollow)
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }

        val currentItem = getItem(position)

        viewHolder.name.text = (currentItem as UserShare).name
        viewHolder.button.isSelected = (currentItem).isPicked
        Glide.with(context).load(currentItem.image).into(viewHolder.image)

        viewHolder.button.setOnClickListener {
            onShareFriend.onClickCheckBox(viewHolder.button.isChecked, position)
        }




        return view!!
    }
    private class ViewHolder{
        lateinit var button: CheckBox
        lateinit var name: TextView
        lateinit var image : ImageView
    }
}
interface ShareFriendCallBack{
    fun onClickCheckBox(isCheck: Boolean, position: Int)
}