package com.example.blogandchat.model

import android.text.format.DateFormat
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

data class PostDetailModel(
    val image: String,
    val caption: String,
    val time: Timestamp? = null,
    val userName: String,
    val imageUser: String,
    val idUser: String,
    var isYour: Boolean,
    var isLiked: Boolean,

    ) : PostId() {
    val timeForShow: String
        get() {
            val calendar = Calendar.getInstance().time
            //val simpleDateFormat = SimpleDateFormat("hh:mm a")
            val currentTime: Long = calendar.time
            val milliseconds: Long = time?.toDate()?.time ?: 0

            val a = currentTime - milliseconds

            val minutes = TimeUnit.MILLISECONDS.toMinutes(a)

            if (minutes / 60 <= 24) {
                if (minutes <= 0) return "vài giây trước"
                else {
                    if (minutes < 60) return "$minutes phút trước"
                    else
                        return "${minutes / 60} giờ trước"
                }
            } else {
                val date = DateFormat.format("MM/dd/yyyy", Date(milliseconds)).toString()
                return date
            }
        }
}
