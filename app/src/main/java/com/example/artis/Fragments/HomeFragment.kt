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
import com.example.artis.R

class HomeFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_home, container, false)

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

        return view

    }

    private fun setupToggleButton(btn: ImageButton) {
        var isLiked = false

        btn.setOnClickListener {
            if (isLiked) {
                btn.setImageResource(R.drawable.likes)
            } else {
                btn.setImageResource(R.drawable.likes_hearted)
            }

            isLiked = !isLiked
        }
    }
}