package dk.itu.moapd.scootersharing.babb.viewmodel


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NavUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dk.itu.moapd.scootersharing.babb.R
import dk.itu.moapd.scootersharing.babb.databinding.FragmentScooterBinding
import dk.itu.moapd.scootersharing.babb.model.NoParkingZones
import dk.itu.moapd.scootersharing.babb.model.Scooter
import dk.itu.moapd.scootersharing.babb.model.ScooterViewModel
import java.util.*
import kotlin.math.sqrt


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class ScooterFragment : Fragment(), SensorEventListener {

    private lateinit var auth : FirebaseAuth
    lateinit var database : DatabaseReference
    private lateinit var storage : FirebaseStorage
    private lateinit var vm : ScooterViewModel

    private val REQUEST_LOCATION_PERMISSION = 1001
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val args : ScooterFragmentArgs? by navArgs()
    private var scooterID : String? = ""
    private var savedScooterId : String? = ""
    private var timerStarted = false
    private var unlocked = false


    //------------------SPEED STUFF
    private var sensorManager: SensorManager? = null
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

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())

        vm = ViewModelProvider(requireActivity()).get(ScooterViewModel::class.java)
        savedScooterId = vm.activeScooterId
        Log.d(TAG, "vm.activeScooterid = ${vm.activeScooterId}")
        Log.d(TAG, "instance created")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "creating view")
        _binding = FragmentScooterBinding.inflate(layoutInflater, container, false)

        if (savedScooterId.isNullOrBlank()){
            scooterID = args?.sid
        }

        with (binding) {
            if (scooterID.isNullOrBlank() && savedScooterId.isNullOrBlank() ) {
                scooterFragmentTitle.text = "No scooter ride in progress"

                activeScooterName.text = "Please start a ride before information can be shown."
                activeScooterSpeed.visibility = View.INVISIBLE
                activeScooterTime.visibility = View.INVISIBLE
                activeScooterUnlock.visibility = View.INVISIBLE
                activeScooterButtonEnd.visibility= View.INVISIBLE
                scooterFragmentProgressBar.visibility = View.INVISIBLE
            } else {
                sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
                // Get the accelerometer sensor
                accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

                if (!savedScooterId.isNullOrBlank()){
                    tryFindScooter(savedScooterId)
                    binding.apply {
                        activeScooterUnlock.isEnabled = false
                        activeScooterButtonEnd.isEnabled = true
                    }
                } else {
                    tryFindScooter(scooterID)
                    enableActiveScooterFields()
                }
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

            activeScooterButtonEnd.setOnClickListener {
                endRide()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }


    fun tryFindScooter(id : String?) {
        if (id != null) {
            database.child("scooters").child(id).get()
                .addOnSuccessListener {d ->
                    val m = d.value as Map<String, Any>
                    val name = m["name"] as String?
                    binding.activeScooterName.text = name

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
                        Log.d(TAG, "Scooter reserved = ${reserved}!")
                    }
                    .addOnFailureListener {
                        shortToast("Scooter reserved = ${reserved}!")
                    }
            }
        }
    }

    private fun enableActiveScooterFields() {

        binding.activeScooterButtonEnd.isEnabled = false

    }

    private fun endRide() {
        if (!checkPermission()){
            ActivityCompat.requestPermissions(
                this.requireActivity(),
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }

        getUserLocation { location ->
            val uL = LatLng(location.latitude, location.longitude)
            if (NoParkingZones.isLatLngWithinPolygon(uL)){
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Scooter cannot be parked here")
                    .setMessage("This is a no parking zone, hence it is not possible to park a scooter in this area")
                    .setNeutralButton("Ok") { dialog, which ->
                        // Respond to neutral button press
                    }.show()
            } else {
                setReserveScooter(false)
                vm.activeScooterId = ""
                binding.activeScooterUnlock.isEnabled = true
                binding.activeScooterButtonEnd.isEnabled = false

                auth.currentUser?.let { user ->
                    scooterID?.let {
                        uploadRidetoDB(user)
                        findNavController().navigate(
                            ScooterFragmentDirections.takePictureEndRide(it)
                        )
                    }
                }
            }
        }
    }


    private fun checkPermission() =
        ActivityCompat.checkSelfPermission(
            this.requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this.requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                getUserLocation { location ->
                    // Handle the location result
                    Log.d(TAG, "User location: ${location.latitude}, ${location.longitude}")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation(callback: (LatLng) -> Unit) {
        val defaultLocation = LatLng(55.69518532166335, 12.550138887442337)

        val locationResult = fusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener(this.requireActivity()) { task ->
            if (task.isSuccessful) {
                val res = task.result
                if (res != null) {
                    callback(LatLng(res.latitude, res.longitude))
                } else {
                    callback(defaultLocation)
                }
            } else {
                Log.d(TAG, "Something went wrong. Setting location to default location.")
                callback(defaultLocation)
            }
        }
    }

    private fun uploadRidetoDB(user : FirebaseUser) {
        database.child("scooters").child(scooterID!!).get().addOnSuccessListener {
            val m = it.value as Map<String, Any>

            getUserLocation { location ->
                val s = Scooter(
                    id = m["id"] as String?,
                    name = m["name"] as String?,
                    locationLat = location.latitude as Double?,
                    locationLng = location.longitude as Double?,
                    reserved = false,
                    createdAt = m["createdAt"] as Long?,
                    lastUpdateTimeStamp = Calendar.getInstance().time,
                    assignedToUserID = null
                )

                database.child("history/${user.uid}/${scooterID!!}/")
                    .setValue(s)
                    .addOnSuccessListener {
                        shortToast("Ride finished")
                    }
                    .addOnFailureListener {e ->
                        shortToast("An error occurred. Ride still active!")
                    }


                database.child("scooters/${scooterID!!}/locationLat/")
                    .setValue(s.locationLat)
                    .addOnSuccessListener {
                        Log.d(TAG, "Success adding lat")
                    }.addOnFailureListener{
                        Log.d(TAG, "Failure adding lat")
                    }

                database.child("scooters/${scooterID!!}/locationLng/")
                    .setValue(s.locationLng)
                    .addOnSuccessListener {
                        Log.d(TAG,"Success adding long")
                    }.addOnFailureListener{
                        Log.d(TAG, "Failure adding long")
                    }
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
        setReserveScooter(true)
        Log.d(TAG, "saving scooter id : $scooterID to vm")
        vm.activeScooterId = scooterID
        Log.d(TAG, "vm.activeScooterId: ${vm.activeScooterId}")

        binding.apply {
            activeScooterUnlock.isEnabled = false
            activeScooterButtonEnd.isEnabled = true
        }

        Toast.makeText(
            this.requireContext(),
            "Ride started",
            Toast.LENGTH_SHORT
        ).show()
    }


    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        // Unregister sensor listeners
        if (sensorManager != null)
            sensorManager?.unregisterListener(this)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // Calculate the speed using accelerometer data
            val speed = calculateSpeed(event.values[0], event.values[1], event.values[2])
            val speedKmH = speed * 3.6f
            binding.activeScooterSpeed.text = String.format("%.2f", speedKmH)
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


