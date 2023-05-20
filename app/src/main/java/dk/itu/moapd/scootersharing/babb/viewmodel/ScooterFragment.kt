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
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
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

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class ScooterFragment : Fragment(), SensorEventListener {

    private lateinit var auth : FirebaseAuth
    lateinit var database : DatabaseReference
    private lateinit var storage : FirebaseStorage

    private lateinit var locationManager: LocationManager

    private val args : ScooterFragmentArgs? by navArgs()
    private var scooterID : String? = ""
    private var timerStarted = false
    private var unlocked = false


    //------------------SPEED STUFF
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gravity: FloatArray = floatArrayOf(0f, 0f, 0f)
    //------------------


    private var _binding : FragmentScooterBinding? = null
    val binding
        get() = checkNotNull(_binding) {
            "Oh no I died"
        }

    companion object {
        private lateinit var DATABASE_URL: String
        private const val TAG = "ScooterFragment"
        private lateinit var BUCKET_URL : String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        DATABASE_URL = resources.getString(R.string.DATABASE_URL)
        database = Firebase.database(DATABASE_URL).reference

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
                sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
                // Get the accelerometer sensor
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

                //lastUpdate = Calendar.getInstance().time
                tryFindScooter(scooterID)

                val t = Timer()
                val tt: TimerTask = object : TimerTask() {
                    override fun run() {

                    }
                }
                t.scheduleAtFixedRate(tt, 0, 1000)

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

    override fun onResume() {
        super.onResume()
        accelerometer?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }


    fun tryFindScooter(id : String?) {
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
                .addOnFailureListener {e ->
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


    override fun onPause() {
        super.onPause()
        // Unregister sensor listeners
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // Calculate the speed using accelerometer data
            val speed = calculateSpeed(event.values[0], event.values[1], event.values[2])
            binding.activeScooterSpeed.text = String.format("%.2f", speed)
        }
    }

    private fun calculateSpeed(x: Float, y: Float, z: Float): Float {
        // Update gravity values using a low-pass filter
        val alpha = 0.8f
        gravity[0] = alpha * gravity[0] + (1 - alpha) * x
        gravity[1] = alpha * gravity[1] + (1 - alpha) * y
        gravity[2] = alpha * gravity[2] + (1 - alpha) * z

        // Remove gravity from acceleration
        val accelerationWithoutGravityX = x - gravity[0]
        val accelerationWithoutGravityY = y - gravity[1]
        val accelerationWithoutGravityZ = z - gravity[2]

        // Calculate speed using acceleration without gravity
        val accelerationMagnitude = sqrt(
            accelerationWithoutGravityX * accelerationWithoutGravityX +
                    accelerationWithoutGravityY * accelerationWithoutGravityY +
                    accelerationWithoutGravityZ * accelerationWithoutGravityZ
        )

        // Assuming device is at rest when acceleration is close to 0
        return if (accelerationMagnitude < 0.1f) 0f else accelerationMagnitude
    }

}


