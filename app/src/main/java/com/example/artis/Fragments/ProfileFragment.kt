package com.example.artis.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.artis.AccountSettingsActivity
import com.example.artis.QRProfileActivity
import com.example.artis.R
import com.google.android.material.button.MaterialButton

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val editProfileBtn: MaterialButton = view.findViewById(R.id.edit_account_settings_btn)
        val qrButton: ImageButton = view.findViewById(R.id.qrButton)

        editProfileBtn.setOnClickListener {
            // Buat Intent untuk memulai AccountSettingActivity
            val intent = Intent(activity, AccountSettingsActivity::class.java)
            startActivity(intent)
        }

        qrButton.setOnClickListener {
            val intent = Intent(activity, QRProfileActivity::class.java)
            startActivity(intent)
        }

        return view
    }

}
