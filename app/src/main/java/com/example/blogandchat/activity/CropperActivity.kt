package com.example.blogandchat.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.blogandchat.R
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.*

class CropperActivity : AppCompatActivity() {

    private lateinit var result: String
    private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cropper)

        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        readIntent()

        val options = UCrop.Options()

        val destUri = StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString()
        UCrop.of(uri, Uri.fromFile(File(cacheDir, destUri)))
            .withOptions(options)
            .withAspectRatio(0F, 0F)
            .useSourceImageAspectRatio()
            .withMaxResultSize(2000, 2000)
            .start(this)
    }

    private fun readIntent() {
        if (intent.extras != null) {
            result = intent.getStringExtra("DATA").toString()
            uri = Uri.parse(result)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = data?.let { UCrop.getOutput(it) }

            val intent = Intent()
            intent.putExtra("RESULT", resultUri.toString())
            setResult(-1, intent)
            finish()
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = data?.let { UCrop.getError(it) }
        }
    }
}
