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

        val signinButton: Button = findViewById(R.id.signin_link_btn)
        signinButton.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        val SignUp_btn: Button = findViewById(R.id.SignUp_btn)
        SignUp_btn.setOnClickListener{
            CreateAccount()
        }
    }

    private fun CreateAccount() {
        val fullName = fullName.text.toString()
        val userName = userName.text.toString()
        val email = email.text.toString()
        val password = password.text.toString()
        val progressDialog = ProgressDialog(this@SignUpActivity)

        when {
                TextUtils.isEmpty(fullName) -> Toast.makeText(this@SignUpActivity, "Full name is required", Toast.LENGTH_LONG).show()
                TextUtils.isEmpty(userName) -> Toast.makeText(this@SignUpActivity, "Username is required", Toast.LENGTH_LONG).show()
                TextUtils.isEmpty(email) -> Toast.makeText(this@SignUpActivity, "Email is required", Toast.LENGTH_LONG).show()
                TextUtils.isEmpty(password) -> Toast.makeText(this@SignUpActivity, "Password is required", Toast.LENGTH_LONG).show()

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("SignUp")
                progressDialog.setMessage("Please wait, this may take a while...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener{task ->
                        if (task.isSuccessful){
                            saveUserInfo(fullName,userName,email,progressDialog)
                        } else {
                            val message= task.exception!!.toString()
                            Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }
                    }
            }
        }
    }

    private fun saveUserInfo(fullName: String, userName: String, email: String, progressDialog: ProgressDialog) {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserId
        userMap["fullname"] = fullName
        userMap["username"] = userName
        userMap["email"] = email
        userMap["work"] = "I'm an artist"
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/artist-app-ea1fd.appspot.com/o/Default%20Images%2Fprofile.png?alt=media&token=e1e73fd6-4844-4d04-87da-9c02d1437eb3"

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