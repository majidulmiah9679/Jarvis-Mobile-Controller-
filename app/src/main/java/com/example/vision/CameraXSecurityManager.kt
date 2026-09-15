package com.example.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * CameraX Continuous Vision and Intruder Security Capture Engine.
 * - Front camera silent capture during theft breach events.
 * - Real-time continuous vision feed analysis.
 */
class CameraXSecurityManager(private val context: Context) {

    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var isContinuousVisionActive = false

    /**
     * Silently captures a security photo from the FRONT CAMERA during a theft breach.
     */
    fun captureFrontCameraTheftPhoto(
        lifecycleOwner: LifecycleOwner,
        onPhotoCaptured: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture)

                val photoFile = File(
                    context.cacheDir,
                    "theft_intruder_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
                )

                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            Log.d("CameraXSecurity", "Intruder photo saved: ${photoFile.absolutePath}")
                            onPhotoCaptured(photoFile)
                        }

                        override fun onError(exc: ImageCaptureException) {
                            Log.e("CameraXSecurity", "Intruder photo capture failed: ${exc.message}")
                            onError(exc.message ?: "Camera capture error")
                        }
                    }
                )
            } catch (e: Exception) {
                onError(e.message ?: "Camera provider initialization error")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Continuous Vision Stream using CameraX ImageAnalysis
     */
    fun startContinuousVision(
        lifecycleOwner: LifecycleOwner,
        onFrameAnalyzed: (avgLuminance: Double, width: Int, height: Int) -> Unit
    ) {
        if (isContinuousVisionActive) return

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    analyzeFrame(imageProxy, onFrameAnalyzed)
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, analysis)

                imageAnalysis = analysis
                isContinuousVisionActive = true
            } catch (e: Exception) {
                Log.e("CameraXSecurity", "Continuous vision failed: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun analyzeFrame(imageProxy: ImageProxy, callback: (Double, Int, Int) -> Unit) {
        val buffer = imageProxy.planes[0].buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        var total = 0L
        for (b in data) {
            total += (b.toInt() and 0xFF)
        }
        val avg = if (data.isNotEmpty()) total.toDouble() / data.size else 0.0
        callback(avg, imageProxy.width, imageProxy.height)
        imageProxy.close()
    }

    fun stopContinuousVision(lifecycleOwner: LifecycleOwner) {
        try {
            val cameraProvider = ProcessCameraProvider.getInstance(context).get()
            cameraProvider.unbindAll()
            isContinuousVisionActive = false
        } catch (_: Exception) {}
    }

    fun isVisionRunning(): Boolean = isContinuousVisionActive

    fun shutdown() {
        cameraExecutor.shutdown()
    }
}
