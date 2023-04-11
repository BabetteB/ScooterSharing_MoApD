package dk.itu.moapd.scootersharing.babb.viewmodel

import android.graphics.Color
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.babb.R
import dk.itu.moapd.scootersharing.babb.databinding.FragmentUpdateRideBinding
import dk.itu.moapd.scootersharing.babb.model.Scooter
import java.util.*

class UpdateRideFragment : Fragment() {

    private lateinit var auth : FirebaseAuth
    private lateinit var database : DatabaseReference

    private var _binding : FragmentUpdateRideBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Oh no I died"
        }

    private val args : UpdateRideFragmentArgs by navArgs()

    companion object{
        private val TAG = UpdateRideFragment::class.qualifiedName
        private lateinit var DATABASE_URL: String
        const val REQUEST_KEY_UPDATED_SCOOTER_LOCATION = "REQUEST_KEY_UPDATED_SCOOTER_LOCATION"
        //const val BUNDLE_KEY_UPDATED_SCOOTER_LOCATION = "BUNDLE_KEY_UPDATED_SCOOTER_LOCATION"
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
    ): View? {
        _binding = FragmentUpdateRideBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        with(binding) {
            informationInput.nameInput.apply {
                setText("wait")
                setTextColor(Color.parseColor("#9c9c9c"))
                isEnabled = false
            }

            buttonUpdateRide.setOnClickListener {
                val newLocation = checkNotNull(informationInput.locationInput.text.toString().trim())

                updateLocation(newLocation)

                setFragmentResult(
                    REQUEST_KEY_UPDATED_SCOOTER_LOCATION, bundleOf(
                        //BUNDLE_KEY_UPDATED_SCOOTER_LOCATION to Scooter(UUID.randomUUID().toString(), args.rideName, newLocation)
                    )
                )
                navController.popBackStack()
            }

            buttonBack.setOnClickListener {
                navController.popBackStack()
            }

        }
    }


    private fun updateLocation(newLoc : String) {
        auth.currentUser?.let { user ->
            database.child("scooters")
                .child("id")
                .child("location")
                .setValue(newLoc)
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