package dk.itu.moapd.scootersharing.babb.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.Manifest
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dk.itu.moapd.scootersharing.babb.R
import dk.itu.moapd.scootersharing.babb.databinding.FragmentSpeedometerBinding
import java.lang.Float.max
import java.lang.Float.min


/**
 * A fragment to act as a Speedometer using the linear acceleration sensor.
 * Use the [SpeedometerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SpeedometerFragment : Fragment(){
    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1010
    }

    private lateinit var sensorManager: SensorManager

    private val linearListener: SensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                binding.apply {

                    binding.showSpeed.text = (event.values[0].normalize()-50).toString()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
    }


    private var _binding: FragmentSpeedometerBinding? = null
    private val binding
        get() = checkNotNull(_binding){
            "Cannot access binding."
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSpeedometerBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val service = Context.SENSOR_SERVICE
        sensorManager = requireActivity().getSystemService(service) as SensorManager
    }

    override fun onResume() {
        super.onResume()

        val linearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        if(linearAcceleration!= null)
            sensorManager.registerListener(linearListener,linearAcceleration, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()

        sensorManager.unregisterListener(linearListener)
    }


    private fun Float.normalize(): Int {
        val norm = min(max(this, -SensorManager.STANDARD_GRAVITY), SensorManager.STANDARD_GRAVITY)
        return ((norm + SensorManager.STANDARD_GRAVITY) /
                (2f * SensorManager.STANDARD_GRAVITY) * 100).toInt()
    }

}