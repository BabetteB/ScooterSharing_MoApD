package dk.itu.moapd.scootersharing.babb.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.registerReceiver
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.babb.R
import dk.itu.moapd.scootersharing.babb.databinding.FragmentScooterBinding
import dk.itu.moapd.scootersharing.babb.model.Scooter
import dk.itu.moapd.scootersharing.babb.model.ScooterViewModel
import java.time.Duration
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.roundToInt

class ScooterFragment : Fragment() {

    private lateinit var auth : FirebaseAuth
    private lateinit var database : DatabaseReference
    private val TAG = "ScooterFragment"

    private val args : ScooterFragmentArgs by navArgs()
    private var scooter : Scooter? = null
    private var timerStarted = false
    private lateinit var serviceIntent: Intent
    private var time = 0.0

    private var unlocked = false;


    private var _binding : FragmentScooterBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Oh no I died"
        }

    companion object {
        private lateinit var DATABASE_URL: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        DATABASE_URL = resources.getString(R.string.DATABASE_URL)
        database = Firebase.database(DATABASE_URL).reference
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScooterBinding.inflate(layoutInflater, container, false)
        scooter = args.scooter

        with (binding) {
            if (scooter == null) {
                scooterFragmentTitle.text = "No scooter ride in progress"

                activeScooterName.text = "Please start a ride before information can be shown."
                activeScooterPrice.visibility = View.INVISIBLE
                activeScooterTime.visibility = View.INVISIBLE
                activeScooterUnlock.visibility = View.INVISIBLE
                activeScooterButtonPause.visibility = View.INVISIBLE
                activeScooterButtonEnd.visibility= View.INVISIBLE
                scooterFragmentProgressBar.visibility = View.INVISIBLE

            } else {
                Log.d(TAG, "Scooter : $scooter")

                activeScooterButtonEnd.isEnabled = false
                activeScooterButtonPause.isEnabled = false

                activeScooterPrice.text = "100 DKK"

            }
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with (binding) {
            activeScooterUnlock.setOnClickListener {
                startRide(binding)
            }

            activeScooterButtonPause.setOnClickListener {
            }

            activeScooterButtonEnd.setOnClickListener {
                activeScooterUnlock.isEnabled = true
                activeScooterButtonEnd.isEnabled = false
                activeScooterButtonPause.isEnabled = false
                endRide();
            }
        }
    }

    private fun endRide() {
        timerStarted = false

        auth.currentUser?.let { user ->
            database.child("history")
                .child(user.uid)
                .child(scooter?.id.toString())
                .setValue(scooter)
                .addOnSuccessListener {
                    Toast.makeText(
                        binding.root.context,
                        "Ride finished",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        binding.root.context,
                        "An error occurred. Ride still active!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun startRide(binding: FragmentScooterBinding) {
        unlocked = true
        timerStarted = true

        scooter?.apply {
            reserved = true
        }

        binding.apply {
            activeScooterUnlock.isEnabled = false
            activeScooterButtonEnd.isEnabled = true
            activeScooterButtonPause.isEnabled = true
        }
    }




}

