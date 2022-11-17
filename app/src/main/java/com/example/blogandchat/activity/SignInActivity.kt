package com.example.blogandchat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.blogandchat.R
import com.example.blogandchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        button_sign_in.setOnClickListener {
            userSignIn()
        }

        sign_up_text.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

    }

    private fun userSignIn() {
        val email: String = edt_sign_in_name.text.toString().trim {it <= ' '}
        val pass: String = edt_sign_in_pass.text.toString().trim {it <= ' '}

        if(email.isNotEmpty() && pass.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass).addOnCompleteListener { taskId ->
                    if (taskId.isSuccessful) {
                        val firebaseUser: FirebaseUser = taskId.result!!.user!!
                        val registerdEmail = firebaseUser.email!!
                        Toast.makeText(this, "dang nhap thang cong", Toast.LENGTH_LONG).show()
                        //FirebaseAuth.getInstance().signOut()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    else {
                        Toast.makeText(this, taskId.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }
        else {
            Toast.makeText(this, "Vui long dien day du thong tin", Toast.LENGTH_SHORT).show()
        }
    }
}