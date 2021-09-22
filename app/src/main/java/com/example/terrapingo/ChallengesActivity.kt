package com.example.terrapingo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.example.terrapingo.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class ChallengesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    //firebase auth
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenges)
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
            val email = firebaseUser.email
            val name = firebaseUser.displayName
            //set email
            //binding.emailTv.text = name
        }
    }
}