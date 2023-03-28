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
import dk.itu.moapd.scootersharing.babb.databinding.FragmentStartRideBinding
import dk.itu.moapd.scootersharing.babb.model.Scooter
import java.util.*

class StartRideFragment : Fragment() {

    private var _binding : FragmentStartRideBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Oh no I died"
        }

    companion object{
        private val TAG = StartRideFragment::class.qualifiedName
        const val REQUEST_KEY_NEW_SCOOTER = "REQUEST_KEY_NEW_SCOOTER"
        const val BUNDLE_KEY_NEW_SCOOTER = "BUNDLE_KEY_NEW_SCOOTER"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "fragment created")
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
                Toast.makeText(
                    binding.root.context,
                    "Scooter created",
                    Toast.LENGTH_SHORT
                ).show()
                setFragmentResult(REQUEST_KEY_NEW_SCOOTER, bundleOf(BUNDLE_KEY_NEW_SCOOTER to createNewScooter()))
                navController.popBackStack()
            }

            buttonBack.setOnClickListener {
                navController.popBackStack()
            }
        }
    }

    private fun createNewScooter() : Scooter {
        val name = binding.informationInput.nameInput.text.toString().trim()
        val location = binding.informationInput.locationInput.text.toString().trim()
        return Scooter(UUID.randomUUID(), name, location)
    }



}