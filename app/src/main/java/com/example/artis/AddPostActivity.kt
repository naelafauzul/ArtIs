package com.example.artis

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.IOException

class AddPostActivity : AppCompatActivity() {
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storagePostPicRef: StorageReference? = null
    private lateinit var saveButton: ImageView
    private lateinit var imagePost: ImageView
    private lateinit var descriptionPost: EditText
    private lateinit var responseFromGPT: EditText

    val apiServices = ApiServices(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        storagePostPicRef = FirebaseStorage.getInstance().reference.child("Post Pictures")

        imagePost = findViewById(R.id.image_post)
        responseFromGPT = findViewById(R.id.responseFromGPT)
        descriptionPost = findViewById(R.id.description_post)
        saveButton = findViewById(R.id.save_new_post_btn)


        saveButton.setOnClickListener {
            uploadImage()
        }

        cropImage.launch(
            options {
                setImageSource(
                    includeGallery = true,
                    includeCamera = true
                )
                setAspectRatio(3, 2)
                setGuidelines(CropImageView.Guidelines.ON)
                setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                setOutputCompressQuality(85)
            }
        )

        responseFromGPT.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                if (responseFromGPT.text.toString().isNotEmpty()) {
                    apiServices.getResponseFromGPT(
                        "You are a literary assistant, so recommend especially beautiful or poetic captions or sentences (write in bahasa indonesia) about art for which the user inputs descriptions: " + responseFromGPT.text.toString(),
                        { response ->
                            descriptionPost.setText(response)
                        },
                        { error ->
                            Log.e("TAGAPI", error)
                            print(error)
                        }
                    )
                } else {
                    Toast.makeText(this, "Please enter your art specifications", Toast.LENGTH_SHORT)
                        .show()
                }
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun uploadImage() {
        when {
            imageUri == null -> Toast.makeText(
                this,
                "Please select an image first.",
                Toast.LENGTH_LONG
            ).show()

            descriptionPost.text.toString().isEmpty() -> Toast.makeText(
                this,
                "Please write a description first.",
                Toast.LENGTH_LONG
            ).show()

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait while we are updating your art post...")
                progressDialog.show()

                val fileRef = storagePostPicRef!!.child("${System.currentTimeMillis()}.jpg")

                val uploadTask = fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener(OnCompleteListener<Uri> { task ->
                    progressDialog.dismiss()
                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")
                        val postId = ref.push().key

                        val postMap = HashMap<String, Any>()
                        postId?.let {
                            postMap["postId"] = it
                        }
                        postMap["description"] = descriptionPost.text.toString().toLowerCase()
                        postMap["publisher"] = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        postMap["postimage"] = myUrl

                        postId?.let {
                            ref.child(it).updateChildren(postMap)
                                .addOnCompleteListener { postTask ->
                                    if (postTask.isSuccessful) {
                                        Toast.makeText(
                                            this,
                                            "Post uploaded successfully.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        val intent =
                                            Intent(this@AddPostActivity, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Failed to upload post.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                        }
                    } else {
                        Toast.makeText(this, "Failed to get image URL.", Toast.LENGTH_LONG).show()
                    }
                })
            }
        }
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uriContent = result.uriContent
            uriContent?.let { uri ->
                imageUri = uri
                try {
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(this@AddPostActivity.contentResolver, uri)
                    imagePost.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else {
            val exception = result.error
        }
    }
}