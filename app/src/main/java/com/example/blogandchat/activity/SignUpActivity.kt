package com.example.blogandchat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.blogandchat.R
import com.example.blogandchat.firebase.FireStore
import com.example.blogandchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        firebaseAuth = FirebaseAuth.getInstance()

        button_sign_up.setOnClickListener {
            registerUser()
        }

        sign_in_text.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }


    }

    private fun registerUser() {
        val email: String = edt_sign_up_name.text.toString().trim()
        val pass: String = edt_sign_up_pass.text.toString().trim()

        if(email.isNotEmpty() && pass.isNotEmpty()) {
            if(email.length >= 10 && pass.length >= 6) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass).addOnCompleteListener { taskId ->
                    if (taskId.isSuccessful) {
                        val firebaseUser: FirebaseUser = taskId.result!!.user!!
                        val registerdEmail = firebaseUser.email!!

                        Toast.makeText(this, "dang ky thang cong", Toast.LENGTH_LONG).show()
                        val intent: Intent = Intent(this, SetUpActivity::class.java)
                        intent.putExtra("email", registerdEmail)
                        intent.putExtra("id", firebaseUser.uid)
                        startActivity(intent)
                    }
                    else {
                        Toast.makeText(this, taskId.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else {
                Toast.makeText(this, "email toi thieu 10 ky tu va mat khau toi thieu 6 ky tu", Toast.LENGTH_SHORT).show()
            }
        }
        else {
            Toast.makeText(this, "Vui long dien day du thong tin", Toast.LENGTH_SHORT).show()
        }
    }



}