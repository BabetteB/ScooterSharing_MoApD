package dk.itu.moapd.scootersharing.babb

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.babb.databinding.FragmentRideListBinding
import dk.itu.moapd.scootersharing.babb.model.*
import dk.itu.moapd.scootersharing.babb.viewmodel.MainActivity
import dk.itu.moapd.scootersharing.babb.viewmodel.StartRideFragment
import dk.itu.moapd.scootersharing.babb.viewmodel.UpdateRideFragment
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "RideListFragment"

class RideListFragment : Fragment(), ItemClickListener {

    private lateinit var adapter : CustomAdapter
    private lateinit var auth : FirebaseAuth
    private lateinit var database : DatabaseReference

    companion object{
        private lateinit var DATABASE_URL: String
    }

    private var _binding: FragmentRideListBinding? = null
    private val binding
        get() = checkNotNull(_binding){
            "Cannot access binding."
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DATABASE_URL = resources.getString(R.string.DATABASE_URL)

        auth = FirebaseAuth.getInstance()
        // Initialize Firebase database
        database = Firebase.database(DATABASE_URL).reference
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRideListBinding.inflate(inflater, container, false)
        binding.rideRecyclerView.layoutManager = LinearLayoutManager(context)

        val options =
            FirebaseRecyclerOptions.Builder<Scooter>()
                .setQuery(database.child("scooters"), Scooter::class.java)
                .build()

        adapter = CustomAdapter(this, options)

        updateBinding(adapter)

        return binding.root
    }

    private fun updateBinding(adapter : CustomAdapter) {
        binding.rideRecyclerView.adapter = adapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentResultListener(
            UpdateRideFragment.REQUEST_KEY_UPDATED_SCOOTER_LOCATION
        ) {
                _, bundle ->
            val newLocation = bundle.getSerializable(UpdateRideFragment.BUNDLE_KEY_UPDATED_SCOOTER_LOCATION) as Scooter
            //ridesDB.updateScooterLocation(newLocation.id, newLocation.location)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRideClicked(scooterId: String) {
        findNavController().navigate(
            RideListFragmentDirections.showUpdateRide(scooterId)
        )
    }

    override fun onRideLongClicked(scooterId: String) {
        /*ridesDB.deleteScooter(scooterId)
        Toast.makeText(
            context,
            "$scooterId deleted",
            Toast.LENGTH_SHORT
        ).show()

        var adapter = makeAdapter(ridesDB.getRidesList(), this)
        updateBinding(adapter)*/
    }

}