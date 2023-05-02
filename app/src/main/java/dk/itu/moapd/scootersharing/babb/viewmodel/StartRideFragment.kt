package dk.itu.moapd.scootersharing.babb.viewmodel

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.babb.R
import dk.itu.moapd.scootersharing.babb.databinding.FragmentStartRideBinding
import dk.itu.moapd.scootersharing.babb.model.Scooter
import dk.itu.moapd.scootersharing.babb.model.ScooterLocation
import java.util.*

class StartRideFragment : Fragment() {


    private lateinit var auth : FirebaseAuth
    private lateinit var database : DatabaseReference

    private val args : StartRideFragmentArgs by navArgs()

    private var _binding : FragmentStartRideBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Oh no I died"
        }

    companion object{
        private val TAG = StartRideFragment::class.qualifiedName
        private lateinit var DATABASE_URL: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        DATABASE_URL = resources.getString(R.string.DATABASE_URL)
        database = Firebase.database(DATABASE_URL).reference

        Log.d(TAG, "StartRideFragment created")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartRideBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        with(binding) {
            buttonStartRide.setOnClickListener {
                createNewScooter()
                navController.popBackStack()
            }

            buttonBack.setOnClickListener {
                navController.popBackStack()
            }
        }
    }

    private fun createNewScooter() {
        val name = binding.nameInput.text.toString().trim()
        val lLat = args.locationLat.toDouble()
        val lLng = args.locationLng.toDouble()

        val scooter = Scooter(UUID.randomUUID().toString(), name, lLat, lLng, false,0L, Calendar.getInstance().time)

        addNewScooterInDatabase(scooter)
    }

    private fun addNewScooterInDatabase(scooter: Scooter) {
        // In the case of authenticated user, create a new unique key for the object in the
        // database.
        auth.currentUser?.let { user ->
            database.child("scooters")
                .child(scooter.id.toString())
                .setValue(scooter)
                .addOnSuccessListener {
                    Toast.makeText(
                        binding.root.context,
                        "Scooter created",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        binding.root.context,
                        "An error occurred. Scooter not created",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }



}