package com.example.artis

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
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
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import java.io.IOException

class AddPostActivity : AppCompatActivity() {
    private var myUrl = ""
    private var imageUri : Uri? = null
    private var storagePostPicRef: StorageReference? = null
    private lateinit var save_new_post_btn: ImageView
    private lateinit var image_post: ImageView
    private lateinit var description_post: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        storagePostPicRef = FirebaseStorage.getInstance().reference.child("Post Pictures")

        image_post = findViewById(R.id.image_post)
        description_post = findViewById(R.id.description_post)
        save_new_post_btn = findViewById(R.id.save_new_post_btn)
        save_new_post_btn.setOnClickListener {
            uploadImage()
        }
        cropImage.launch(
            options {
                setImageSource(
                    includeGallery = true,
                    includeCamera = true
                )
                setAspectRatio(2,1)
                setGuidelines(CropImageView.Guidelines.ON)
                setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                setOutputCompressQuality(85)
            }
        )
    }

    private fun uploadImage() {
        when{
            imageUri == null-> Toast.makeText(this, "Please select image first.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(description_post.toString()) -> Toast.makeText(this, "Please write full name first.", Toast.LENGTH_LONG).show()

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait, we are updating your art post...")
                progressDialog.show()

                var fileRef = storagePostPicRef!!.child(System.currentTimeMillis().toString() + ".jpg")

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
                        val postId = ref.push().key

                        val postMap = HashMap<String, Any>()
                        postMap["postId"] = postId!!
                        postMap["description"] = description_post.text.toString().toLowerCase()
                        postMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                        postMap["postimage"] = myUrl

                        ref.child(postId).updateChildren(postMap)

                        Toast.makeText(this, "Post upload successfully.", Toast.LENGTH_LONG).show()

                        val intent = Intent(this@AddPostActivity, MainActivity::class.java)
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

    // Di dalam cropImage callback
    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // use the returned uri
            val uriContent = result.uriContent
            uriContent?.let { uri ->
                imageUri = uri

                // Jika Anda ingin menggunakan Bitmap untuk menampilkan gambar di ImageView
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this@AddPostActivity.contentResolver, uri)
                    image_post.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else {
            // an error occurred
            val exception = result.error
        }
    }
}