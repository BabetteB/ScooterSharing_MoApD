package dk.itu.moapd.scootersharing.babb.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.babb.R
import dk.itu.moapd.scootersharing.babb.databinding.FragmentScooterBinding
import java.lang.Float.max
import java.lang.Float.min
import java.util.*


class ScooterFragment : Fragment() {

    private lateinit var auth : FirebaseAuth
    private lateinit var database : DatabaseReference
    private val TAG = "ScooterFragment"

    private val args : ScooterFragmentArgs? by navArgs()
    private var scooterID : String? = ""
    private var timerStarted = false


    private var handler = Handler()

    private var sensorManager: SensorManager? = null

    private var appliedAcceleration = 0f
    private var currentAcceleration = 0f
    private var velocity = 0f
    private var lastUpdate: Date? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var time = 0.0

    private var unlocked = false;


    private var _binding : FragmentScooterBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Oh no I died"
        }

    companion object {
        private lateinit var DATABASE_URL: String
        private val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        DATABASE_URL = resources.getString(R.string.DATABASE_URL)
        database = Firebase.database(DATABASE_URL).reference
        Log.d(TAG, "fragment created")

        lastUpdate = Date(System.currentTimeMillis())


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScooterBinding.inflate(layoutInflater, container, false)
        scooterID = args?.scooterID

        with (binding) {
            if (scooterID == null) {
                scooterFragmentTitle.text = "No scooter ride in progress"

                activeScooterName.text = "Please start a ride before information can be shown."
                activeScooterSpeed.visibility = View.INVISIBLE
                activeScooterTime.visibility = View.INVISIBLE
                activeScooterUnlock.visibility = View.INVISIBLE
                activeScooterButtonPause.visibility = View.INVISIBLE
                activeScooterButtonEnd.visibility= View.INVISIBLE
                scooterFragmentProgressBar.visibility = View.INVISIBLE

            } else {

                tryFindScooter(scooterID)

                sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager?
                val accelerometer: Sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

                sensorManager!!.registerListener(
                    sensorEventListener,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_FASTEST
                )

                val updateTimer = Timer("velocityUpdate")
                updateTimer.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        updateGUI()
                    }
                }, 0, 1000)

            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with (binding) {
            activeScooterUnlock.setOnClickListener {
                startRide()
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

    private fun tryFindScooter(id : String?) {
        if (id != null) {
            database.child("scooters").child(id).get()
                .addOnSuccessListener {d ->
                    val m = d.getValue() as Map<String, Object>

                    val name = m.get("name") as String?
                    enableActiveScooterFields(name)
                    setReserveScooter(true)


                }.addOnFailureListener {
                    Log.d(TAG, "could not get scooter from db")
                }
        } else {
            Log.d(TAG, "scooter id is null")
        }
    }

    private fun setReserveScooter(reserved : Boolean) {
        auth.currentUser?.let { user ->
            scooterID?.let {
                database.child("scooters")
                    .child(scooterID!!)
                    .child("reserved")
                    .setValue(
                        reserved
                    )
                    .addOnSuccessListener {
                        Log.d(TAG, "Scooter reserved")
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            binding.root.context,
                            "An error occurred. Scooter is not reserved!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    private fun enableActiveScooterFields(scooterName : String?) {
        binding.activeScooterName.text = scooterName

        binding.activeScooterButtonEnd.isEnabled = false
        binding.activeScooterButtonPause.isEnabled = false

    }



    private fun endRide() {
        setReserveScooter(false)
        auth.currentUser?.let { user ->
            scooterID?.let {
                database.child("history")
                    .child(user.uid)
                    .setValue(it)
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
    }

    private fun startRide() {
        unlocked = true
        timerStarted = true

        binding.apply {
            activeScooterUnlock.isEnabled = false
            activeScooterButtonEnd.isEnabled = true
            activeScooterButtonPause.isEnabled = true
        }

        Toast.makeText(
            this.requireContext(),
            "Ride started",
            Toast.LENGTH_SHORT
        ).show()
    }


    private fun updateGUI() {
        // Convert from meters per second to miles per hour.
        val mph = (Math.round(velocity * 3600) / 1000).toDouble()

        // Update the GUI
        handler.post { binding.activeScooterSpeed.text = velocity.toString() + "mph" }
    }

    private fun updateVelocity() {
        // Calculate how long this acceleration has been applied.
        Log.d(TAG, "lastUpdate: ${lastUpdate?.time}")
        val timeNow = Date(System.currentTimeMillis())
        val timeDelta = timeNow.time - lastUpdate!!.time
        lastUpdate!!.time = timeNow.time
        Log.d(TAG, "timedelta: ${timeDelta}")
        // Calculate the change in velocity at the
        // current acceleration since the last update.
        val deltaVelocity = appliedAcceleration * (timeDelta / 1000)
        appliedAcceleration = currentAcceleration

        // Add the velocity change to the current velocity.
        velocity += deltaVelocity

        Log.d(TAG, "Updatevelocity : ${velocity}")
        //binding.activeScooterSpeed.text = "Speed : ${velocity} mph"
    }

    private val sensorEventListener: SensorEventListener = object : SensorEventListener {
        var calibration = Double.NaN

        override fun onSensorChanged(event: SensorEvent) {
            var x = event.values[0].toDouble()
            var y = event.values[1].toDouble()
            var z = event.values[2].toDouble()

            var a = Math.sqrt(Math.pow(x, 2.0) + Math.pow(y, 2.0) + Math.pow(z, 2.0))

            if (calibration === Double.NaN) calibration = a else {
                updateVelocity()
                currentAcceleration = a.toFloat()
            }

        }


        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

}

