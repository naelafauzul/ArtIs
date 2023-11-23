package com.example.artis

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.example.artis.Fragments.ProfileFragment
import com.example.artis.Model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import java.io.IOException

class AccountSettingsActivity : AppCompatActivity() {
    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri : Uri? = null
    private var storageProfilePicRef: StorageReference? = null

    private lateinit var profile_image_view_profile_frag: ImageView
    private lateinit var full_name_profile_frag: TextInputLayout
    private lateinit var bio_profile_frag: TextInputLayout
    private lateinit var username_profile_frag: TextInputLayout
    private lateinit var change_image_text_btn: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        profile_image_view_profile_frag = findViewById(R.id.profile_image_view_profile_frag)
        full_name_profile_frag = findViewById(R.id.full_name_profile_frag)
        bio_profile_frag = findViewById(R.id.bio_profile_frag)
        username_profile_frag = findViewById(R.id.username_profile_frag)

        val logout_btn: Button = findViewById(R.id.logout_btn)
        logout_btn.setOnClickListener{
           FirebaseAuth.getInstance().signOut()

            val intent = Intent(this@AccountSettingsActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        change_image_text_btn = findViewById(R.id.change_image_text_btn)
        change_image_text_btn.setOnClickListener {
            checker = "clicked"
            cropImage.launch(
                options {
                    setImageSource(
                        includeGallery = true,
                        includeCamera = true
                    )
                    setAspectRatio(1,1)
                    setGuidelines(CropImageView.Guidelines.ON)
                    setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                    setOutputCompressQuality(85)
                }
            )
        }


        val save_infor_profile_btn: ImageView = findViewById(R.id.save_infor_profile_btn)
        save_infor_profile_btn.setOnClickListener{
            if (checker == "clicked"){
                updateImageAndUpdateInfo()

            } else {
                updateUserInfoOnly()

            }
        }

        getUserInfo()
    }



    // Di dalam cropImage callback
    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // use the returned uri
            val uriContent = result.uriContent
            uriContent?.let { uri ->
                imageUri = uri

                // Jika Anda ingin menggunakan Bitmap untuk menampilkan gambar di ImageView
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this@AccountSettingsActivity.contentResolver, uri)
                    profile_image_view_profile_frag.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else {
            // an error occurred
            val exception = result.error
        }
    }


    private fun updateUserInfoOnly() {
        val fullNameEditText = full_name_profile_frag.editText
        val usernameEditText = username_profile_frag.editText
        val bioEditText = bio_profile_frag.editText

        val fullName = fullNameEditText?.text.toString()
        val username = usernameEditText?.text.toString()
        val bio = bioEditText?.text.toString()

        when {
            TextUtils.isEmpty(full_name_profile_frag.toString()) -> Toast.makeText(this, "Please write full name first.", Toast.LENGTH_LONG).show()
            username_profile_frag.toString() == "" -> Toast.makeText(this, "Please write user name first.", Toast.LENGTH_LONG).show()
            bio_profile_frag.toString() == "" -> Toast.makeText(this, "Please write your bio first.", Toast.LENGTH_LONG).show()

            else -> {
                val usersRef = FirebaseDatabase.getInstance().reference.child("Users")

                val userMap = HashMap<String, Any>()
                userMap["fullname"] = fullName.toLowerCase()
                userMap["username"] = username.toLowerCase()
                userMap["work"] = bio.toLowerCase()

                usersRef.child(firebaseUser.uid).updateChildren(userMap)

                val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                startActivity(intent)
                finish()

            }
        }
    }


    private fun getUserInfo() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(User::class.java)

                    user?.let {
                        Picasso.get()
                            .load(it.getImage())
                            .placeholder(R.drawable.profile)
                            .into(profile_image_view_profile_frag)

                        // Get TextInputEditText from TextInputLayout
                        val usernameEditText = username_profile_frag.editText
                        val fullNameEditText = full_name_profile_frag.editText
                        val bioEditText = bio_profile_frag.editText

                        // Set or get text from TextInputEditText
                        usernameEditText?.setText(user.getUsername())
                        fullNameEditText?.setText(user.getFullName())
                        bioEditText?.setText(user.getWork())
                    }
                }
            }


            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun updateImageAndUpdateInfo() {
        val fullNameEditText = full_name_profile_frag.editText
        val usernameEditText = username_profile_frag.editText
        val bioEditText = bio_profile_frag.editText
        val imageProfile = profile_image_view_profile_frag

        val fullName = fullNameEditText?.text.toString()
        val username = usernameEditText?.text.toString()
        val bio = bioEditText?.text.toString()




        when {
            imageUri == null-> Toast.makeText(this, "Please select image first.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(full_name_profile_frag.toString()) -> Toast.makeText(this, "Please write full name first.", Toast.LENGTH_LONG).show()
            username_profile_frag.toString() == "" -> Toast.makeText(this, "Please write user name first.", Toast.LENGTH_LONG).show()
            bio_profile_frag.toString() == "" -> Toast.makeText(this, "Please select image first.", Toast.LENGTH_LONG).show()

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait, we are updating your profile...")
                progressDialog.show()

                val fileRef = storageProfilePicRef!!.child(firebaseUser!!.uid + ".jpg")

                var uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (task.isSuccessful){
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener( OnCompleteListener<Uri> {task ->
                    if (task.isSuccessful){
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")
                        val userMap = HashMap<String, Any>()
                        userMap["fullname"] = fullName.toLowerCase()
                        userMap["username"] = username.toLowerCase()
                        userMap["work"] = bio.toLowerCase()
                        userMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)

                        val intent = Intent(this@AccountSettingsActivity, ProfileFragment::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()

                    }
                    else{
                        progressDialog.dismiss()
                    }

                })
            }
        }


    }
}