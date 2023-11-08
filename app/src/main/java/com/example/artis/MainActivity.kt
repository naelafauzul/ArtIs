package com.example.artis

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.artis.Fragments.HomeFragment
import com.example.artis.Fragments.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    internal var selectedFragment: Fragment? = null

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                selectedFragment = HomeFragment ()

            }
            R.id.nav_search -> {
                selectedFragment = SearchFragment ()

            }
            R.id.nav_add_post -> {

            }
//            R.id.nav_notifications -> {
//                textView.setText("Notif")
//                return@OnNavigationItemSelectedListener true
//            }
//            R.id.nav_profile -> {
//                textView.setText("Profile")
//                return@OnNavigationItemSelectedListener true
//            }
        }
        if (selectedFragment != null){
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                selectedFragment!!
            ).commit()
        }

        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        supportFragmentManager.beginTransaction().replace(
            R.id.fragment_container,
            HomeFragment()
        ).commit()
    }
}