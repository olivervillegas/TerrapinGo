package com.example.terrapingo

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import android.content.ContentValues.TAG
import android.content.Context

import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.location.LocationListener
import android.location.LocationManager

import android.os.Build
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class FirstChallengeActivity : AppCompatActivity(), LocationListener, GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener {
    private final var TIME_ZONE_BIAS: Long = 14400000;
    private final var CHALLENGE_NAME: String = "ExampleChallenge"
    private final var CHALLENGE_LAT_MAX: Double = 38.992426
    private final var CHALLENGE_LAT_MIN: Double = 38.992026
    private final var CHALLENGE_LONG_MAX: Double = -76.941040
    private final var CHALLENGE_LONG_MIN: Double = -76.949640
    private final var FIRST_TIME_PTS: Long = 15
    private final var REGULAR_PTS: Long = 2
    private final var DAY_IN_UNIX_MS: Long = 86400000
    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var tvGpsLocation: TextView
    private lateinit var tvMessage: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    // Unique tag for the error dialog fragment
    private val DIALOG_ERROR = "dialog_error"

    // Bool to track whether the app is already resolving an error
    private var mResolvingError = false

    // Request code to use when launching the resolution activity
    private val REQUEST_RESOLVE_ERROR = 555
    private val locationPermissionCode = 2

    var ACCESS_FINE_LOCATION_CODE = 3310
    var ACCESS_COARSE_LOCATION_CODE = 3410
    private lateinit var mGoogleApiClient: GoogleApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildGoogleApiClient()
        setContentView(R.layout.activity_first_challenge)

        /*fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                val mCurrentLocation = location
                Log.d("TAG", "Your location is: "+location)
            }*/
    }

    // When user first come to this activity we try to connect Google services for location and map related work
    @Synchronized
    protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
    }


    // Google Api Client is connected
    override fun onConnected(bundle: Bundle?) {
        if (mGoogleApiClient!!.isConnected) {
            //if connected successfully show user the settings dialog to enable location from settings services
            // If location services are enabled then get Location directly
            // Else show options for enable or disable location services
            settingsrequest()
        }
    }


    // This is the method that will be called if user has disabled the location services in the device settings
    // This will show a dialog asking user to enable location services or not
    // If user tap on "Yes" it will directly enable the services without taking user to the device settings
    // If user tap "No" it will just Finish the current Activity
    fun settingsrequest() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = (30 * 1000).toLong()
        locationRequest.fastestInterval = (5 * 1000).toLong()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true) //this is the key ingredient
        val result =
            LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build())
        result.setResultCallback { result ->
            val status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> if (mGoogleApiClient!!.isConnected) {

                    // check if the device has OS Marshmellow or greater than
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                        if (ActivityCompat.checkSelfPermission(
                                this@FirstChallengeActivity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                this@FirstChallengeActivity,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                this@FirstChallengeActivity,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                ACCESS_FINE_LOCATION_CODE
                            )
                        } else {
                            // get Location
                        }
                    } else {
                        // get Location
                    }
                }
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->                         // Location settings are not satisfied. But could be fixed by showing the user
                    // a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        status.startResolutionForResult(
                            this@FirstChallengeActivity,
                            REQUEST_RESOLVE_ERROR
                        )
                    } catch (e: SendIntentException) {
                        // Ignore the error.
                    }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                }
            }
        }
    }


    // This method is called only on devices having installed Android version >= M (Marshmellow)
    // This method is just to show the user options for allow or deny location services at runtime
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            3310 -> {
                if (grantResults.size > 0) {
                    var i = 0
                    val len = permissions.size
                    while (i < len) {
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            // Show the user a dialog why you need location
                        } else if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            // verify that location is accurate
                            Log.d("MSG", "Current Location: "+PackageManager.FEATURE_LOCATION_GPS)
                            locationVerifier()
                        } else {
                            finish()
                        }
                        i++
                    }
                }
                return
            }
        }
    }

    private fun locationVerifier() {
        PackageManager.FEATURE_LOCATION_GPS
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false
            when (resultCode) {
                RESULT_OK -> {
                }
                RESULT_CANCELED -> finish()
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.d("TAG", "Checking if: "+location.latitude+" is less than: "+CHALLENGE_LAT_MAX+", Which is: ")
        if(location.latitude < CHALLENGE_LAT_MAX && location.latitude > CHALLENGE_LAT_MIN){
            //The location is within the expected range, so display success, write to DB
            firebaseFirestore = Firebase.firestore
            firebaseAuth = FirebaseAuth.getInstance()
            val firebaseUser = firebaseAuth.currentUser
            //get user info
            val uid = firebaseUser!!.uid
            val name = firebaseUser!!.displayName
            val email = firebaseUser!!.email
            val userDocRef = firebaseFirestore.collection("challenges")
                .document(CHALLENGE_NAME)
                .collection("UsersCompleted")
                .document(uid)
            var userRef = firebaseFirestore.collection("users")
                .document(uid)
                .collection("CompletedChallenges")
                .document(CHALLENGE_NAME)

            tvMessage = findViewById(R.id.succMessage)

            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                        //Not first time completing challenge, so first check that hasn't been done in past day
                        //Read in last time completed from time field
                        val lastTime: Long = document.get("time") as Long
                        val relativeStreak = document.get("relativestreak")
                        //If at least 1 day has gone by since the last time completed, then:
                        if(System.currentTimeMillis() >= startOfTomorrow(lastTime)) {
                            userRef.update("pointsfrom", FieldValue.increment(REGULAR_PTS))
                            userRef.update("timescompleted", FieldValue.increment(1))
                            //Only increment the streak if 2 days haven't gone by
                            if (System.currentTimeMillis() - (lastTime as Long) <= 2 * DAY_IN_UNIX_MS) {
                                userRef.update("relativestreak", FieldValue.increment(1))
                                //If reached a streak of 5, aka done for whole week, message + reset
                                if (relativeStreak == 5) {
                                    userRef.update("fiveinarow", FieldValue.increment(1))
                                    userRef.update("relativestreak", FieldValue.increment(-5))
                                    tvMessage.text =
                                        "You're amazing! You completed " + CHALLENGE_NAME + " all 5 days in a row!\nStreak reset."
                                } else {
                                    tvMessage.text =
                                        "Well done! You completed " + CHALLENGE_NAME + " " + relativeStreak + " days in a row!"
                                }
                            } else {

                            }
                        } else {
                            tvMessage.text = "Uh-Oh! Challenges can only be completed once per day! Come back tomorrow :)"
                        }
                        //Access current points and increment:
                        //Atomically increment the points by REGULAR_PTS.

                    } else {
                        Log.d(TAG, "No such document")
                        //first time completing, so:
                        val map = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "uid" to uid,
                            "time" to System.currentTimeMillis()
                        )

                        val map2 = hashMapOf(
                            "pointsfrom" to FIRST_TIME_PTS,
                            "relativestreak" to 1,
                            "timescompleted" to 1,
                            "fiveinarow" to 0
                        )

                        firebaseFirestore.collection("challenges")
                            .document(CHALLENGE_NAME)
                            .collection("UsersCompleted")
                            .document(uid)
                            .set(map)

                        firebaseFirestore.collection("users")
                            .document(uid)
                            .collection("CompletedChallenges")
                            .document(CHALLENGE_NAME)
                            .set(map2)

                        tvMessage.text = "Success! You completed " + CHALLENGE_NAME + " and received points."
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }
        tvGpsLocation = findViewById(R.id.yourLocation)
        //tvGpsLocation.text = "Latitude: " + location.latitude + " , Longitude: " + location.longitude
    }


    override fun onConnectionSuspended(i: Int) {}


    // When there is an error connecting Google Services
    override fun onConnectionFailed(result: ConnectionResult) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR)
            } catch (e: SendIntentException) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient!!.connect()
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(result.errorCode)
            mResolvingError = true
        }
    }

    fun millisSoFar(): Long{
        return System.currentTimeMillis() % DAY_IN_UNIX_MS
    }

    fun millisUntilTomorrow(): Long{
        return (DAY_IN_UNIX_MS - millisSoFar()) + TIME_ZONE_BIAS
    }

    fun endOfTomorrow(reference: Long): Long{
        return startOfTomorrow(reference) + DAY_IN_UNIX_MS
    }

    fun startOfTomorrow(reference: Long): Long{
        return reference + millisUntilTomorrow()
    }

    /* Creates a dialog for an error message */
    private fun showErrorDialog(errorCode: Int) {
        // Create a fragment for the error dialog
        //val dialogFragment = ErrorDialogFragment()
        // Pass the error that should be displayed
        val args = Bundle()
        args.putInt(DIALOG_ERROR, errorCode)
        //dialogFragment.setArguments(args)
        //dialogFragment.show(supportFragmentManager, "errordialog")
        Log.d("TAG", "Erro mutherfuckerrrrrrrrr")
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    fun onDialogDismissed() {
        mResolvingError = false
    }

    /* A fragment to display an error dialog
    class ErrorDialogFragment : DialogFragment() {
        fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Get the error code and retrieve the appropriate dialog
            val errorCode: Int = this.getArguments().getInt(DIALOG_ERROR)
            return GoogleApiAvailability.getInstance().getErrorDialog(
                this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR
            )
        }

        fun onDismiss(dialog: DialogInterface?) {
            (getActivity() as LocationActivity).onDialogDismissed()
        }
    }*/


    // Connect Google Api Client if it is not connected already
    override fun onStart() {
        super.onStart()
        mGoogleApiClient?.connect()
    }

    // Stop the service when we are leaving this activity
    override fun onStop() {
        super.onStop()
        mGoogleApiClient?.disconnect()
    }

}