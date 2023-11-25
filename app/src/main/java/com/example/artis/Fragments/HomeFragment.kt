package com.example.artis.Fragments

import android.media.Image
import android.os.Bundle
import android.provider.ContactsContract.Profile
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.artis.Adapter.PostAdapter
import com.example.artis.Model.Post
import com.example.artis.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<Post>? = null
    private var followingList: MutableList<Post>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        /*
        // Inflate the layout for this fragment
        val profileButton: ImageButton = view.findViewById(R.id.profileButton)
        profileButton.setOnClickListener {

            val profileFragment = ProfileFragment()
            val transaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.fragment_container, profileFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        // nanti di loop
        setupToggleButton(view.findViewById(R.id.heartPost1))
        setupToggleButton(view.findViewById(R.id.heartPost2))

*/


        /*
    private fun setupToggleButton(btn: ImageButton) {
        var isLiked = false

        btn.setOnClickListener {
            if (isLiked) {
                btn.setImageResource(R.drawable.likes)
                TODO("Kode buat nambah data post yang di likes dibawah ini nanti")
                //...
            } else {
                btn.setImageResource(R.drawable.likes_hearted)
                TODO("Kode buat nambah data post yang di unllikes dibawah ini nanti")
                //...
            }

            isLiked = !isLiked
        }
    } */

        var recyclerView: RecyclerView? = null
        recyclerView = view.findViewById(R.id.app_bar_layout) //harusnya recycler_view_home
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it, postList as ArrayList<Post>) }
        recyclerView.adapter = postAdapter

        checkFollowings()

        return view

    }


    private fun checkFollowings() {
        followingList = ArrayList()

        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (followingList as ArrayList<String>).clear()

                    for (snapshot in p0.children) {
                        snapshot.key?.let { (followingList as ArrayList<String>).add(it) }
                    }

                    retrievePosts()

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                postList?.clear()

                for (snapshot in p0.children) {
                    val post = snapshot.getValue(Post::class.java)

                    for (userID in (followingList as ArrayList<String>)) {
                        if (post!!.getPublisher() == id.toString()) {
                            postList!!.add(post)
                        }

                        postAdapter!!.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }
}
