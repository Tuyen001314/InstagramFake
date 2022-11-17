package com.example.blogandchat.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.blogandchat.R
import com.example.blogandchat.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_post.*
import java.util.*

class AddPostActivity : AppCompatActivity() {
    private val REQUEST_CODE = 100
    private var isPhotoSelected: Boolean = false
    var uri: Uri? = null
    private lateinit var viewModel: AddPostViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        viewModel = ViewModelProvider(this)[AddPostViewModel::class.java]

        viewModel.uiState.observe(this) { uiState ->
            uiState?.let {
                img_view_add.setImageURI(uiState.uri)
                progressBar_post.isVisible = uiState.addingPost
                if (uiState.addPostSuccess) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        progressBar_post.visibility = View.INVISIBLE;

        img_view_add.setOnClickListener {
            openGalleryImage()
        }

        btn_add_post.setOnClickListener {
            progressBar_post.visibility = View.VISIBLE;
//            sendDataToStorage()
            viewModel.addPost(edt_caption.text.toString().trim())
        }
    }

    private fun openGalleryImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {

//            img_view_add.setImageURI(data?.data) // handle chosen image
            viewModel.getImage(data?.data!!)
            isPhotoSelected = true

            if (data != null) {
                uri = data.data
            }

        }
    }
}
