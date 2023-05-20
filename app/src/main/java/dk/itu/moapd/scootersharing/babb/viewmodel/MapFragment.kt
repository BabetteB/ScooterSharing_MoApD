package dk.itu.moapd.scootersharing.babb.viewmodel

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.babb.model.NoParkingZones
import dk.itu.moapd.scootersharing.babb.databinding.FragmentMapBinding
import dk.itu.moapd.scootersharing.babb.model.Scooter
import dk.itu.moapd.scootersharing.babb.R

import java.util.*


class MapFragment : Fragment(), OnMapReadyCallback  {

    private val REQUEST_LOCATION_PERMISSION = 1001

    private lateinit var database: DatabaseReference
    private lateinit var auth : FirebaseAuth

    private lateinit var map: GoogleMap
    private var cameraPosition: CameraPosition? = null

    private val defaultLocation = LatLng(55.69518532166335, 12.550138887442337)
    private var lastKnownLocation: Location? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    private var _binding: FragmentMapBinding? = null
    private val binding
        get() = checkNotNull(_binding){
            "Cannot access binding."
        }

    companion object{
        private val TAG = MapFragment::class.java.simpleName

        private lateinit var DATABASE_URL: String

        private const val DEFAULT_ZOOM = 15

        // Keys for storing activity state.
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        DATABASE_URL = resources.getString(R.string.DATABASE_URL)
        database = Firebase.database(DATABASE_URL).reference

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.markerScooterInfo.visibility = View.INVISIBLE

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        map?.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()

        map.addMarker(MarkerOptions().position(defaultLocation))

        setMapLongClick(map)
        setPoiClick(map)

        map.setOnMarkerClickListener(OnMarkerClickListener { marker ->
            map.animateCamera(CameraUpdateFactory.newLatLng(marker.position))
            if (binding.markerScooterInfo.getVisibility() === View.VISIBLE)
                binding.markerScooterInfo.setVisibility(
                    View.GONE
                ) else showInfoWindow(marker)
            true
        })

        NoParkingZones.addDefaultNoParkingZones(map)

        database.child("scooters").get().addOnSuccessListener {
            it.children.forEach { s ->
                val scot = s.getValue<Scooter>()!!
                val marker = map.addMarker(
                    if (scot.reserved == true){
                        MarkerOptions()
                            .title(scot.name)
                            .position(LatLng(scot.locationLat!!, scot.locationLng!!))
                        //todo : disable click
                    } else {
                        MarkerOptions()
                            .title(scot.name)
                            .position(LatLng(scot.locationLat!!, scot.locationLng!!))
                    }
                )
            }
        }
    }

    private fun showInfoWindow(marker : Marker) {
        binding.markerScooterInfo.setVisibility(View.VISIBLE)

        val title: TextView = binding.scooterTitle
        val location: TextView = binding.scooterLocation

        title.text = marker.title
        location.text = "Location: " + marker.position.latitude + "," + marker.position.longitude
        binding.btnScooterStartRide.setOnClickListener {
            findNavController().navigate(
                MapFragmentDirections.showQrscan()
            )
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            val lat = latLng.latitude
            val lng = latLng.longitude
            if (NoParkingZones.isLatLngWithinPolygon(latLng)){
                cannotCreateRideDialog()
            } else {
                createRideDialog(lat, lng)
            }
        }
    }

    private fun cannotCreateRideDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cannot create a new scooter in this area")
            .setMessage("This is a no parking zone, hence it is not possible to add a scooter to this area")
            .setNeutralButton("Ok") { dialog, which ->
                // Respond to neutral button press
            }.show()
    }

    private fun createRideDialog(lat : Double, lng : Double) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create a new scooter?")
            .setMessage("Create a new scooter at location: $lat, $lng")
            .setNeutralButton("Cancel") { dialog, which ->
                // Respond to neutral button press
            }
            .setPositiveButton("Create new scooter") { dialog, which ->
                auth.currentUser?.let {
                    findNavController().navigate(
                        MapFragmentDirections.addScooterAtLocation(lat.toFloat(), lng.toFloat())
                    )
                }
            }.show()
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )?.showInfoWindow()
        }
    }

    private fun checkPermission() =
        ActivityCompat.checkSelfPermission(
            this.requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this.requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

    private fun enableMyLocation() {
        if (checkPermission()) {
            updateLocationUI()
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(this.requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Set the map's camera position to the current location of the device.
                    lastKnownLocation = task.result
                    if (lastKnownLocation != null) {
                        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(lastKnownLocation!!.latitude,
                                lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
                    }
                } else {
                    Log.d(TAG, "Current location is null. Using defaults.")
                    Log.e(TAG, "Exception: %s", task.exception)
                    map?.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                    map?.uiSettings?.isMyLocationButtonEnabled = false
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this.requireActivity(),
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        try {
            map?.isMyLocationEnabled = true
            map?.uiSettings?.isMyLocationButtonEnabled = true
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }




}


