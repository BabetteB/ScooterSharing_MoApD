package dk.itu.moapd.scootersharing.babb.viewmodel

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dk.itu.moapd.scootersharing.babb.R
import dk.itu.moapd.scootersharing.babb.databinding.FragmentQrscanBinding
import java.util.concurrent.Executors


class QrscanFragment : Fragment() {

    private var _binding : FragmentQrscanBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            findNavController().popBackStack(R.id.rideListFragment, false)
        }

    private var scooterID : String = ""

    companion object {
        private const val TAG = "QrscanFragment"
        private const val CAMERA_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkCameraPermission()){
            bindCameraUseCases()
        } else requestCameraPermission()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrscanBinding.inflate(layoutInflater, container, false)

        with(binding){
            btnGoToRide.isEnabled = false
            btnGoToRide.setOnClickListener {
                if (scooterID.isBlank()){
                    Toast.makeText(
                        requireContext(),
                        "Please scan a valid scooter QR code. Scanned is now: $scooterID",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    goToScooter()
                }
            }
        }

        return binding.root
    }

    private fun goToScooter(){
        findNavController().navigate(
            QrscanFragmentDirections.showScooterFrag(scooterID)
        )
    }


    private fun bindCameraUseCases() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val options = BarcodeScannerOptions.Builder().setBarcodeFormats(
                Barcode.FORMAT_QR_CODE
            ).build()
            val scanner = BarcodeScanning.getClient(options)


            val analysisUseCase = ImageAnalysis.Builder()
                .build()

            analysisUseCase.setAnalyzer(
                // newSingleThreadExecutor() will let us perform analysis on a single worker thread
                Executors.newSingleThreadExecutor()
            ) { imageProxy ->
                processImageProxy(scanner, imageProxy)
            }

            // setting up the preview use case
            val previewUseCase = Preview.Builder()
                .build()
                .also {
                    try {
                        it.setSurfaceProvider(binding.cameraView.surfaceProvider)
                    } catch (e : java.lang.Exception) {
                        Log.e(TAG, e.message.orEmpty())
                        findNavController().popBackStack(R.id.rideListFragment, false)
                    }

                }

            // configure to use the back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    previewUseCase,
                    analysisUseCase
                )
            } catch (illegalStateException: IllegalStateException) {
                // If the use case has already been bound to another lifecycle or method is not called on main thread.
                Log.e(TAG, illegalStateException.message.orEmpty())
                findNavController().popBackStack(R.id.rideListFragment, false)
            }
        }, ContextCompat.getMainExecutor(this.requireContext()))

    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(
        barcodeScanner: BarcodeScanner,
        imageProxy: ImageProxy
    ) {

        imageProxy.image?.let { image ->
            val inputImage =
                InputImage.fromMediaImage(
                    image,
                    imageProxy.imageInfo.rotationDegrees
                )

            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodeList ->
                    val barcode = barcodeList.getOrNull(0)
                    // `rawValue` is the decoded value of the barcode
                    barcode?.rawValue?.let { value ->
                        // update our textView to show the decoded value
                        binding.cameraResultText.text = "Found scooter! Please continue."
                        binding.btnGoToRide.isEnabled = true
                        scooterID = value
                    }
                }
                .addOnFailureListener {

                    Log.e(TAG, it.message.orEmpty())
                }.addOnCompleteListener {

                    imageProxy.image?.close()
                    imageProxy.close()
                }
        }
    }


    private fun checkCameraPermission() : Boolean {
        return ContextCompat.checkSelfPermission(
            this.requireContext(),
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this.requireActivity(), arrayOf(android.Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
    }



}