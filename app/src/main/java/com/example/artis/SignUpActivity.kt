package com.example.artis

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val signinButton: Button = findViewById(R.id.signin_link_btn)
        signinButton.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}