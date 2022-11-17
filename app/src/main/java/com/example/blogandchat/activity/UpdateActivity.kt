package com.example.blogandchat.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.blogandchat.R
import kotlinx.android.synthetic.main.activity_update.*

class UpdateActivity : AppCompatActivity() {
    private val REQUEST_CODE = 100
    private var isPhotoSelected = false

    private val viewModel: UpdateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)


        Glide.with(this).load(intent.getStringExtra("image")).into(circleImageView_change)
        edt_name_enter_change.hint = intent.getStringExtra("name")

        viewModel.uiState.observe(this) { uiState ->
            uiState?.let {
                Glide.with(this).load(uiState.uri).into(circleImageView_change)
                progressBar_change.isVisible = uiState.addingUser
                if (uiState.addUserSuccess) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    progressBar_change.visibility = View.GONE
                }
            }
        }

        circleImageView_change.setOnClickListener {
            openGalleryForImage()
        }

        btn_save_change.setOnClickListener {
            progressBar_change.visibility = View.VISIBLE

            if (!isPhotoSelected && edt_name_enter_change.length() == 0) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                progressBar_change.visibility = View.GONE
            } else if (edt_name_enter_change.length() != 0) {
                viewModel.changeName(
                    intent.getStringExtra("id").toString(),
                    edt_name_enter_change.text.toString().trim(),
                    intent.getStringExtra("image").toString()
                )
            } else {
                viewModel.changeProfile(
                    intent.getStringExtra("id").toString(),
                    edt_name_enter_change.text.toString().trim()
                )
            }
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

            data?.data?.let { viewModel.getImage(it) }
            isPhotoSelected = true
        }
    }
}