package com.example.artis

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.google.android.gms.common.api.Response
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import org.json.JSONObject
import java.io.IOException
import java.lang.reflect.Method

class AddPostActivity : AppCompatActivity() {
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storagePostPicRef: StorageReference? = null
    private lateinit var saveButton: ImageView
    private lateinit var imagePost: ImageView
    private lateinit var descriptionPost: EditText
    private lateinit var buttonRekomendasi: Button
    private lateinit var responseFromGPT: EditText

    var url = "https://api.openai.com/v1/completions"

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
                setAspectRatio(2, 1)
                setGuidelines(CropImageView.Guidelines.ON)
                setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                setOutputCompressQuality(85)
            }
        )

        responseFromGPT.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                // setting response tv on below line.
                descriptionPost.setText("Please wait the caption recommendation")
                // validating text
                if (responseFromGPT.text.toString().length > 0) {
                    // calling get response to get the response.
                    getResponse( "You are a literary assistant, so recommend a beautiful caption about this art in bahasa indonesia" + responseFromGPT.text.toString())
                } else {
                    Toast.makeText(this, "Please enter your art specifications", Toast.LENGTH_SHORT).show()
                }
                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun uploadImage() {
        when {
            imageUri == null -> Toast.makeText(this, "Please select an image first.", Toast.LENGTH_LONG).show()
            descriptionPost.text.toString().isEmpty() -> Toast.makeText(this, "Please write a description first.", Toast.LENGTH_LONG).show()
            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait while we are updating your art post...")
                progressDialog.show()

                val fileRef = storagePostPicRef!!.child("${System.currentTimeMillis()}.jpg")

                val uploadTask = fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>> { task ->
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
                            ref.child(it).updateChildren(postMap).addOnCompleteListener { postTask ->
                                if (postTask.isSuccessful) {
                                    Toast.makeText(this, "Post uploaded successfully.", Toast.LENGTH_LONG).show()
                                    val intent = Intent(this@AddPostActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this, "Failed to upload post.", Toast.LENGTH_LONG).show()
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
                    val bitmap = MediaStore.Images.Media.getBitmap(this@AddPostActivity.contentResolver, uri)
                    imagePost.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else {
            val exception = result.error
        }
    }

    private fun getResponse(query: String) {
        // setting text on for question on below line.
        responseFromGPT.setText("")
        // creating a queue for request queue.
        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)
        // creating a json object on below line.
        val jsonObject: JSONObject? = JSONObject()
        // adding params to json object.
        jsonObject?.put("model", "text-davinci-003")
        jsonObject?.put("prompt", query)
        jsonObject?.put("temperature", 0)
        jsonObject?.put("max_tokens", 100)
        jsonObject?.put("top_p", 1)
        jsonObject?.put("frequency_penalty", 0.0)
        jsonObject?.put("presence_penalty", 0.0)

        // on below line making json object request.
        val postRequest: JsonObjectRequest =
            // on below line making json object request.
            object : JsonObjectRequest(
                Method.POST, url, jsonObject,
                com.android.volley.Response.Listener { response ->
                    // on below line getting response message and setting it to text view.
                    val responseMsg: String =
                        response.getJSONArray("choices").getJSONObject(0).getString("text")
                    descriptionPost.setText(responseMsg)
                },
                // adding on error listener
                com.android.volley.Response.ErrorListener { error ->
                    Log.e("TAGAPI", "Error is : " + error.message + "\n" + error)
                }) {
                override fun getHeaders(): kotlin.collections.MutableMap<kotlin.String, kotlin.String> {
                    val params: MutableMap<String, String> = HashMap()
                    // adding headers on below line.
                    params["Content-Type"] = "application/json"
                    params["Authorization"] =
                        "Bearer "
                    return params;
                }
            }

        // on below line adding retry policy for our request.
        postRequest.setRetryPolicy(object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        })
        // on below line adding our request to queue.
        queue.add(postRequest)
    }
}


