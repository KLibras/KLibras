package com.br.klibras

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

class HandLandmarkerHelper(
    val context: Context,
    // The listener is no longer nullable as it's required for LIVE_STREAM mode
    val landmarkerListener: LandmarkerListener,
    // Set default delegate to GPU for better performance
    private var currentDelegate: Int = DELEGATE_GPU
) {

    private var handLandmarker: HandLandmarker? = null

    init {
        setupHandLandmarker()
    }

    fun clearHandLandmarker() {
        handLandmarker?.close()
        handLandmarker = null
    }

    fun setupHandLandmarker() {
        try {
            val baseOptionsBuilder = BaseOptions.builder().setModelAssetPath(MP_HAND_LANDMARKER_TASK)

            when (currentDelegate) {
                DELEGATE_CPU -> {
                    baseOptionsBuilder.setDelegate(Delegate.CPU)
                    Log.d(TAG, "CPU delegate selected.")
                }
                DELEGATE_GPU -> {
                    baseOptionsBuilder.setDelegate(Delegate.GPU)
                    Log.d(TAG, "GPU delegate selected.")
                }
            }

            val baseOptions = baseOptionsBuilder.build()
            val options = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setNumHands(DEFAULT_NUM_HANDS)
                .setMinHandDetectionConfidence(DEFAULT_HAND_DETECTION_CONFIDENCE)
                .setMinTrackingConfidence(DEFAULT_HAND_TRACKING_CONFIDENCE)
                .setMinHandPresenceConfidence(DEFAULT_HAND_PRESENCE_CONFIDENCE)
                .setResultListener(this::returnLivestreamResult)
                .setErrorListener(this::returnLivestreamError)
                .build()
            handLandmarker = HandLandmarker.createFromOptions(context, options)
        } catch (e: IllegalStateException) {
            landmarkerListener.onError(
                "Hand Landmarker failed to initialize. See error log.",
                OTHER_ERROR
            )
            Log.e(TAG, "MediaPipe failed to initialize the task with error: ${e.message}")
        } catch (e: RuntimeException) {
            // This happens if the GPU delegate is not supported on the device
            landmarkerListener.onError(
                "Hand Landmarker failed to initialize with GPU. Falling back to CPU.",
                GPU_ERROR
            )
            Log.e(TAG, "GPU delegate failed: ${e.message}")
            // Fallback to CPU
            currentDelegate = DELEGATE_CPU
            setupHandLandmarker() // Retry with CPU
        }
    }

    fun detectLiveStream(bitmap: Bitmap) {
        if (handLandmarker == null) return

        val frameTime = SystemClock.uptimeMillis()
        val mpImage = BitmapImageBuilder(bitmap).build()

        // This is an asynchronous call, the result will be returned in the listener
        handLandmarker?.detectAsync(mpImage, frameTime)
    }

    private fun returnLivestreamResult(result: HandLandmarkerResult, input: MPImage) {
        val finishTime = SystemClock.uptimeMillis()
        val inferenceTime = finishTime - result.timestampMs()
        landmarkerListener.onResults(
            ResultBundle(
                results = listOf(result),
                inferenceTime = inferenceTime
            )
        )
    }

    private fun returnLivestreamError(error: RuntimeException) {
        landmarkerListener.onError(
            error.message ?: "An unknown error has occurred",
            OTHER_ERROR
        )
    }

    interface LandmarkerListener {
        fun onError(error: String, errorCode: Int = OTHER_ERROR)
        fun onResults(resultBundle: ResultBundle)
    }

    data class ResultBundle(
        val results: List<HandLandmarkerResult>,
        val inferenceTime: Long
    )

    companion object {
        const val TAG = "HandLandmarkerHelper"
        private const val MP_HAND_LANDMARKER_TASK = "hand_landmarker.task"

        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DEFAULT_HAND_DETECTION_CONFIDENCE = 0.5F
        const val DEFAULT_HAND_TRACKING_CONFIDENCE = 0.5F
        const val DEFAULT_HAND_PRESENCE_CONFIDENCE = 0.5F
        const val DEFAULT_NUM_HANDS = 2
        const val OTHER_ERROR = 0
        const val GPU_ERROR = 1
    }
}