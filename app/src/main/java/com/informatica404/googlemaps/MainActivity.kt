package com.informatica404.googlemaps

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(),OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    val PERMISSION_REQUEST_LOCATION = 1
    private var mFusedLocationClient: FusedLocationProviderClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map)
                as SupportMapFragment
        mapFragment.getMapAsync(this)

        checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val mondejar = LatLng(40.323, -3.11505)
        mMap.addMarker(MarkerOptions().position(mondejar).title("Marker in Mondejar"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mondejar))
    }


    private fun checkPermission(permission: String) {
        if (ActivityCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            locateUser()
        } else {
            requestPermission(permission)
        }
    }


    private fun requestPermission(permission: String) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                permission
            )) {
            showSnackbar(
                R.string.access_required,
                Snackbar.LENGTH_INDEFINITE, android.R.string.ok
            ) {
                startInstalledAppDetailsActivity()
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                PERMISSION_REQUEST_LOCATION
            )
        }
    }

    fun startInstalledAppDetailsActivity() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    fun showSnackbar(
        msg: Int,
        length: Int,
        actionMessage: Int? = null,
        action: ((View) -> Unit)? = null
    ) {
        val snackbar = Snackbar.make(container, msg, length)
        if (actionMessage != null) {
            snackbar.setAction(actionMessage) {
                action?.invoke(container)
            }
        }
        snackbar.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            // Request for camera permission.
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                locateUser()
            } else {
                showSnackbar(
                    R.string.access_required,
                    Snackbar.LENGTH_INDEFINITE, android.R.string.ok
                ) {
                    startInstalledAppDetailsActivity()
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    fun locateUser(){
        Log.i("Main Activity", "Todo Ok")

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mFusedLocationClient?.lastLocation?.addOnCompleteListener(this) { task ->

            if (task.isSuccessful && task.result != null) {
                Log.i("Main Activity", "Existe Localizacion")
                val mLastLocation = task.result
                positionInMap(mLastLocation)
            }
            Log.i("MapsActivity", "StartLocationUpdates")
            startLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.locations) {
                    positionInMap(location)
                    mFusedLocationClient?.removeLocationUpdates(this)
                }
            }
        }
        mFusedLocationClient?.requestLocationUpdates(
            createLocationRequest(),
            locationCallback, Looper.myLooper()
        );
    }

    private fun createLocationRequest(): LocationRequest? {
        return LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    @SuppressLint("MissingPermission")
    private fun positionInMap(mLastLocation: Location) {
        val myLocation = LatLng(mLastLocation.latitude, mLastLocation.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15f))
        mMap.isMyLocationEnabled = true
    }

}
