package com.example.blogandchat.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.example.blogandchat.R
import com.example.blogandchat.databinding.ActivityMainBinding
import com.example.blogandchat.fragment.*
import com.example.blogandchat.model.User
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    private lateinit var binding: ActivityMainBinding
    private lateinit var user: User
    // private lateinit var bottomNav: BottomNavigationView

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val navUserName = binding.navView.findViewById<TextView>(R.id.nameUser)
        val navImgUser = binding.navView.findViewById<ImageView>(R.id.imageUser)

        val docRef = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().uid.toString())

        docRef.update("status", "online").addOnSuccessListener { }

        docRef.get().addOnSuccessListener { documentSnapshot ->
            user = documentSnapshot.toObject<User>()!!
            /*navUserName.text = user.name
            Glide.with(this).load(user.image).into(navImgUser)*/
        }

        loadFragment(HomeFragment())

//        binding.bottomNav.setOnItemSelectedListener {it ->
//            when (it.itemId) {
//                R.id.home -> {
//                    val homeFragment = HomeFragment()
//                    loadFragment(homeFragment)
//                }
//                R.id.message -> {
//                    val chatFragment = ChatFragment()
//                    loadFragment(chatFragment)
//                }
//                R.id.search -> {
//                    val searchFragment = SearchFragment()
//                    loadFragment(searchFragment)
//                }
//            }
//            true
//        }

//        val appBarConfiguration = AppBarConfiguration(setOf(
//            androidx.appcompat.R.id.home, R.id.navigation_dashboard, R.id.navigation_notifications))
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)

//        val appBarConfiguration = AppBarConfiguration(setOf(
//            R.id.navigation_home, R.id.navigation_favorite, R.id.navigation_search, R.id.navigation_chat ))
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        bottomNav.setupWithNavController(navController)

        binding.bottomNav.setOnItemSelectedListener { id ->
            when (id) {
                R.id.home -> {
                    val homeFragment = HomeFragment()
                    loadFragment(homeFragment)
                }

                R.id.message -> {
                    val chatFragment = ChatFragment()
                    loadFragment(chatFragment)
                }

                R.id.search -> {
                    val searchFragment = SearchFragment()
                    loadFragment(searchFragment)
                }

                R.id.favorites -> {
                    val favoriteFragment = FavoriteFragment()
                    loadFragment(favoriteFragment)
                }
            }
            true
        }

        actionBarDrawerToggle =
            ActionBarDrawerToggle(this, binding.drawerLayout, R.string.nav_open, R.string.nav_close)

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        // to make the Navigation drawer icon always appear on the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        // transaction.hide(fragment)
        transaction.replace(R.id.container, fragment)
        // transaction.addToBackStack(null)
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_account -> {
                startActivity(Intent(this, SettingActivity::class.java))
            }

            R.id.nav_logout -> {
                val docRef = FirebaseFirestore.getInstance().collection("users")
                    .document(FirebaseAuth.getInstance().uid.toString())

                docRef.update("status", "offline").addOnSuccessListener { }

                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, SignInActivity::class.java))
            }
        }
        return true
    }

    override fun onStop() {
        val docRef = FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().uid.toString())
        docRef.update("status", "offline").addOnSuccessListener { }

        super.onStop()
    }
}
