package com.example.blogandchat.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.blogandchat.R
import com.example.blogandchat.databinding.ActivityAddPostBinding

// import kotlinx.android.synthetic.main.activity_add_post.*

class AddPostActivity : AppCompatActivity() {
    private val REQUEST_CODE = 100
    private val viewModel: AddPostViewModel by viewModels()

    private lateinit var mGetContent: ActivityResultLauncher<String>

    lateinit var binding: ActivityAddPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_post)
        viewModel.uiState.observe(this) { uiState ->
            uiState?.let {
                binding.imgViewAdd.setImageURI(uiState.uri)
                binding.progressBarPost.isVisible = uiState.addingPost
                if (uiState.addPostSuccess) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else if (!uiState.addError) {
                    Toast.makeText(this, "Vui lòng thêm hình ảnh trước khi đăng", Toast.LENGTH_LONG)
                        .show()
                    binding.progressBarPost.visibility = View.INVISIBLE
                }
            }
        }

        binding.progressBarPost.visibility = View.INVISIBLE

        binding.imgViewAdd.setOnClickListener {
            mGetContent.launch("image/*")
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
            viewModel.addPost(binding.edtCaption.text.toString().trim())
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
