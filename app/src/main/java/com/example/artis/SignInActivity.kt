package com.example.artis

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth


class SignInActivity : AppCompatActivity() {
    private lateinit var email: TextInputEditText
    private lateinit var password: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        email = findViewById(R.id.email_login)
        password = findViewById(R.id.pass_login)

        val login_btn: Button = findViewById(R.id.login_btn)
        login_btn.setOnClickListener{
            loginUser()
        }

        val signupTextView: TextView = findViewById(R.id.signup_link_textview)
        signupTextView.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun loginUser(){
        val emailStr = email.text.toString()
        val passwordStr = password.text.toString()
        val progressDialog = ProgressDialog(this@SignInActivity)

        when {
            TextUtils.isEmpty(emailStr) -> {
                Toast.makeText(this@SignInActivity, "Email is required", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(passwordStr) -> {
                Toast.makeText(this@SignInActivity, "Password is required", Toast.LENGTH_LONG).show()
            }
            else -> {
                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
                progressDialog.setTitle("Sign In")
                progressDialog.setMessage("Please wait, this may take a while...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                mAuth.signInWithEmailAndPassword(emailStr, passwordStr).addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        progressDialog.dismiss()

                        val intent = Intent(this@SignInActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    } else {
                        progressDialog.dismiss()
                        Toast.makeText(this@SignInActivity, "Invalid password. Please try again.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    override fun onStart(){
        super.onStart()

        if (FirebaseAuth.getInstance().currentUser != null){
            val intent = Intent(this@SignInActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}

