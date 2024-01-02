package com.example.blogandchat.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


fun optimizeAndConvertImageToByteArray(bitmap: Bitmap): ByteArray? {
    // Kích thước tối đa mong muốn của ảnh
    val maxWidth = 800
    val maxHeight = 800

    // Tính toán kích thước mới dựa trên tỉ lệ khung hình
    var width = bitmap.width
    var height = bitmap.height
    val ratio = width.toFloat() / height
    if (width > maxWidth || height > maxHeight) {
        if (ratio > 1) {
            width = maxWidth
            height = (width / ratio).toInt()
        } else {
            height = maxHeight
            width = (height * ratio).toInt()
        }
    }

    // Thay đổi kích thước ảnh
    val newBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

    // Chuyển đổi ảnh thành byte array
    val baos = ByteArrayOutputStream()
    newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
    val byteArray = baos.toByteArray()

    // Giải phóng bộ nhớ của bitmap
    newBitmap.recycle()
    return byteArray
}

fun isVideoFile(context: Context, uri: Uri): Boolean {
    val contentResolver: ContentResolver = context.contentResolver
    val type = contentResolver.getType(uri)
    return type != null && type.startsWith("video");
}

fun createFileFromUri(context: Context, uri: Uri?): File? {
    val contentResolver = context.contentResolver

    // Truy vấn thông tin về tài nguyên từ URI
    val cursor = contentResolver.query(uri!!, null, null, null, null)
    try {
        if (cursor != null && cursor.moveToFirst()) {
            val displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))

            // Tạo đối tượng File mới
            val file =
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), displayName)

            // Đọc dữ liệu từ InputStream của URI và ghi vào file mới
            val inputStream = contentResolver.openInputStream(uri)
            val outputStream: OutputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream!!.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            // Đóng luồng
            inputStream.close()
            outputStream.close()

            // Trả về đối tượng File đã tạo
            return file
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        cursor?.close()
    }

    // Trả về null nếu có lỗi
    return null
}

fun convertVideoToByteArray(context: Context, videoUri: Uri?): ByteArray? {
    val contentResolver = context.contentResolver
    var inputStream: InputStream? = null
    return try {
        // Mở InputStream từ URI
        inputStream = contentResolver.openInputStream(videoUri!!)

        // Đọc dữ liệu từ InputStream và chuyển đổi thành mảng byte
        val byteArrayOutputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream!!.read(buffer).also { bytesRead = it } != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead)
        }
        byteArrayOutputStream.toByteArray()
    } catch (e: IOException) {
        e.printStackTrace()
        null
    } finally {
        if (inputStream != null) {
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

fun convertUriToBitmap(context: Context, imageUri: Uri?): Bitmap? {
    val contentResolver = context.contentResolver
    var inputStream: InputStream? = null
    return try {
        // Mở InputStream từ URI
        inputStream = contentResolver.openInputStream(imageUri!!)

        // Đọc dữ liệu từ InputStream và chuyển đổi thành đối tượng Bitmap
        BitmapFactory.decodeStream(inputStream)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    } finally {
        if (inputStream != null) {
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

suspend fun getVideoFileSize(uri: Uri, context: Context): Long? {
    return withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        var fileSize: Long? = null
        val cursor = contentResolver.query(uri, null, null, null, null, null)

        cursor?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst() && sizeIndex != -1) {
                fileSize = cursor.getLong(sizeIndex)
            }
        }

        return@withContext fileSize
    }
}