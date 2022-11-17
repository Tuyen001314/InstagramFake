package com.example.blogandchat.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.blogandchat.R
import com.example.blogandchat.firebase.FireStore
import com.example.blogandchat.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_set_up.*
import kotlinx.android.synthetic.main.nav_header_layout.*
import java.util.*

class SetUpActivity : AppCompatActivity() {
    private val REQUEST_CODE = 100
    private var isPhotoSelected: Boolean = false
    var uri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_up)

        circleImageView.setOnClickListener {
            openGalleryForImage()
        }

        //val imageRef = storageReference.child("Profile_pics").child(id + ".jpg")
        btn_save.setOnClickListener {
            sendDataToStorage()
        }

    }

    private fun sendDataToStorage() {

        val email: String = intent.getStringExtra("email").toString()
        val id: String = intent.getStringExtra("id").toString()
        val name = edt_name_enter.text.toString().trim()

        if (uri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        if (name.isNotEmpty() && isPhotoSelected) {
            ref.putFile(uri!!).addOnSuccessListener {

                ref.downloadUrl.addOnSuccessListener {
                    progressBar.visibility = View.VISIBLE
                    val user = User(id = id, email = email, name = name, image = it.toString(), status = "online")
                    FireStore().registerUser(this, user)
                    progressBar.visibility = View.INVISIBLE

                    val myRef = FirebaseDatabase.getInstance().getReference("/users/$id")
                    myRef.setValue(user).addOnSuccessListener { }

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        else {
            Toast.makeText(this, "vui long dien du thong tin", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            circleImageView.setImageURI(data?.data) // handle chosen image
            isPhotoSelected = true

            if (data != null) {
                uri = data.data
            }

        }
    }

    fun userRegisteredSuccess() {
        Toast.makeText(this, "success", Toast.LENGTH_LONG).show()
        //FirebaseAuth.getInstance().signOut()
        finish()
    }
}