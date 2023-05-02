package dk.itu.moapd.scootersharing.babb.viewmodel

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.babb.databinding.FragmentMapBinding
import dk.itu.moapd.scootersharing.babb.model.Scooter
import dk.itu.moapd.scootersharing.babb.R

import java.util.*


class MapFragment : Fragment(), OnMapReadyCallback  {

    private val REQUEST_LOCATION_PERMISSION = 1

    private lateinit var database: DatabaseReference
    private lateinit var auth : FirebaseAuth

    private lateinit var map: GoogleMap

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


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        DATABASE_URL = resources.getString(R.string.DATABASE_URL)
        database = Firebase.database(DATABASE_URL).reference

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

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val lat = 55.69518532166335
        val lng = 12.550138887442337
        val defaultLatLng = LatLng(lat, lng)
        val zoomLevel = 15f

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, zoomLevel))

        map.addMarker(MarkerOptions().position(defaultLatLng))



        setMapLongClick(map)
        setPoiClick(map)
        enableMyLocation()

        map.setOnMarkerClickListener(OnMarkerClickListener { marker ->
            map.animateCamera(CameraUpdateFactory.newLatLng(marker.position))
            if (binding.markerScooterInfo.getVisibility() === View.VISIBLE)
                binding.markerScooterInfo.setVisibility(
                    View.GONE
                ) else showInfoWindow(marker)
            true
        })

        database.child("scooters").get().addOnSuccessListener {
            it.children.forEach { s ->
                val scot = s.getValue<Scooter>()!!
                val marker = map.addMarker(
                    MarkerOptions()
                        .title(scot.name)
                        .position(LatLng(scot.locationLat!!, scot.locationLng!!))

                )
            }
        }
    }

    private fun showInfoWindow(marker : Marker) {
        binding.markerScooterInfo.setVisibility(View.VISIBLE)

        val title: TextView = binding.scooterTitle
        val location: TextView = binding.scooterLocation
        val reserved : TextView = binding.scooterReserved

        title.text = marker.title
        location.text = "Location: " + marker.position.latitude + "," + marker.position.longitude

    }



    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            val lat = latLng.latitude
            val lng = latLng.longitude
            createRideDialog(lat, lng)
        }
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
            map.isMyLocationEnabled = true
        }
        else {
            ActivityCompat.requestPermissions(
                this.requireActivity(),
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
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


