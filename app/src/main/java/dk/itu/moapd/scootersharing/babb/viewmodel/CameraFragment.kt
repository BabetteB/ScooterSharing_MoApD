package dk.itu.moapd.scootersharing.babb.viewmodel

import android.app.Activity.RESULT_OK
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
import androidx.recyclerview.widget.GridLayoutManager
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

    private val REQUEST_CAMERA_PERMISSION = 1099

    private lateinit var auth : FirebaseAuth
    private lateinit var database : DatabaseReference
    private lateinit var storage : FirebaseStorage

    private var _binding : FragmentCameraBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Oh no I died"
        }

    companion object{
        private const val TAG = "CameraFragment"
        private lateinit var DATABASE_URL: String
        private lateinit var BUCKET_URL : String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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


        with (binding) {
            imagesRecyclerView.layoutManager = GridLayoutManager(context, 3)
            checkPermission()

            buttonOpenCamera.setOnClickListener {
                val photoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                photoLauncher.launch(photoIntent)
            }
        }

        return binding.root
    }

    private val photoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        photoResult(result)
    }


    private fun photoResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            // Create the folder structure save the selected image in the bucket.
            auth.currentUser?.let {
                val filename = Calendar.getInstance().time.toString()
                val image = storage.reference.child("images/${it.uid}/$filename")
                result.data?.extras?.get("data").let {
                    val bm = result.data?.extras?.get("data") as Bitmap
                    uploadImageToBucket(bm, image)
                }
            }
        } else {
            Toast.makeText(
                binding.root.context,
                "An error occurred. Image not registered.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun uploadImageToBucket(bitmap: Bitmap, image: StorageReference) {
        Log.d(TAG, "uploading image to bucket")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data: ByteArray = baos.toByteArray()

        // Upload the original image.
        image.putBytes(data).addOnSuccessListener {
            Log.d(TAG, "Image uploaded")
        }.addOnFailureListener{
            Log.d(TAG, "Image not uploaded. Exception: $it")
        }
    }

    private fun checkPermission() =
        if (ActivityCompat.checkSelfPermission(this.requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this.requireContext(), "Camera Permission Already Granted", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(this.requireActivity(), arrayOf<String>(android.Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }






}