package dk.itu.moapd.scootersharing.babb.viewmodel


import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dk.itu.moapd.scootersharing.babb.R
import dk.itu.moapd.scootersharing.babb.databinding.FragmentScooterBinding
import dk.itu.moapd.scootersharing.babb.model.Scooter
import java.io.File
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class ScooterFragment : Fragment() {

    val REQUEST_IMAGE_CAPTURE = 1069

    private lateinit var auth : FirebaseAuth
    private lateinit var database : DatabaseReference
    private lateinit var storage : FirebaseStorage

    private lateinit var outPutDirectory: File
    private lateinit var fileProvider: FileProvider

    private val args : ScooterFragmentArgs? by navArgs()
    private var scooterID : String? = ""
    private var timerStarted = false


    private var handler = Handler()

    private var sensorManager: SensorManager? = null

    private var appliedAcceleration = 0f
    private var currentAcceleration = 0f
    private var velocity = 0f
    private var lastUpdate: Date? = null

    private var unlocked = false


    private var _binding : FragmentScooterBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Oh no I died"
        }

    companion object {
        private lateinit var DATABASE_URL: String
        private const val TAG = "ScooterFragment"
        private lateinit var BUCKET_URL : String
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        DATABASE_URL = resources.getString(R.string.DATABASE_URL)
        database = Firebase.database(DATABASE_URL).reference
        Log.d(TAG, "fragment created")

        //lastUpdate = Date(System.currentTimeMillis())

        BUCKET_URL = resources.getString(R.string.BUCKET_URL)
        storage = Firebase.storage(BUCKET_URL)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScooterBinding.inflate(layoutInflater, container, false)
        scooterID = args?.sid

        with (binding) {
            if (scooterID.isNullOrBlank()) {
                scooterFragmentTitle.text = "No scooter ride in progress"

                activeScooterName.text = "Please start a ride before information can be shown."
                activeScooterSpeed.visibility = View.INVISIBLE
                activeScooterTime.visibility = View.INVISIBLE
                activeScooterUnlock.visibility = View.INVISIBLE
                activeScooterButtonPause.visibility = View.INVISIBLE
                activeScooterButtonEnd.visibility= View.INVISIBLE
                scooterFragmentProgressBar.visibility = View.INVISIBLE

            } else {
                lastUpdate = Calendar.getInstance().time
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
                endRide()
            }
        }
    }

    private fun tryFindScooter(id : String?) {
        if (id != null) {
            database.child("scooters").child(id).get()
                .addOnSuccessListener {d ->
                    val m = d.value as Map<String, Any>
                    val name = m["name"] as String?
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
        auth.currentUser?.let { _ ->
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
                        shortToast("Scooter reserved = ${reserved}!")
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
                uploadRidetoDB(user)
                findNavController().navigate(
                    ScooterFragmentDirections.takePictureEndRide(it)
                )
            }
        }
    }

    private fun uploadRidetoDB(user : FirebaseUser) {
        database.child("scooters").child(scooterID!!).get().addOnSuccessListener {
            val m = it.value as Map<String, Any>

            val s = Scooter(
                id = m["id"] as String?,
                name = m["name"] as String?,
                locationLat = m["locationLat"] as Double?,
                locationLng = m["locationLng"] as Double?,
                reserved = false,
                createdAt = m["createdAt"] as Long?,
                lastUpdateTimeStamp = Calendar.getInstance().time,
                assignedToUserID = null
            )

            database.child("history")
                .child(user.uid)
                .child(scooterID!!)
                .setValue(s)
                .addOnSuccessListener {
                    shortToast("Ride finished")
                }
                .addOnFailureListener {
                    shortToast("An error occurred. Ride still active!")
                }

        }
    }

    private fun shortToast(text : String) {
        Toast.makeText(
            binding.root.context,
            text,
            Toast.LENGTH_SHORT
        ).show()
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


    @SuppressLint("SetTextI18n")
    private fun updateGUI() {
        val kmh = ((velocity * 3600).roundToInt() / 1000).toDouble()

        // Update the GUI
        handler.post { binding.activeScooterSpeed.text = "Speed ${kmh}kmh" }
    }

    private fun updateVelocity() {
        // Calculate how long this acceleration has been applied.
        //Log.d(TAG, "lastUpdate: ${lastUpdate?.time}")
        val timeNow = Date(System.currentTimeMillis())
        val timeDelta = timeNow.time - lastUpdate!!.time
        lastUpdate!!.time = timeNow.time
        //Log.d(TAG, "time delta: $timeDelta")
        // Calculate the change in velocity at the
        // current acceleration since the last update.
        val deltaVelocity = appliedAcceleration * (timeDelta / 1000)
        appliedAcceleration = currentAcceleration

        // Add the velocity change to the current velocity.
        velocity += deltaVelocity

        //Log.d(TAG, "Update velocity : $velocity")
        //binding.activeScooterSpeed.text = "Speed : ${velocity} mph"
    }

    private val sensorEventListener: SensorEventListener = object : SensorEventListener {
        var calibration = Double.NaN

        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0].toDouble()
            val y = event.values[1].toDouble()
            val z = event.values[2].toDouble()

            val a = sqrt(x.pow(2.0) + y.pow(2.0) + z.pow(2.0))

            if (calibration === Double.NaN) calibration = a else {
                updateVelocity()
                currentAcceleration = a.toFloat()
            }

        }


        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

}

