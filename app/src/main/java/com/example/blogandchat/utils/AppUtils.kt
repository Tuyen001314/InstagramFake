package com.example.blogandchat.utils

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

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