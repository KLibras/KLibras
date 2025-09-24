package com.br.klibras

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.br.klibras.databinding.ActivityMainBinding
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    // --- View Binding and Core Components ---
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var signLanguageModel: SignLanguageModel

    // --- MediaPipe Landmarkers ---
    private var handLandmarker: HandLandmarker? = null
    private var poseLandmarker: PoseLandmarker? = null

    // --- Model and Data Handling Configuration ---
    private val maxFrames = 30       // Must match the model's expected sequence length
    private val numKeypoints = 258   // Must match the features extracted during training
    private val capturedFrames = mutableListOf<FloatArray>()
    private var frameCounter = 0

    // --- State Machine for Capture Logic ---
    private enum class CaptureState { IDLE, CAPTURING, COOLDOWN }
    private var currentState = CaptureState.IDLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // UI setup
        binding.recordButton.isEnabled = false // Button is not used for this logic
        binding.recordButton.text = "Live"
        binding.predictionText.text = "Show a hand to begin" // Initial prompt

        signLanguageModel = SignLanguageModel(assets)
        cameraExecutor = Executors.newSingleThreadExecutor()

        setupLandmarkers()
        requestCameraPermission()
    }

    private fun setupLandmarkers() {
        try {
            // Hand Landmarker (remains in IMAGE mode to match training)
            val handBaseOptions = BaseOptions.builder()
                .setModelAssetPath("hand_landmarker.task")
                .setDelegate(Delegate.GPU).build()
            val handOptions = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(handBaseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .setNumHands(2).build()
            handLandmarker = HandLandmarker.createFromOptions(this, handOptions)

            // Pose Landmarker (remains in IMAGE mode to match training)
            val poseBaseOptions = BaseOptions.builder()
                .setModelAssetPath("pose_landmarker_lite.task")
                .setDelegate(Delegate.GPU).build()
            val poseOptions = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(poseBaseOptions)
                .setRunningMode(RunningMode.IMAGE).build()
            poseLandmarker = PoseLandmarker.createFromOptions(this, poseOptions)
        } catch (e: Exception) {
            Log.e("Landmarker", "Error setting up landmarkers", e)
            Toast.makeText(this, "Error initializing models.", Toast.LENGTH_LONG).show()
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun analyzeImage(imageProxy: ImageProxy) {
        // Throttle analysis for performance, even when idle.
        if (frameCounter++ % 3 != 0) {
            imageProxy.close()
            return
        }

        val bitmap = imageProxy.toBitmap() ?: run { imageProxy.close(); return }
        val rotatedBitmap = rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees.toFloat())
        val mpImage = BitmapImageBuilder(rotatedBitmap).build()

        try {
            val handResult = handLandmarker?.detect(mpImage)
            val poseResult = poseLandmarker?.detect(mpImage)
            val isHandPresent = handResult?.landmarks()?.isNotEmpty() == true

            // --- State Machine Logic ---
            when (currentState) {
                CaptureState.IDLE -> {
                    if (isHandPresent) {
                        // Hand detected, start capturing
                        currentState = CaptureState.CAPTURING
                        capturedFrames.clear()
                        Log.d("StateMachine", "Hand detected. Starting capture.")
                        // Fall-through to capture the very first frame immediately
                    }
                }

                CaptureState.CAPTURING -> {
                    if (isHandPresent) {
                        val keypoints = extractKeypoints(poseResult, handResult)
                        capturedFrames.add(keypoints)

                        // Update UI with capture progress
                        runOnUiThread {
                            binding.predictionText.text = "Capturing... ${capturedFrames.size} / $maxFrames"
                        }

                        // Check if we have collected enough frames
                        if (capturedFrames.size == maxFrames) {
                            Log.d("StateMachine", "Capture complete. Running prediction.")
                            val inputBuffer = preprocessKeypointsForModel(capturedFrames.toList())
                            val predictionResult = signLanguageModel.predict(inputBuffer)

                            // Display result and enter cooldown
                            runOnUiThread {
                                displayPredictionResult(predictionResult)
                            }
                            currentState = CaptureState.COOLDOWN
                        }
                    } else {
                        // Hand was lost during capture, reset
                        Log.d("StateMachine", "Hand lost during capture. Resetting.")
                        currentState = CaptureState.IDLE
                        runOnUiThread { binding.predictionText.text = "Capture failed. Show hand." }
                    }
                }

                CaptureState.COOLDOWN -> {
                    // Wait for the hand to disappear to prevent immediate re-triggering
                    if (!isHandPresent) {
                        Log.d("StateMachine", "Hand removed. Resetting to IDLE.")
                        currentState = CaptureState.IDLE
                        runOnUiThread { binding.predictionText.text = "Show a hand to begin" }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AnalyzeImage", "Error during detection", e)
        } finally {
            imageProxy.close()
        }
    }

    private fun extractKeypoints(poseResult: PoseLandmarkerResult?, handResult: HandLandmarkerResult?): FloatArray {
        val keypoints = FloatArray(numKeypoints) { 0f }

        poseResult?.landmarks()?.firstOrNull()?.forEachIndexed { i, landmark ->
            val baseIndex = i * 4
            if (baseIndex + 3 < 132) {
                keypoints[baseIndex + 0] = landmark.x()
                keypoints[baseIndex + 1] = landmark.y()
                keypoints[baseIndex + 2] = landmark.z()
                keypoints[baseIndex + 3] = landmark.visibility().orElse(0f)
            }
        }

        val POSE_KEYPOINTS_COUNT = 132
        val HAND_KEYPOINTS_COUNT = 63
        var leftHandFound = false
        var rightHandFound = false
        handResult?.let {
            for ((handIndex, landmarks) in it.landmarks().withIndex()) {
                if (handIndex >= it.handednesses().size || it.handednesses()[handIndex].isEmpty()) continue
                val handedness = it.handednesses()[handIndex].first().categoryName()

                if (handedness == "Left" && !leftHandFound) {
                    val baseIndex = POSE_KEYPOINTS_COUNT
                    landmarks.forEachIndexed { i, landmark ->
                        val idx = baseIndex + (i * 3)
                        if (idx + 2 < keypoints.size) {
                            keypoints[idx + 0] = landmark.x()
                            keypoints[idx + 1] = landmark.y()
                            keypoints[idx + 2] = landmark.z()
                        }
                    }
                    leftHandFound = true
                } else if (handedness == "Right" && !rightHandFound) {
                    val baseIndex = POSE_KEYPOINTS_COUNT + HAND_KEYPOINTS_COUNT
                    landmarks.forEachIndexed { i, landmark ->
                        val idx = baseIndex + (i * 3)
                        if (idx + 2 < keypoints.size) {
                            keypoints[idx + 0] = landmark.x()
                            keypoints[idx + 1] = landmark.y()
                            keypoints[idx + 2] = landmark.z()
                        }
                    }
                    rightHandFound = true
                }
            }
        }
        return keypoints
    }

    private fun displayPredictionResult(prediction: FloatArray?) {
        if (prediction == null || prediction.size < 2) {
            binding.predictionText.text = "Prediction failed"
            return
        }
        val labels = listOf("Obrigado", "Nada")
        val obrigadoConfidence = prediction[0]
        val nadaConfidence = prediction[1]
        val confidenceThreshold = 0.7f

        val resultText = when {
            obrigadoConfidence > nadaConfidence && obrigadoConfidence > confidenceThreshold ->
                "Sign: ${labels[0]} (${(obrigadoConfidence * 100).toInt()}%)"
            nadaConfidence > obrigadoConfidence && nadaConfidence > confidenceThreshold ->
                "Sign: ${labels[1]} (${(nadaConfidence * 100).toInt()}%)"
            else -> "Sign: Not recognized"
        }
        binding.predictionText.text = resultText
    }

    private fun preprocessKeypointsForModel(keypoints: List<FloatArray>): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(1 * maxFrames * numKeypoints * 4)
        buffer.order(ByteOrder.nativeOrder())
        keypoints.forEach { frame -> frame.forEach { buffer.putFloat(it) } }
        buffer.rewind()
        return buffer
    }

    @SuppressLint("RestrictedApi")
    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }
            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also { it.setAnalyzer(cameraExecutor, this::analyzeImage) }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, preview, imageAnalyzer)
            } catch (exc: Exception) {
                Log.e("Camera", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) startCamera() else Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_LONG).show()
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> startCamera()
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        handLandmarker?.close()
        poseLandmarker?.close()
    }
}