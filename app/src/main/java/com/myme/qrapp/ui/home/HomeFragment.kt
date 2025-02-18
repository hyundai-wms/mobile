package com.myme.qrapp.ui.home

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.google.mlkit.vision.barcode.common.Barcode
import com.myme.qrapp.databinding.FragmentHomeBinding
import com.myme.qrapp.ui.QrCodeDialogFragment
import java.io.File

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var cameraProvider: ProcessCameraProvider
    private var isQrScanningActive = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // 카메라 시작
        startCamera()

        // QR 코드 인식 버튼 클릭 시 처리
        binding.startQrScanButton.setOnClickListener {
            isQrScanningActive = !isQrScanningActive  // QR 코드 스캔 활성화 상태 변경
            if (isQrScanningActive) {
                startQrCodeScanning()  // QR 코드 인식 시작
            } else {
                stopQrCodeScanning()   // QR 코드 인식 중지
            }
        }

        return binding.root
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraView.surfaceProvider)
            }

            // ImageAnalysis 설정
            imageAnalysis = ImageAnalysis.Builder()
                .build()
            imageAnalysis.setAnalyzer(cameraExecutor, QRCodeAnalyzer())

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e("CameraFragment", "카메라 바인딩 실패: ${exc.message}")
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun startQrCodeScanning() {
        Log.d("CameraFragment", "QR 코드 스캔 시작")
        // QR 코드 인식이 활성화되면 다시 바인딩
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()

            // Preview 설정
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraView.surfaceProvider)
            }

            // QR 코드 인식 ImageAnalysis 바인딩
            cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalysis
            )
        } catch (exc: Exception) {
            Log.e("CameraFragment", "카메라 바인딩 실패: ${exc.message}")
        }
    }

    private fun stopQrCodeScanning() {
        Log.d("CameraFragment", "QR 코드 스캔 중지")
        // QR 코드 인식이 비활성화되면 ImageAnalysis를 unbind하여 중지
        cameraProvider.unbind(imageAnalysis)
    }

    // QR 코드 인식 분석기
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
                                Log.d("CameraFragment", "QR 코드 인식 성공: $qrCodeValue")
                                //TODO:  QR 코드 인식 후 다이얼로그 띄우기 (여기 부분 수정 하면 됨)
                                if (parentFragmentManager.findFragmentByTag("QrCodeDialog") == null) {
                                    QrCodeDialogFragment(qrCodeValue).show(parentFragmentManager, "QrCodeDialog")
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("CameraFragment", "QR 코드 인식 실패: ${e.message}")
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }
}
