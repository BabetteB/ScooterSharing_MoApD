package dk.itu.moapd.scootersharing.babb.viewmodel

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dk.itu.moapd.scootersharing.babb.R
import dk.itu.moapd.scootersharing.babb.databinding.FragmentMapBinding
import dk.itu.moapd.scootersharing.babb.databinding.FragmentRideListBinding

class MapFragment : OnMapReadyCallback, Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding
        get() = checkNotNull(_binding){
            "Cannot access binding."
        }

    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        //TODO : se https://github.com/fabricionarcizo/moapd2023/blob/main/lecture09/09-2_GoogleMaps/app/src/main/java/dk/itu/moapd/googlemaps/MainActivity.kt
        /*val mapFragment = supportFragmentManager
            .findFragmentById(R.id.google_maps) as SupportMapFragment
        mapFragment.getMapAsync(this)


        with (binding) {
            mapView.getMapAsync(this@MapFragment)
        }*/

        return binding.root
    }

    override fun onMapReady(map: GoogleMap) {
        map?.let {
            googleMap = it
        }
    }

}