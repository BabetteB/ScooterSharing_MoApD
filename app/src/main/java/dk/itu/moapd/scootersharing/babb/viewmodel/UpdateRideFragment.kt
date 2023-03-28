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
import dk.itu.moapd.scootersharing.babb.databinding.FragmentUpdateRideBinding
import dk.itu.moapd.scootersharing.babb.model.Scooter
import java.util.*

class UpdateRideFragment : Fragment() {

    private var _binding : FragmentUpdateRideBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Oh no I died"
        }

    private val args : UpdateRideFragmentArgs by navArgs()

    companion object{
        private val TAG = UpdateRideFragment::class.qualifiedName
        const val REQUEST_KEY_UPDATED_SCOOTER_LOCATION = "REQUEST_KEY_UPDATED_SCOOTER_LOCATION"
        const val BUNDLE_KEY_UPDATED_SCOOTER_LOCATION = "BUNDLE_KEY_UPDATED_SCOOTER_LOCATION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "The scooter name is ${args.rideName}")
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
                setText(args.rideName)
                setTextColor(Color.parseColor("#9c9c9c"))
                isEnabled = false
            }

            buttonUpdateRide.setOnClickListener {
                Toast.makeText(
                    binding.root.context,
                    "Location updated",
                    Toast.LENGTH_SHORT
                ).show()
                val newLocation = checkNotNull(informationInput.locationInput.text.toString().trim())
                setFragmentResult(
                    REQUEST_KEY_UPDATED_SCOOTER_LOCATION, bundleOf(
                        BUNDLE_KEY_UPDATED_SCOOTER_LOCATION to Scooter(1u, args.rideName, newLocation)
                    )
                )
                navController.popBackStack()
            }

            buttonBack.setOnClickListener {
                navController.popBackStack()
            }

        }
    }

}