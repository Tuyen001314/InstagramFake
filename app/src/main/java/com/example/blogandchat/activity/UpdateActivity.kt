package com.example.blogandchat.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.blogandchat.R
import com.example.blogandchat.databinding.ActivityUpdateBinding

class UpdateActivity : AppCompatActivity() {
    private val REQUEST_CODE = 100
    private var isPhotoSelected = false

    private val viewModel: UpdateViewModel by viewModels()
    private lateinit var binding: ActivityUpdateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_update)
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        Glide.with(this).load(intent.getStringExtra("image")).into(binding.circleImageViewChange)
        binding.edtNameEnterChange.hint = intent.getStringExtra("name")

        viewModel.uiState.observe(this) { uiState ->
            uiState?.let {
                Glide.with(this).load(uiState.uri).into(binding.circleImageViewChange)
                binding.progressBarChange.isVisible = uiState.addingUser
                if (uiState.addUserSuccess) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    binding.progressBarChange.visibility = View.GONE
                }
            }
        }

        binding.circleImageViewChange.setOnClickListener {
            openGalleryForImage()
        }

        binding.btnSaveChange.setOnClickListener {
            binding.progressBarChange.visibility = View.VISIBLE

            if (!isPhotoSelected && binding.edtNameEnterChange.length() == 0) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                binding.progressBarChange.visibility = View.GONE
            } else if (binding.edtNameEnterChange.length() != 0) {
                viewModel.changeName(
                    intent.getStringExtra("id").toString(),
                    binding.edtNameEnterChange.text.toString().trim(),
                    intent.getStringExtra("image").toString()
                )
            } else {
                viewModel.changeProfile(
                    intent.getStringExtra("id").toString(),
                    binding.edtNameEnterChange.text.toString().trim()
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