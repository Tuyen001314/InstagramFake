package com.example.blogandchat.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.blogandchat.R
import kotlinx.android.synthetic.main.activity_add_post.*

class AddPostActivity : AppCompatActivity() {
    private val REQUEST_CODE = 100
    private lateinit var viewModel: AddPostViewModel

    private lateinit var  mGetContent: ActivityResultLauncher<String>


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
                } else if (!uiState.addError) {
                    Toast.makeText(this, "Vui lòng thêm hình ảnh trước khi đăng", Toast.LENGTH_LONG)
                        .show()
                    progressBar_post.visibility = View.INVISIBLE;
                }
            }
        }

        progressBar_post.visibility = View.INVISIBLE;

        img_view_add.setOnClickListener {
            mGetContent.launch("image/*")
        }

        mGetContent = registerForActivityResult(ActivityResultContracts.GetContent())  { uri->
            // Handle the returned Uri
            val intent = Intent(this, CropperActivity::class.java)
            intent.putExtra("DATA", uri.toString())
            startActivityForResult(intent, 101)

        }

        btn_add_post.setOnClickListener {
            progressBar_post.visibility = View.VISIBLE;
//            sendDataToStorage()
            viewModel.addPost(edt_caption.text.toString().trim())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == -1 && requestCode == 101) {

//            img_view_add.setImageURI(data?.data) // handle chosen image
            //viewModel.getImage(data?.data!!)

            val result = data?.getStringExtra("RESULT")
            if(result != null) {
                val resultUri = Uri.parse(result)
                viewModel.getImage(resultUri)
            }
        }
    }
}
