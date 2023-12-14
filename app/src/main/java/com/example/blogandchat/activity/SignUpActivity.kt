package com.example.blogandchat.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.blogandchat.R
import com.example.blogandchat.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    private val viewModel: SignUpViewModel by viewModels()
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_sign_up)
        firebaseAuth = FirebaseAuth.getInstance()

        viewModel.uiState.observe(this) { uiState ->
            uiState?.let {
                if (uiState.result == true) {
                    Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_LONG).show()
                    val intent: Intent = Intent(this, SetUpActivity::class.java)
                    intent.putExtra("email", uiState.email)
                    intent.putExtra("id", uiState.id)
                    startActivity(intent)
                } else if (uiState.check == false) {
                    Toast.makeText(this, "Email tối thiểu 10 ký tự và Password tối thiểu 6 ký tự", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(
                        this,
                        "Đăng ký thất bại. Email này đã được đăng ký trước đó",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        binding.buttonSignUp.setOnClickListener {
            viewModel.userSignUp(binding.edtSignUpName.text.toString().trim(), binding.edtSignUpPass.text.toString().trim())
        }

        binding.signInText.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

    }

//    private fun registerUser() {
//        val email: String = edt_sign_up_name.text.toString().trim()
//        val pass: String = edt_sign_up_pass.text.toString().trim()
//
//        if(email.isNotEmpty() && pass.isNotEmpty()) {
//            if(email.length >= 10 && pass.length >= 6) {
//                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass).addOnCompleteListener { taskId ->
//                    if (taskId.isSuccessful) {
//                        val firebaseUser: FirebaseUser = taskId.result!!.user!!
//                        val registerdEmail = firebaseUser.email!!
//
//                        Toast.makeText(this, "dang ky thang cong", Toast.LENGTH_LONG).show()
//                        val intent: Intent = Intent(this, SetUpActivity::class.java)
//                        intent.putExtra("email", registerdEmail)
//                        intent.putExtra("id", firebaseUser.uid)
//                        startActivity(intent)
//                    }
//                    else {
//                        Toast.makeText(this, taskId.exception!!.message, Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//            else {
//                Toast.makeText(this, "email toi thieu 10 ky tu va mat khau toi thieu 6 ky tu", Toast.LENGTH_SHORT).show()
//            }
//        }
//        else {
//            Toast.makeText(this, "Vui long dien day du thong tin", Toast.LENGTH_SHORT).show()
//        }
//    }

}