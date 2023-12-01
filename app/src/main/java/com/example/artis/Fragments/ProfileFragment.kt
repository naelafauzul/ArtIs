package com.example.artis.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.artis.AccountSettingsActivity
import com.example.artis.Adapter.MyImagesAdapter
import com.example.artis.Model.Post
import com.example.artis.Model.User
import com.example.artis.QRProfileActivity
import com.example.artis.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.lang.reflect.Array
import java.util.Collections


class ProfileFragment : Fragment() {
    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var edit_account_settings_btn: Button
    private lateinit var total_followers: TextView
    private lateinit var total_following: TextView
    private lateinit var pro_image_profile_frag: ImageView
    private lateinit var full_name_profile_frag: TextView
    private lateinit var bio_profile_frag: TextView
    private lateinit var total_arts: TextView


    var postList: List<Post>? = null
    var myImagesAdapter: MyImagesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val qrButton: ImageButton = view.findViewById(R.id.qrButton)

        edit_account_settings_btn = view.findViewById<Button>(R.id.edit_account_settings_btn)
        total_followers = view.findViewById(R.id.total_followers)
        total_following = view.findViewById(R.id.total_following)
        pro_image_profile_frag = view.findViewById(R.id.pro_image_profile_frag)
        full_name_profile_frag = view.findViewById(R.id.full_name_profile_frag)
        bio_profile_frag = view.findViewById(R.id.bio_profile_frag)
        total_arts = view.findViewById(R.id.total_arts)


        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = requireContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            this.profileId = pref.getString("profileId", "none").toString()
        }
        if (profileId == firebaseUser.uid) {
            edit_account_settings_btn.text = "Edit Profile"
        } else if (profileId != firebaseUser.uid) {
            checkFollowAndFollowButtonStatus()
        }

        var recyclerViewUploadImage : RecyclerView
        recyclerViewUploadImage = view.findViewById(R.id.recycler_view_upload_pic)
        recyclerViewUploadImage.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewUploadImage.layoutManager = linearLayoutManager

        postList = ArrayList()
        myImagesAdapter = context?.let { MyImagesAdapter(it, postList as ArrayList<Post>) }
        recyclerViewUploadImage.adapter = myImagesAdapter


        edit_account_settings_btn.setOnClickListener {
            val getButtonText = edit_account_settings_btn.text.toString()

            when {
                getButtonText == "Edit Profile" -> startActivity(Intent(context, AccountSettingsActivity::class.java))

                getButtonText == "Follow" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId)
                            .setValue(true)
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString())
                            .setValue(true)
                    }
                }

                getButtonText == "Following" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId)
                            .removeValue()
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString())
                            .removeValue()
                    }
                }
            }

        }

        if (profileId == firebaseUser.uid) {
            qrButton.visibility = View.VISIBLE
            qrButton.isEnabled = true
        } else {
            qrButton.visibility = View.INVISIBLE
            qrButton.isEnabled = false

            val params = qrButton.layoutParams as LinearLayout.LayoutParams
            params.weight = 0f
            qrButton.layoutParams = params
        }

        getFollowers()
        getFollowings()
        getUserInfo()
        myPhotos()
        getTotalNumberOfPosts()

        qrButton.setOnClickListener {
            val intent = Intent(activity, QRProfileActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun checkFollowAndFollowButtonStatus() {
        val qrButton = view?.findViewById<ImageButton>(R.id.qrButton)
        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }

        if (followingRef != null) {
            followingRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.child(profileId).exists()) {
                        edit_account_settings_btn.text = "Following"
                    } else {
                        edit_account_settings_btn.text = "Follow"
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Error: ${databaseError.message}")
                }
            })
        }
    }


    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    total_followers?.text = p0.childrenCount.toString()

                }
            }

            override fun onCancelled(p0: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun getFollowings() {
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Following")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    total_following?.text = p0.childrenCount.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun myPhotos()
    {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists())
                {
                    (postList as ArrayList<Post>).clear()

                    for (snapshot in snapshot.children)
                    {
                        val post = snapshot.getValue(Post::class.java)
                        if (post != null && post.getPublisher().equals(profileId))
                        {
                            (postList as ArrayList<Post>).add(post)
                        }
                        Collections.reverse(postList)
                        myImagesAdapter!!.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    private fun getUserInfo() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(profileId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)

                    user?.let {
                        Picasso.get()
                            .load(it.getImage())
                            .placeholder(R.drawable.profile)
                            .into(pro_image_profile_frag)

                        full_name_profile_frag.text = it.getFullName()
                        bio_profile_frag.text = it.getWork()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    override fun onStop() {
        super.onStop()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    private fun getTotalNumberOfPosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    var postCounter = 0

                    for (snapShot in dataSnapshot.children)
                    {
                        val post = snapShot.getValue(Post::class.java)!!

                        if (post.getPublisher() == profileId)
                        {
                            postCounter++
                        }
                    }
                    total_arts.text = " " + postCounter
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}
