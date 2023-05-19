package dk.itu.moapd.scootersharing.babb.viewmodel

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import dk.itu.moapd.scootersharing.babb.R
import dk.itu.moapd.scootersharing.babb.databinding.FragmentCameraBinding
import java.io.ByteArrayOutputStream
import java.util.*


class CameraFragment : Fragment() {

    val REQUEST_CAMERA_PERMISSION = 1099

    lateinit var auth: FirebaseAuth
    lateinit var database: DatabaseReference
    lateinit var storage: FirebaseStorage

    val args: CameraFragmentArgs? by navArgs()

    var _binding: FragmentCameraBinding? = null
    val binding
        get() = checkNotNull(_binding) {
            "Oh no I died"
        }

    companion object {
        const val TAG = "CameraFragment"
        lateinit var DATABASE_URL: String
        lateinit var BUCKET_URL: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "Creating $TAG")
        auth = FirebaseAuth.getInstance()

        DATABASE_URL = resources.getString(R.string.DATABASE_URL)
        BUCKET_URL = resources.getString(R.string.BUCKET_URL)

        database = Firebase.database(DATABASE_URL).reference
        storage = Firebase.storage(BUCKET_URL)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(layoutInflater, container, false)

        Log.d(TAG, "Permission : ${checkPermission()}")
        if (checkPermission()) {
            startPhotoIntent()
        } else {
            requestPermission()
        }
        return binding.root
    }

    fun startPhotoIntent() {
        val photoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        Log.d(TAG, "Launching photo intent")
        photoLauncher.launch(photoIntent)
    }


    var photoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "PhotoLaunch result : $result")
        photoResult(result)
        findNavController().popBackStack(R.id.rideListFragment, false)
    }


    fun photoResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            // Create the folder structure save the selected image in the bucket.
            auth.currentUser?.let {
                Log.d(TAG, "finding path for image")
                val image = storage.reference.child("scooterImages/${args?.sid}")
                val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
                with(sharedPref.edit()) {
                    putString("imageUri", "scooterImages/${args?.sid}")
                    apply()
                }
                result.data?.extras?.get("data").let {
                    val bm = result.data?.extras?.get("data") as Bitmap
                    uploadImageToBucket(bm, image)
                }
            }
        } else {
            shortToast("An error occurred. Image not registered.")
        }
    }

    fun uploadImageToBucket(bitmap: Bitmap, image: StorageReference) {
        Log.d(TAG, "uploading image to bucket")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data: ByteArray = baos.toByteArray()

        // Upload the original image.
        image.putBytes(data).addOnSuccessListener {
            Log.d(TAG, "Image uploaded")
        }.addOnFailureListener {
            Log.d(TAG, "Image not uploaded. Exception: $it")
        }
    }

    fun shortToast(text: String) {
        Toast.makeText(
            binding.root.context,
            text,
            Toast.LENGTH_SHORT
        ).show()
    }

    fun checkPermission() =
        ActivityCompat.checkSelfPermission(
            this.requireContext(), android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    fun requestPermission() {
        Log.d(TAG, "Requesting permission")
        ActivityCompat.requestPermissions(
            this.requireActivity(),
            arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_CAMERA_PERMISSION
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                startPhotoIntent()
            }
        }
    }
}



