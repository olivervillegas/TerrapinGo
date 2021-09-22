package com.example.terrapingo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.SignInButton
import androidx.core.app.ActivityCompat.startActivityForResult

import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.terrapingo.databinding.ActivityHomeBinding
import com.example.terrapingo.databinding.ActivityMainBinding
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import android.R
import android.view.*

import com.google.android.material.bottomnavigation.BottomNavigationView

import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import android.widget.TextView
import org.w3c.dom.Text
import android.view.View
import java.net.URL

import java.io.InputStream

import android.graphics.BitmapFactory

import android.graphics.Bitmap
import android.net.Uri

import android.widget.ImageView
import java.net.URI


class HomeActivity : AppCompatActivity(){
    //view binding
    private lateinit var binding: ActivityHomeBinding
    private lateinit var menuBar: MenuItem
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_home)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //handle click, logout user
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        bottomNavigationView = binding.bottomBar

        val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                com.example.terrapingo.R.id.homeBtn -> {
                    val fragment = HomeActivity.HomeFragment()
                    supportFragmentManager.beginTransaction().replace(com.example.terrapingo.R.id.nav_container, fragment, fragment.javaClass.getSimpleName())
                        .commit()
                    return@OnNavigationItemSelectedListener true
                }
                com.example.terrapingo.R.id.challengesBtn -> {
                    val fragment = ChallengesFragment()
                    supportFragmentManager.beginTransaction().replace(com.example.terrapingo.R.id.nav_container, fragment, fragment.javaClass.getSimpleName())
                        .commit()
                    return@OnNavigationItemSelectedListener true
                }
                com.example.terrapingo.R.id.informationBtn -> {
                    val fragment = InfoFragment()
                    supportFragmentManager.beginTransaction().replace(com.example.terrapingo.R.id.nav_container, fragment, fragment.javaClass.getSimpleName())
                        .commit()
                    return@OnNavigationItemSelectedListener true
                }
                com.example.terrapingo.R.id.profileBtn -> {
                    val fragment = ProfileFragment()
                    supportFragmentManager.beginTransaction().replace(com.example.terrapingo.R.id.nav_container, fragment, fragment.javaClass.getSimpleName())
                        .commit()
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        if (savedInstanceState == null) {
            val fragment = HomeActivity.HomeFragment()
            supportFragmentManager.beginTransaction().replace(com.example.terrapingo.R.id.nav_container, fragment, fragment.javaClass.getSimpleName())
                .commit()
        }



        /*if (Intent.ACTION_VIEW == intent.action) {
            val uri = intent.data
            val valueOne = uri!!.getQueryParameter("keyOne")
            val valueTwo = uri!!.getQueryParameter("keyTwo")
        }*/
    }

    class ChallengesFragment : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(com.example.terrapingo.R.layout.fragment_challenges, container, false)
        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
        }
    }


    class HomeFragment : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(com.example.terrapingo.R.layout.fragment_home, container, false)
        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
        }
    }


    class InfoFragment : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(com.example.terrapingo.R.layout.fragment_info, container, false)
        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
        }
    }

    class ProfileFragment : Fragment() {
        private lateinit var firebaseAuth: FirebaseAuth
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            inflater.inflate(com.example.terrapingo.R.layout.fragment_profile, container, false)
            val myInflatedView: View = inflater.inflate(com.example.terrapingo.R.layout.fragment_profile, container, false)

            // Set the Text to try this out
            val emailText = myInflatedView.findViewById<TextView>(com.example.terrapingo.R.id.emailTv)
            val nameText = myInflatedView.findViewById<TextView>(com.example.terrapingo.R.id.nameTv)

            firebaseAuth = FirebaseAuth.getInstance()
            val firebaseUser = firebaseAuth.currentUser
            val email = firebaseUser?.email.toString()
            val name_user = firebaseUser?.displayName.toString()
            val photo_user = firebaseUser?.photoUrl
            emailText.text = email
            nameText.text = name_user
            val i = myInflatedView.findViewById<ImageView>(com.example.terrapingo.R.id.imageV)
            //val bitmap = BitmapFactory.decodeStream(URL(photo_user).openStream())
            i.setImageURI(photo_user)

            return myInflatedView
        }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
        }
    }

    private fun checkUser() {
        //get current user
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            //user not logged in
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }else{
            //user logged in
            //get user info
            val email = firebaseUser.email.toString()

            val name_user = firebaseUser.displayName.toString()
            //set email
            //binding.emailTv.text = name
        }
    }
}