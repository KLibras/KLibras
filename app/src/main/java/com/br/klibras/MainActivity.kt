package com.br.klibras

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.os.CountDownTimer
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
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var signLanguageModel: SignLanguageModel
    private var handLandmarker: HandLandmarker? = null
    private var poseLandmarker: PoseLandmarker? = null

    private var isRecording = false
    private val recordedFrames = mutableListOf<FloatArray>()
    private var recordingTimer: CountDownTimer? = null
    private val recordingDurationMs = 3000L

    // Original keypoint count for both Pose and Hands
    private val numKeypoints = 258
    private val maxFrames = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ensure SignLanguageModel is loading your original Pose+Hands model
        signLanguageModel = SignLanguageModel(assets)
        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.recordButton.setOnClickListener {
            if (!isRecording) startRecording() else stopRecordingAndPredict()
        }

        binding.recordButton.isEnabled = false
        Executors.newSingleThreadExecutor().execute {
            setupLandmarkers()
            runOnUiThread {
                requestCameraPermission()
                binding.recordButton.isEnabled = true
            }
        }
    }

    private fun setupLandmarkers() {
        try {
            // Setup Hand Landmarker with GPU
            val handBaseOptions = BaseOptions.builder()
                .setModelAssetPath("hand_landmarker.task")
                .setDelegate(Delegate.GPU)
                .build()
            val handOptions = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(handBaseOptions)
                .setRunningMode(RunningMode.VIDEO)
                .setNumHands(2)
                .build()
            handLandmarker = HandLandmarker.createFromOptions(this, handOptions)

            // Setup Pose Landmarker with GPU
            val poseBaseOptions = BaseOptions.builder()
                .setModelAssetPath("pose_landmarker_lite.task")
                .setDelegate(Delegate.GPU)
                .build()
            val poseOptions = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(poseBaseOptions)
                .setRunningMode(RunningMode.VIDEO)
                .build()
            poseLandmarker = PoseLandmarker.createFromOptions(this, poseOptions)
        } catch (e: Exception) {
            Log.e("Landmarker", "Error setting up landmarkers", e)
            runOnUiThread {
                Toast.makeText(this, "Failed to initialize models.", Toast.LENGTH_LONG).show()
            }
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun analyzeImage(imageProxy: ImageProxy) {
        val startTime = System.currentTimeMillis()

        val bitmap = imageProxy.toBitmap()
        val rotatedBitmap = if (imageProxy.imageInfo.rotationDegrees != 0) {
            rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees.toFloat())
        } else { bitmap }

        val mpImage = BitmapImageBuilder(rotatedBitmap).build()
        val frameTimestamp = TimeUnit.NANOSECONDS.toMillis(imageProxy.imageInfo.timestamp)

        try {
            // Smart Trigger: Run fast hand detector first
            val handResult = handLandmarker?.detectForVideo(mpImage, frameTimestamp)

            // Only if hands are found, run the slower pose detector
            if (isRecording && handResult != null && handResult.landmarks().isNotEmpty()) {
                val poseResult = poseLandmarker?.detectForVideo(mpImage, frameTimestamp)

                // Now combine both results
                val keypoints = extractKeypoints(poseResult, handResult)
                recordedFrames.add(keypoints)
            }

            val totalTime = System.currentTimeMillis() - startTime
            Log.d("Performance", "Total processing time: ${totalTime}ms")
        } catch (e: Exception) {
            Log.e("AnalyzeImage", "Error during detection", e)
        } finally {
            imageProxy.close()
        }
    }

    private fun extractKeypoints(poseResult: PoseLandmarkerResult?, handResult: HandLandmarkerResult?): FloatArray {
        val keypoints = FloatArray(numKeypoints) { 0f }

        // 1. Pose landmarks (33 landmarks * 4 values = 132)
        poseResult?.landmarks()?.firstOrNull()?.forEachIndexed { i, landmark ->
            val baseIndex = i * 4
            if (baseIndex + 3 < 132) {
                keypoints[baseIndex + 0] = landmark.x()
                keypoints[baseIndex + 1] = landmark.y()
                keypoints[baseIndex + 2] = landmark.z()
                keypoints[baseIndex + 3] = landmark.visibility().orElse(0f)
            }
        }

        // 2. Hand landmarks
        handResult?.let {
            for ((handIndex, landmarks) in it.landmarks().withIndex()) {
                if (handIndex < it.handednesses().size && it.handednesses()[handIndex].isNotEmpty()) {
                    val handedness = it.handednesses()[handIndex].first().categoryName()

                    // Left hand data starts at index 132, Right at 195
                    val baseIndex = if (handedness == "Left") 132 else 195

                    landmarks.forEachIndexed { i, landmark ->
                        val landmarkIndex = baseIndex + (i * 3)
                        if (landmarkIndex + 2 < keypoints.size) {
                            keypoints[landmarkIndex + 0] = landmark.x()
                            keypoints[landmarkIndex + 1] = landmark.y()
                            keypoints[landmarkIndex + 2] = landmark.z()
                        }
                    }
                }
            }
        }
        return keypoints
    }

    private fun startRecording() {
        isRecording = true
        recordedFrames.clear()
        binding.recordButton.text = "Stop"
        binding.predictionText.text = "Recording..."
        recordingTimer = object : CountDownTimer(recordingDurationMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() { if (isRecording) stopRecordingAndPredict() }
        }.start()
    }

    private fun stopRecordingAndPredict() {
        isRecording = false
        recordingTimer?.cancel()
        binding.recordButton.text = "Record"
        binding.predictionText.text = "Processing..."

        if (recordedFrames.isEmpty()) {
            binding.predictionText.text = "No hands detected."
            return
        }

        Log.d("Prediction", "Collected ${recordedFrames.size} frames.")
        val finalSequence = prepareSequenceForModel(recordedFrames)
        val inputBuffer = preprocessKeypointsForModel(finalSequence)
        val predictionResult = signLanguageModel.predict(inputBuffer)
        displayPredictionResult(predictionResult)
    }

    private fun displayPredictionResult(prediction: FloatArray?) {
        if (prediction == null || prediction.size < 2) {
            binding.predictionText.text = "Prediction failed"
            return
        }

        val obrigadoConfidence = prediction[0]
        val nadaConfidence = prediction[1]

        val resultText = if (obrigadoConfidence > nadaConfidence && obrigadoConfidence > 0.7F) {
            "Sign: Obrigado (${(obrigadoConfidence * 100).toInt()}%)"
        } else {
            "Sign: Nada (${(nadaConfidence * 100).toInt()}%)"
        }
        binding.predictionText.text = resultText
    }

    private fun prepareSequenceForModel(frames: List<FloatArray>): List<FloatArray> {
        val frameCount = frames.size
        if (frameCount == maxFrames) return frames

        val preparedFrames = mutableListOf<FloatArray>()
        if (frameCount > maxFrames) {
            val step = frameCount.toFloat() / maxFrames
            for (i in 0 until maxFrames) {
                val index = (i * step).roundToInt().coerceIn(0, frameCount - 1)
                preparedFrames.add(frames[index])
            }
        } else {
            preparedFrames.addAll(frames)
            val emptyKeypoints = FloatArray(numKeypoints) { 0f }
            while (preparedFrames.size < maxFrames) {
                preparedFrames.add(emptyKeypoints)
            }
        }
        return preparedFrames
    }

    @SuppressLint("RestrictedApi")
    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = binding.viewFinder.surfaceProvider
            }
            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, this::analyzeImage)
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
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

    private fun preprocessKeypointsForModel(keypoints: List<FloatArray>): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(1 * maxFrames * numKeypoints * 4)
        buffer.order(ByteOrder.nativeOrder())
        keypoints.forEach { frame -> frame.forEach { buffer.putFloat(it) } }
        buffer.rewind()
        return buffer
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            startCamera()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        handLandmarker?.close()
        poseLandmarker?.close()
        recordingTimer?.cancel()
    }
}