package com.example.artis

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    private lateinit var fullName: TextView
    private lateinit var userName: TextView
    private lateinit var email:TextView
    private lateinit var password:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        fullName = findViewById(R.id.fullName_SignUp)
        userName = findViewById(R.id.username_SignUp)
        email = findViewById(R.id.email_SignUp)
        password = findViewById(R.id.pass_signUp)

        val signinTextView: TextView = findViewById(R.id.signin_link_textview)
        signinTextView.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        val SignUp_btn: Button = findViewById(R.id.SignUp_btn)
        SignUp_btn.setOnClickListener{
            CreateAccount()
        }
    }

    private fun CreateAccount() {
        val fullNameInput = fullName.text.toString()
        val userNameInput = userName.text.toString()
        val emailInput = email.text.toString()
        val passwordInput = password.text.toString()
        val progressDialog = ProgressDialog(this@SignUpActivity)

        when {
            TextUtils.isEmpty(fullNameInput) -> {
                Toast.makeText(this@SignUpActivity, "Full name is required", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(userNameInput) -> {
                Toast.makeText(this@SignUpActivity, "Username is required", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(emailInput) -> {
                Toast.makeText(this@SignUpActivity, "Email is required", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(passwordInput) -> {
                Toast.makeText(this@SignUpActivity, "Password is required", Toast.LENGTH_LONG).show()
            }
            !isPasswordValid(passwordInput) -> {
                Toast.makeText(this@SignUpActivity, "Password must contain at least 8 characters with letters and numbers", Toast.LENGTH_LONG).show()
            }
            else -> {
                // All fields are filled and password meets criteria, proceed with account creation...
                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                progressDialog.setTitle("SignUp")
                progressDialog.setMessage("Please wait, this may take a while...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                mAuth.createUserWithEmailAndPassword(emailInput, passwordInput)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            saveUserInfo(fullNameInput, userNameInput, emailInput, progressDialog)
                        } else {
                            val message = task.exception?.message ?: "Unknown error occurred"
                            Toast.makeText(this@SignUpActivity, "Error: $message", Toast.LENGTH_LONG).show()

                            if (task.exception is FirebaseAuthUserCollisionException) {
                                Toast.makeText(this@SignUpActivity, "Email is already registered", Toast.LENGTH_LONG).show()
                                progressDialog.dismiss()
                            } else {
                                mAuth.signOut()
                                progressDialog.dismiss()
                            }
                        }
                    }
            }
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        val passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}\$".toRegex()
        return passwordPattern.matches(password)
    }

    private fun saveUserInfo(fullName: String, userName: String, email: String, progressDialog: ProgressDialog) {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        // Generate Dynamic Link Firebase untuk User
        val dynamicLink = "https://artis.page.link/user?userId=$currentUserId&token=secureToken"

        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserId
        userMap["fullname"] = fullName.toLowerCase()
        userMap["username"] = userName.toLowerCase()
        userMap["email"] = email
        userMap["work"] = "I'm an artist"
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/artist-app-ea1fd.appspot.com/o/Default%20Images%2Fprofile.png?alt=media&token=e1e73fd6-4844-4d04-87da-9c02d1437eb3"

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && user.uid == currentUserId) {
            // User sudah login dan dapat mengakses link QR Code
            // Pindah ke halaman profile user lain
        } else {
            // User diharap login untuk membuka tautan QR Code
            // Pindah halaman Sign Up
        }
        // Tambah link unik ke UserMap
        userMap["qrCodeLink"] = dynamicLink

        usersRef.child(currentUserId).setValue(userMap)
            .addOnCompleteListener{task ->
                if(task.isSuccessful){
                    progressDialog.dismiss()
                    Toast.makeText(this, "Account has been created successfully.", Toast.LENGTH_LONG).show()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                else {
                    val message= task.exception!!.toString()
                    Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }

            }
    }
}