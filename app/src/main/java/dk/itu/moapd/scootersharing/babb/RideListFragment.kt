package dk.itu.moapd.scootersharing.babb

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.babb.databinding.FragmentRideListBinding
import dk.itu.moapd.scootersharing.babb.model.*
import dk.itu.moapd.scootersharing.babb.viewmodel.StartRideFragment
import dk.itu.moapd.scootersharing.babb.viewmodel.UpdateRideFragment


class RideListFragment : Fragment(), ItemClickListener {
    private val vm : ScooterViewModel by activityViewModels()

    private lateinit var database: DatabaseReference
    private lateinit var auth : FirebaseAuth



    companion object{
        private lateinit var adapter : CustomAdapter
        private lateinit var DATABASE_URL: String
    }

    private var _binding: FragmentRideListBinding? = null
    private val binding
        get() = checkNotNull(_binding){
            "Cannot access binding."
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
        _binding = FragmentRideListBinding.inflate(inflater, container, false)
        binding.rideRecyclerView.layoutManager = LinearLayoutManager(context)

        with (binding) {
            floatingActionButton.setOnClickListener {
                findNavController().navigate(
                    RideListFragmentDirections.showStartRide()
                )
            }
        }

        // get all scooters and sort by location
        val query = database
            .child("scooters")

        val options =
            FirebaseRecyclerOptions.Builder<Scooter>()
                .setQuery(query, Scooter::class.java)
                .setLifecycleOwner(this)
                .build()

        adapter = CustomAdapter(this, options)

        updateBinding(adapter)
        return binding.root
    }



    private fun updateBinding(adapter : CustomAdapter) {
        binding.rideRecyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onRideClicked(scooterId: String, scooterName : String) {
        findNavController().navigate(
            RideListFragmentDirections.showUpdateRide(scooterId, scooterName)
        )
    }

    override fun onRideLongClicked(scooterId: String?) {
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