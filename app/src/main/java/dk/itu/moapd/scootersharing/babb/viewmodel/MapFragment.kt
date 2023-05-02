package dk.itu.moapd.scootersharing.babb.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.opengl.GLSurfaceView.Renderer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.babb.R
import dk.itu.moapd.scootersharing.babb.databinding.FragmentMapBinding
import dk.itu.moapd.scootersharing.babb.model.Scooter
import dk.itu.moapd.scootersharing.babb.model.ScooterLocation
import java.util.Objects


class MapFragment : Fragment()/*, OnMapsSdkInitializedCallback */{

    private lateinit var database: DatabaseReference
    private lateinit var auth : FirebaseAuth

    private lateinit var map: GoogleMap

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var lastKnownLocation: Location? = null


    private var _binding: FragmentMapBinding? = null
    private val binding
        get() = checkNotNull(_binding){
            "Cannot access binding."
        }

    companion object{
        private val TAG = MapFragment::class.java.simpleName

        private const val ALL_PERMISSIONS_RESULT = 1011
        private lateinit var DATABASE_URL: String

        private const val DEFAULT_ZOOM = 15
        //private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        // Keys for storing activity state.
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"

        // Used for selecting the current place.
        //private const val M_MAX_ENTRIES = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        DATABASE_URL = resources.getString(R.string.DATABASE_URL)
        database = Firebase.database(DATABASE_URL).reference

        //MapsInitializer.initialize(this.requireContext(), MapsInitializer.Renderer.LATEST, this)

        //requestUserPermissions()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        //val mapFragment = childFragmentManager.findFragmentById(R.id.google_maps) as SupportMapFragment?
        //mapFragment?.getMapAsync(callback)
        val mapFragment =


        return binding.root
    }


    /*
    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        getDeviceLocation()

        map = googleMap

        map.mapType = GoogleMap.MAP_TYPE_HYBRID

        // Setup the UI settings state.
        map.uiSettings.apply {
            isCompassEnabled = true
            isIndoorLevelPickerEnabled = true

            isRotateGesturesEnabled = true
            isScrollGesturesEnabled = true
            isTiltGesturesEnabled = true
            isZoomControlsEnabled = true
            isZoomGesturesEnabled = true
        }
        // Move the Google Maps UI buttons under the OS top bar.
        map.setPadding(0, 100, 0, 0)

        database.child("scooters").get().addOnSuccessListener {
            it.children.forEach { s ->
                val scot = s.getValue<Scooter>()!!
                val marker = googleMap.addMarker(
                    MarkerOptions()
                        .title(scot.name)
                        .position(LatLng(scot.locationLat!!, scot.locationLng!!))
                )
            }
        }

        if (checkPermission()) {
            map.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true
        }


    }


    private fun requestUserPermissions() {

        // An array with location-aware permissions.
        val permissions: ArrayList<String> = ArrayList()
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        // Check which permissions is needed to ask to the user.
        val permissionsToRequest = permissionsToRequest(permissions)

        // Show the permissions dialogs to the user.
        if (permissionsToRequest.size > 0)
            requestPermissions(
                permissionsToRequest.toTypedArray(),
                ALL_PERMISSIONS_RESULT
            )
    }

    private fun permissionsToRequest(permissions: ArrayList<String>): ArrayList<String> {
        val result: ArrayList<String> = ArrayList()
        for (permission in permissions)
            if (activity?.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                result.add(permission)
        return result
    }

    private fun checkPermission() : Boolean =
        ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED

    private fun getDeviceLocation() {
        try {
            if (checkPermission()) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        map.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        map.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    override fun onMapsSdkInitialized(p0: MapsInitializer.Renderer) {
        when (p0) {
            MapsInitializer.Renderer.LATEST -> Log.d(TAG, "Map init renderer is LATEST")
            else -> { Log.d(TAG, "Map init renderer is not LATEST")}
        }
    }

     */


}


