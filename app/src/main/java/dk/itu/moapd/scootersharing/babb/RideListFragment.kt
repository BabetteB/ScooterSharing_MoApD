package dk.itu.moapd.scootersharing.babb

import android.os.Bundle
import android.util.Log
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.babb.databinding.FragmentRideListBinding
import dk.itu.moapd.scootersharing.babb.model.*
import dk.itu.moapd.scootersharing.babb.viewmodel.StartRideFragment
import dk.itu.moapd.scootersharing.babb.viewmodel.UpdateRideFragment


class RideListFragment : Fragment(), ItemClickListener {

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
        fun createMockScooters(): List<Scooter> {
            val scooter1 = Scooter(
                id = "scooter1",
                name = "Scooter A",
                locationLat = 37.7749,
                locationLng = -122.4194,
                reserved = false,
                createdAt = System.currentTimeMillis(),
                assignedToUserID = "user123",
                imageUri = "https://example.com/scooter1.jpg"
            )

            val scooter2 = Scooter(
                id = "scooter2",
                name = "Scooter B",
                locationLat = 34.0522,
                locationLng = -118.2437,
                reserved = true,
                createdAt = System.currentTimeMillis(),
                assignedToUserID = "user456",
                imageUri = "https://example.com/scooter2.jpg"
            )

            return listOf(scooter1, scooter2)
        }

        fun saveMockScootersToFirebase() {
            val mockScooters = createMockScooters()

            // Save each scooter under its ID as a separate child
            for (scooter in mockScooters) {
                scooter.id?.let {
                    database.child(it).setValue(scooter)
                        .addOnSuccessListener {
                            Log.d("Firebase", "Scooter $it added successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firebase", "Error adding scooter", e)
                        }
                }
            }
        }
saveMockScootersToFirebase()
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
            .child("scooter")

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


}