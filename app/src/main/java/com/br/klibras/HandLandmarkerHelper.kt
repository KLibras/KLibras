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
    var minHandDetectionConfidence: Float = DEFAULT_HAND_DETECTION_CONFIDENCE,
    var minHandTrackingConfidence: Float = DEFAULT_HAND_TRACKING_CONFIDENCE,
    var minHandPresenceConfidence: Float = DEFAULT_HAND_PRESENCE_CONFIDENCE,
    var maxNumHands: Int = DEFAULT_NUM_HANDS,
    var currentDelegate: Int = DELEGATE_CPU,
    var runningMode: RunningMode = RunningMode.LIVE_STREAM,
    val context: Context,


    // Necessário pra fazer o uso do LIVE_STREAM
    val handLandmarkerHelperListener: LandmarkerListener? = null
) {

    private var handLandmarker: HandLandmarker? = null

    init {
        setupHandLandmarker()
    }

    // Fecha o helper pra poupar recursos
    fun isClosed(): Boolean {
        return handLandmarker == null
    }


    // Limpa o helper pra poupar recursos
    fun clearHandLandmarker() {
        handLandmarker?.close()
        handLandmarker = null
    }


    // Builder
    fun setupHandLandmarker() {
        // Setta as opções em geral
        val baseOptionsBuilder = BaseOptions.builder()

        // Poder "escolher" o que vai usar gpu ou cpu
        when (currentDelegate) {
            DELEGATE_CPU -> {
                baseOptionsBuilder.setDelegate(Delegate.CPU)
            }
            DELEGATE_GPU -> {
                baseOptionsBuilder.setDelegate(Delegate.GPU)
            }
        }

        baseOptionsBuilder.setModelAssetPath(MP_HAND_LANDMARKER_TASK)

        // Garante que o modo ta em LIVE_STREAM
        if (runningMode == RunningMode.LIVE_STREAM && handLandmarkerHelperListener == null) {
            throw IllegalStateException(
                "Precisa de um listener para rodar em modo LIVE_STREAM"
            )
        }

        try {
            val baseOptions = baseOptionsBuilder.build()

            val optionsBuilder = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(runningMode)
                .setNumHands(maxNumHands)
                .setMinHandDetectionConfidence(minHandDetectionConfidence)
                .setMinTrackingConfidence(minHandTrackingConfidence)
                .setMinHandPresenceConfidence(minHandPresenceConfidence)


            if (runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder
                    .setResultListener(this::returnLivestreamResult)
                    .setErrorListener(this::returnLivestreamError)
            }

            val options = optionsBuilder.build()
            handLandmarker = HandLandmarker.createFromOptions(context, options)

        } catch (e: IllegalStateException) {
            handLandmarkerHelperListener?.onError(
                "Landmarker falhou em iniciar",
                OTHER_ERROR
            )
            Log.e(TAG, "MediaPipe falhou em inicar: ${e.message}")
        } catch (e: RuntimeException) {
            // Vai chamar isso aqui se a GPU não conseguir assumir
            handLandmarkerHelperListener?.onError(
                "Landmarker falhou em iniciar",
                GPU_ERROR
            )
            Log.e(TAG, "MediaPipe failed to load the task with error: ${e.message}")
        }
    }

    // Processa os frames 1 por 1
    fun detectLiveStream(bitmap: Bitmap, isFrontCamera: Boolean) {
        if (runningMode != RunningMode.LIVE_STREAM) {
            throw IllegalArgumentException("This function is only for LIVE_STREAM mode.")
        }
        if (isClosed()) {
            return
        }

        val frameTime = SystemClock.uptimeMillis()
        val mpImage = BitmapImageBuilder(bitmap).build()

        handLandmarker?.detectAsync(mpImage, frameTime)
    }

    // Passa o resultado pro UI
    private fun returnLivestreamResult(result: HandLandmarkerResult, input: MPImage) {
        val finishTime = SystemClock.uptimeMillis()
        val inferenceTime = finishTime - result.timestampMs()

        handLandmarkerHelperListener?.onResults(
            ResultBundle(
                listOf(result),
                inferenceTime,
                input.height,
                input.width
            )
        )
    }


    private fun returnLivestreamError(error: RuntimeException) {
        handLandmarkerHelperListener?.onError(
            error.message ?: "An unknown error has occurred",
            OTHER_ERROR
        )
    }


    companion object {
        const val TAG = "HandLandmarkerHelper"
        private const val MP_HAND_LANDMARKER_TASK = "hand_landmarker.task" // Seleciona a task do /assets

        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1 // Define a GPU como primera escolha
        const val DEFAULT_HAND_DETECTION_CONFIDENCE = 0.5F // Esse e os dois de baixo define a confiança do landmarker em achar as mãos, 0.5 ta bom pra testar mas idealmente é 0.7> em produção
        const val DEFAULT_HAND_TRACKING_CONFIDENCE = 0.5F
        const val DEFAULT_HAND_PRESENCE_CONFIDENCE = 0.5F
        const val DEFAULT_NUM_HANDS = 2 // Define o número de mãos
        const val OTHER_ERROR = 0
        const val GPU_ERROR = 1
    }

    data class ResultBundle(
        val results: List<HandLandmarkerResult>,
        val inferenceTime: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int,
    )

    interface LandmarkerListener {
        fun onError(error: String, errorCode: Int = OTHER_ERROR)
        fun onResults(resultBundle: ResultBundle)
    }
}