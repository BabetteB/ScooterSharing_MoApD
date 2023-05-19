package dk.itu.moapd.scootersharing.babb.viewmodel


import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
class ScooterFragment : Fragment() {

    private lateinit var auth : FirebaseAuth
    private lateinit var database : DatabaseReference
    private lateinit var storage : FirebaseStorage

    private val args : ScooterFragmentArgs? by navArgs()
    private var scooterID : String? = ""
    private var timerStarted = false

    private var unlocked = false
    private lateinit var speedCalculator: SpeedCalculator


    private var _binding : FragmentScooterBinding? = null
    private val binding
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
                //lastUpdate = Calendar.getInstance().time
                tryFindScooter(scooterID)
                speedCalculator = SpeedCalculator(requireContext())

                val t = Timer()
                val tt: TimerTask = object : TimerTask() {
                    override fun run() {
                        //Log.d(TAG, "Updating speed: $speed")
                        updateSpeedText()
                    }
                }
                t.scheduleAtFixedRate(tt, 0, 1000)

            }
        }

        return binding.root
    }

    private fun updateSpeedText() {
        binding.activeScooterSpeed.text = String.format("%.2f", speedCalculator.getSpeed())
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


    class SpeedCalculator(private val context: Context) : SensorEventListener {

        private val sensorManager: SensorManager =
            context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        private val accelerometer: Sensor? =
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        private var lastTime: Long = 0
        private var lastX: Float = 0f
        private var lastY: Float = 0f
        private var lastZ: Float = 0f

        private var speed: Float = 0f

        init {
            startListening()
        }

        private fun startListening() {
            accelerometer?.let { sensor ->
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }

        private fun stopListening() {
            sensorManager.unregisterListener(this)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Ignore
        }

        override fun onSensorChanged(event: SensorEvent) {
            val currentTime = System.currentTimeMillis()
            val deltaTime = currentTime - lastTime

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val deltaX = x - lastX
            val deltaY = y - lastY
            val deltaZ = z - lastZ

            val speedX = deltaX / deltaTime * 10000 // Adjust scaling factor as needed
            val speedY = deltaY / deltaTime * 10000
            val speedZ = deltaZ / deltaTime * 10000

            speed = Math.sqrt((speedX * speedX + speedY * speedY + speedZ * speedZ).toDouble()).toFloat()

            lastX = x
            lastY = y
            lastZ = z
            lastTime = currentTime
        }

        fun getSpeed(): Float {
            return (speed * 3.6f)
        }

        fun release() {
            stopListening()
        }
    }



}

