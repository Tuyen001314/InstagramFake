package com.example.blogandchat.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.blogandchat.R
import com.example.blogandchat.databinding.ActivityAddPostBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID


// import kotlinx.android.synthetic.main.activity_add_post.*

class AddPostActivity : AppCompatActivity() {
    private val REQUEST_CODE = 100
    private val viewModel: AddPostViewModel by viewModels()

    private lateinit var mGetContent: ActivityResultLauncher<String>

    lateinit var binding: ActivityAddPostBinding

    var video: Uri? = null
    var uploadVideo: MaterialButton? = null
    lateinit var type: String

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null) {
                    uploadVideo?.isEnabled = true
                    video = result.data!!.data
                    binding.imgViewPost.visibility = View.VISIBLE
                    binding.imgUpload.visibility = View.GONE
                    binding.progressBarPost.isVisible = false
                    binding.imgViewPost.let { Glide.with(this).load(video).into(it) }
                }
            } else {
                Toast.makeText(this, "Please select a video", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        type = intent.getStringExtra("TYPE").toString()

        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_post)

        binding.edtCaption.isVisible = type != "VIDEO"

        viewModel.uiState.observe(this) { uiState ->
            uiState?.let {
                binding.imgViewPost.setImageURI(uiState.uri)
                binding.imgUpload.visibility = View.GONE
                binding.progressBarPost.isVisible = uiState.addingPost
                if (uiState.addPostSuccess) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else if (!uiState.addError) {
                    Toast.makeText(this, "Vui lòng thêm hình ảnh trước khi đăng", Toast.LENGTH_LONG)
                        .show()
                    binding.imgUpload.visibility = View.VISIBLE
                    binding.progressBarPost.visibility = View.INVISIBLE
                }
            }
        }

        binding.progressBarPost.visibility = View.INVISIBLE

        binding.imgViewAdd.setOnClickListener {
            if (type == "VIDEO") {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "video/*"
                activityResultLauncher.launch(intent)
            } else {
                mGetContent.launch("image/*")
            }
        }

        mGetContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            // Handle the returned Uri
            val intent = Intent(this, CropperActivity::class.java)
            intent.putExtra("DATA", uri.toString())
            startActivityForResult(intent, 101)
        }

        binding.btnAddPost.setOnClickListener {
            binding.progressBarPost.visibility = View.VISIBLE
//            sendDataToStorage()
            if (type == "VIDEO") {
                video?.let { it1 -> uploadVideo(it1) }
            } else {
                viewModel.addPost(binding.edtCaption.text.toString().trim())
            }
        }
    }

    private fun uploadVideo(uri: Uri) {
        val reference = FirebaseStorage.getInstance().reference.child("videos/${UUID.randomUUID()}")
        reference.putFile(uri).addOnSuccessListener {
            binding.progressBarPost.visibility = View.GONE
            Toast.makeText(this, "Video uploaded successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            binding.progressBarPost.visibility = View.GONE
            Toast.makeText(this, "Failed to upload video", Toast.LENGTH_SHORT).show()
        }.addOnProgressListener { snapshot ->
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == -1 && requestCode == 101) {
//            img_view_add.setImageURI(data?.data) // handle chosen image
            // viewModel.getImage(data?.data!!)

            val result = data?.getStringExtra("RESULT")
            if (result != null) {
                val resultUri = Uri.parse(result)
                viewModel.getImage(resultUri)
            }
        }
    }
}
