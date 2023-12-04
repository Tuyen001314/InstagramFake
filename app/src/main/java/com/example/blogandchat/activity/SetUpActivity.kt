package com.example.blogandchat.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.blogandchat.R
import com.example.blogandchat.databinding.ActivitySetUpBinding
import com.example.blogandchat.firebase.FireStore

class SetUpActivity : AppCompatActivity() {
    private val REQUEST_CODE = 100

    private val viewModel: SetUpViewModel by viewModels()
    private lateinit var binding: ActivitySetUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding = DataBindingUtil.setContentView(this,R.layout.activity_set_up)

        viewModel.uiState.observe(this) { uiState ->
            uiState?.let {
                Glide.with(this).load(uiState.uri).into(binding.circleImageView)
                binding.progressBar.isVisible = uiState.addingUser

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

        binding.circleImageView.setOnClickListener {
            openGalleryForImage()
        }

        binding.btnSave.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE

            viewModel.addUser(
                intent.getStringExtra("id").toString(),
                intent.getStringExtra("email").toString(),
                binding.edtNameEnter.text.toString().trim()
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