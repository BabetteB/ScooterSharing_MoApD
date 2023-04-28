package dk.itu.moapd.scootersharing.babb.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import dk.itu.moapd.scootersharing.babb.databinding.FragmentScooterBinding
import dk.itu.moapd.scootersharing.babb.model.Scooter
import dk.itu.moapd.scootersharing.babb.model.ScooterViewModel
import java.time.Duration
import java.util.*
import kotlin.collections.HashMap

class ScooterFragment : Fragment() {
    private val vm : ScooterViewModel by activityViewModels()

    private lateinit var auth : FirebaseAuth
    private lateinit var database : DatabaseReference
    private val TAG = "ScooterFragment"

    private val args : ScooterFragmentArgs by navArgs()
    private var scooter : Scooter? = null

    private var unlocked = false;


    private var _binding : FragmentScooterBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Oh no I died"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = vm.getDB()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScooterBinding.inflate(layoutInflater, container, false)

        var scooterId : String? = ""
        try {
            scooterId = args.scooterID
        } catch (e : java.lang.Exception) {
            Log.d(TAG, "ScooterID : $scooterId")
        }



        with (binding) {
            if (scooterId == "" || scooterId.isNullOrBlank()) {
                scooterFragmentTitle.text = "No scooter ride in progress"

                activeScooterName.text = "Please start a ride before information can be shown."
                activeScooterPrice.visibility = View.INVISIBLE
                activeScooterTime.visibility = View.INVISIBLE
                activeScooterUnlock.visibility = View.INVISIBLE
                activeScooterButtonPause.visibility = View.INVISIBLE
                activeScooterButtonEnd.visibility= View.INVISIBLE
                scooterFragmentProgressBar.visibility = View.INVISIBLE

            } else {
                database.child("scooters").child(scooterId).get().addOnSuccessListener {
                    var m = it.value as HashMap<String, String>
                    Log.d(TAG, "m : $m")
                    scooter?.name = m.getValue("name")
                    Log.d(TAG, "scootername : ${scooter?.name}")
                }.addOnFailureListener{
                    Log.e("firebase", "Error getting data", it)
                    scooter = null
                }

                activeScooterTime.stop()
                activeScooterButtonEnd.isEnabled = false
                activeScooterButtonPause.isEnabled = false

                activeScooterName.text = scooter?.name
                activeScooterPrice.text = "100 DKK"



            }
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        with (binding) {
            activeScooterUnlock.setOnClickListener {
                unlocked = true
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

                endRide(vm.getUserScooter()!!);

            }
        }
    }


    override fun onPause() {
        super.onPause()
        if (vm.getUserScooter()?.startRideTime != null){
        }

        if (!unlocked) {
            vm.removeUserScooter()
        }
    }


    fun endRide(scooter: Scooter) {
        auth.currentUser?.let { user ->
            database.child("history")
                .child(scooter.id.toString())
                .setValue(scooter)
                .addOnSuccessListener {
                    Toast.makeText(
                        binding.root.context,
                        "Scooter ride ended",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        binding.root.context,
                        "An error occurred. Scooter ride not ended.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }


}

