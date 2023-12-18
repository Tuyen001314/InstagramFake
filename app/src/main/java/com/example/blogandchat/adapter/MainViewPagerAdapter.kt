package com.example.blogandchat.adapter

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.blogandchat.fragment.ChatFragment
import com.example.blogandchat.fragment.FavoriteFragment
import com.example.blogandchat.fragment.HomeFragment
import com.example.blogandchat.fragment.SearchFragment

class MainViewPagerAdapter(context: FragmentActivity) :
    FragmentStateAdapter(context) {
    private val fragments = ArrayList<Fragment>()

    init {
        fragments.add(HomeFragment())
        fragments.add(FavoriteFragment())
        fragments.add(SearchFragment())
        fragments.add(ChatFragment())
    }

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position]

    fun getFragment(position: Int) = fragments[position]

}