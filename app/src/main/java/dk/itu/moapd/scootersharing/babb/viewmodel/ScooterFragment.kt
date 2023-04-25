package dk.itu.moapd.scootersharing.babb.viewmodel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dk.itu.moapd.scootersharing.babb.R
import dk.itu.moapd.scootersharing.babb.databinding.FragmentScooterBinding
import dk.itu.moapd.scootersharing.babb.databinding.FragmentStartRideBinding
import dk.itu.moapd.scootersharing.babb.model.Scooter
import dk.itu.moapd.scootersharing.babb.model.ScooterViewModel
import java.time.Duration
import java.util.*

class ScooterFragment : Fragment() {
    private val vm : ScooterViewModel by activityViewModels()

    private var _binding : FragmentScooterBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Oh no I died"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScooterBinding.inflate(layoutInflater, container, false)

        with (binding) {
            if (vm.getUserScooter() == null) {
                scooterFragmentTitle.text = "No scooter ride in progress"

                activeScooterName.text = "Please start a ride before information can be shown."
                activeScooterPrice.visibility = View.INVISIBLE
                activeScooterTime.visibility = View.INVISIBLE
                activeScooterButtonPause.visibility = View.INVISIBLE
                activeScooterButtonEnd.visibility= View.INVISIBLE
                scooterFragmentProgressBar.visibility = View.INVISIBLE

            } else {
                activeScooterTime.stop()
                activeScooterButtonEnd.isEnabled = false
                activeScooterButtonPause.isEnabled = false
                activeScooterName.text = vm.getUserScooter()!!.name
                activeScooterPrice.text = "100 DKK"



            }
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        with (binding) {
            activeScooterUnlock.setOnClickListener {
                vm.getUserScooter()!!.startRideTime = activeScooterTime
                vm.getUserScooter()?.startRideTime?.start()
                vm.getUserScooter()?.reserved = true
                activeScooterUnlock.isEnabled = false
                activeScooterButtonEnd.isEnabled = true
                activeScooterButtonPause.isEnabled = true
            }

            activeScooterButtonPause.setOnClickListener {
                vm.getUserScooter()?.startRideTime?.stop()
            }

            activeScooterButtonEnd.setOnClickListener {
                vm.getUserScooter()?.startRideTime?.stop()
            }
        }
    }


}

