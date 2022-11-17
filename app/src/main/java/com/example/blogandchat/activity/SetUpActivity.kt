package com.example.blogandchat.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.blogandchat.R
import com.example.blogandchat.firebase.FireStore
import kotlinx.android.synthetic.main.activity_set_up.*

class SetUpActivity : AppCompatActivity() {
    private val REQUEST_CODE = 100

    private val viewModel: SetUpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_up)

        viewModel.uiState.observe(this) { uiState ->
            uiState?.let {
                Glide.with(this).load(uiState.uri).into(circleImageView)
                progressBar.isVisible = uiState.addingUser

                if (uiState.addError) {
                    Toast.makeText(
                        this,
                        "Vui lòng thêm đủ thông tin trước khi đăng",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (uiState.addUserSuccess) {
                    val user = uiState.user
                    if (user != null) {
                        FireStore().registerUser(this, user)
                    }
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        circleImageView.setOnClickListener {
            openGalleryForImage()
        }

        btn_save.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            viewModel.addUser(
                intent.getStringExtra("id").toString(),
                intent.getStringExtra("email").toString(),
                edt_name_enter.text.toString().trim()
            )
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

        }
    }

    fun userRegisteredSuccess() {
        Toast.makeText(this, "success", Toast.LENGTH_LONG).show()
        finish()
    }
}