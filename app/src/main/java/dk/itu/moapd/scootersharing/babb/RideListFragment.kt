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
import dk.itu.moapd.scootersharing.babb.databinding.FragmentRideListBinding
import dk.itu.moapd.scootersharing.babb.model.*
import dk.itu.moapd.scootersharing.babb.viewmodel.UpdateRideFragment


class RideListFragment : Fragment(), ItemClickListener {
    private val vm : ScooterViewModel by activityViewModels()

    private lateinit var database: DatabaseReference
    private lateinit var auth : FirebaseAuth



    companion object{
        private lateinit var adapter : CustomAdapter
    }

    private var _binding: FragmentRideListBinding? = null
    private val binding
        get() = checkNotNull(_binding){
            "Cannot access binding."
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = vm.getDB()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRideListBinding.inflate(inflater, container, false)
        binding.rideRecyclerView.layoutManager = LinearLayoutManager(context)

        // get all scooters and sort by location
        val query = database
            .child("scooters")
            //.child(scooter.id)
        //.orderByChild("location")

        val options =
            FirebaseRecyclerOptions.Builder<Scooter>()
                .setQuery(query, Scooter::class.java)
                //.setLifecycleOwner(this)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentResultListener(
            UpdateRideFragment.REQUEST_KEY_UPDATED_SCOOTER_LOCATION
        ) {
                _, _ ->
            //val newLocation = bundle.getSerializable(UpdateRideFragment.BUNDLE_KEY_UPDATED_SCOOTER_LOCATION) as Scooter
            //ridesDB.updateScooterLocation(newLocation.id, newLocation.location)
        }


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