package com.myme.qrapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.myme.qrapp.databinding.FragmentHomeBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.google.mlkit.vision.barcode.common.Barcode
import com.myme.qrapp.R
import com.myme.qrapp.SharedViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var cameraProvider: ProcessCameraProvider
    private var isQrScanningActive = false

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // 이제 startCamera() 호출은 onViewCreated에서 합니다
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰 바인딩이 null이 아닌 상태에서 코드 실행
        if (_binding != null) {
            binding.startQrScanButton.setOnClickListener {
                isQrScanningActive = !isQrScanningActive
                if (isQrScanningActive) {
                    startQrCodeScanning()
                } else {
                    stopQrCodeScanning()
                }
            }

//            startCamera()  // 뷰 바인딩이 완료된 후에 호출
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraView.surfaceProvider)
            }

            imageAnalysis = ImageAnalysis.Builder().build()
            imageAnalysis.setAnalyzer(cameraExecutor, QRCodeAnalyzer())

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (exc: Exception) {
                Log.e("HomeFragment", "카메라 바인딩 실패: ${exc.message}")
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun startQrCodeScanning() {
        Log.d("HomeFragment", "QR 코드 스캔 시작")
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraView.surfaceProvider)
            }

            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
        } catch (exc: Exception) {
            Log.e("HomeFragment", "카메라 바인딩 실패: ${exc.message}")
        }
    }

    private fun stopQrCodeScanning() {
        Log.d("HomeFragment", "QR 코드 스캔 중지")
        cameraProvider.unbind(imageAnalysis)
    }

    private inner class QRCodeAnalyzer : ImageAnalysis.Analyzer {
        @OptIn(ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {
            if (!isQrScanningActive) {
                imageProxy.close()
                return
            }

            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()
                val scanner = BarcodeScanning.getClient(options)

                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            barcode.rawValue?.let { qrCodeValue ->
                                Log.d("HomeFragment", "QR 코드 인식 성공: $qrCodeValue")

                                sharedViewModel.setQrCodeValue(qrCodeValue)

                                val bundle = Bundle().apply {
                                    putString("qrCodeValue", qrCodeValue)
                                }

                                findNavController().apply {
                                    popBackStack()
                                    navigate(R.id.navigation_dashboard)
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("HomeFragment", "QR 코드 인식 실패: ${e.message}")
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startCamera()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }
}
