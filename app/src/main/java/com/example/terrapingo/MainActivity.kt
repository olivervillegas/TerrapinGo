package com.example.terrapingo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.SignInButton
import androidx.core.app.ActivityCompat.startActivityForResult

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.terrapingo.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {
    //val default_web_client_id = "991889699168-rar4mt5mcn1i88i5gaepjmlvg4tta5c0.apps.googleusercontent.com";

    //view binding
    private lateinit var binding: ActivityMainBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    private companion object{
        private const val RC_SIGN_IN = 100
        private const val TAG = "GOOGLE_SIGN_IN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_main)


        //configure Google SignIn
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestId()
            .requestProfile()
            .requestEmail()
            .build()

        //getString(R.string.

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        binding.googleSignInBtn.setOnClickListener{
            Log.d(TAG, "onCreate: begin Google Sign In")
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }
        //updateUI(account)
    }

    private fun checkUser(){
        //check if user is logged in or not
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser != null){
            //user is already logged in
            //start Home activity
            startActivity(Intent(this@MainActivity, HomeActivity::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)

        //Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if(requestCode == RC_SIGN_IN){
            Log.d(TAG, "onActivityResult: Google SignIn intent result")
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                //Google SignIn success, now auth with firebase

                val account = accountTask.getResult(ApiException::class.java)
                firebaseAuthWithGoogleAccount(account)
            }
            catch (e: Exception){
                //failed Google SignIn
                Log.d(TAG, "onActivityResult: ${e.message}")
            }
        }


    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?){
        Log.d(TAG, "firebaseAuthWithGoogleAccount: begin firebase auth w/ google account")
        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                //login success
                //get loggedIn user
                val firebaseUser = firebaseAuth.currentUser
                //get user info
                val uid = firebaseUser!!.uid
                val email = firebaseUser!!.email

                val split: List<String>? = email?.split("@")
                val domain = split?.get(1) //This Will Give You The Domain After '@'

                if(domain.equals("terpmail.umd.edu"))
                {
                    //Proceed Ahead.
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: LoggedIn")

                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Uid: $uid")
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Email: $email")

                    //check if user is new or existing
                    if(authResult.additionalUserInfo!!.isNewUser){
                        //user is new - Account created
                        Log.d(TAG, "firebaseAuthWithGoogleAccount: Account created... \n$email")
                        Toast.makeText(this@MainActivity, "Account created...\n$email", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        //existing user - loggedIn
                        Log.d(TAG, "firebaseAuthWithGoogleAccount: Existing user...\n$email")
                        Toast.makeText(this@MainActivity, "LoggedIn...\n$email", Toast.LENGTH_SHORT).show()
                    }
                    //start Home activity
                    startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                    finish()
                } else {
                    //Show User Warning UI.
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Login failed due to: must log in with terpmail.umd.edu account")
                    Toast.makeText(this@MainActivity, "Login Failed due to: must log in with terpmail.umd.edu account", Toast.LENGTH_SHORT).show()
                    firebaseAuth.signOut()
                    finish()
                }

            }
            .addOnFailureListener { e ->
                //login failed
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Login failed due to ${e.message}")
                Toast.makeText(this@MainActivity, "Login Failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}