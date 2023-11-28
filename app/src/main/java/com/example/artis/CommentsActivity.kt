package com.example.artis.Adapter


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.artis.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.example.artis.Adapter.CommentsAdapter
import com.example.artis.R
import de.hdodenhof.circleimageview.CircleImageView


class CommentsActivity : AppCompatActivity() {

    private var postId = ""
    private var publisherId = ""
    private var firebaseUser: FirebaseUser? = null
    private var commentAdapter: CommentsAdapter? = null
    private var commentList: MutableList<com.example.artis.Model.Comment>? = null
    private var add_comment: EditText? = null
    private var profile_image_comment: CircleImageView? = null
    private var post_image_comment: CircleImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        val post_comment: Button = findViewById(R.id.post_comment)


        val intent = intent
        postId = intent.getStringExtra("postId")!!
        publisherId = intent.getStringExtra("publisherId")!!

        firebaseUser = FirebaseAuth.getInstance().currentUser

        var recyclerView: RecyclerView
        recyclerView = findViewById(R.id.recycler_view_comments)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        recyclerView.layoutManager = linearLayoutManager

        commentList = ArrayList()
        commentAdapter = CommentsAdapter(this, commentList)
        recyclerView.adapter = commentAdapter

        getUserInfo()
        readComments()
        
        post_comment.setOnClickListener(View.OnClickListener {
            if (add_comment!!.text.toString() == "")
            {
                Toast.makeText(this@CommentsActivity, "Please write comment first.", Toast.LENGTH_LONG).show()
            }
            else
            {
                addComment()
            }
        })
    }

    private fun addComment() {
        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments").child(postId!!)

        val commentsMap = HashMap<String, Any>()
        commentsMap["comment"] = add_comment!!.text.toString()
        commentsMap["publisher"] = firebaseUser!!.uid

        commentsRef.push().setValue(commentsMap)

        add_comment!!.text.clear()

    }

    private fun getUserInfo() {
        val usersRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(profile_image_comment)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getPostImage() {
        val postRef = FirebaseDatabase.getInstance()
            .reference.child("Posts")
            .child(postId!!).child("postimage")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val image = p0.value.toString()
                    Picasso.get().load(image).placeholder(R.drawable.profile)
                        .into(post_image_comment)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun readComments()
    {
        val commentsRef = FirebaseDatabase.getInstance()
            .reference.child("Comments")
            .child(postId)

        commentsRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists())
                {
                    commentList!!.clear()

                    for(snapshot in snapshot.children)
                    {
                        val comment = snapshot.getValue(com.example.artis.Model.Comment::class.java)
                        commentList!!.add(comment!!)
                    }

                    commentAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}