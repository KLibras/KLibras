package com.br.klibras

import android.content.res.AssetManager
import android.util.Log
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import org.tensorflow.lite.Interpreter

class SignLanguageModel(assetManager: AssetManager) {
    private var interpreter: Interpreter? = null

    init {
        try {
            interpreter = Interpreter(loadModelFile(assetManager, "SlModel.tflite"))
            // Log the model's expected input details to help debug
            val inputTensor = interpreter?.getInputTensor(0)
            val inputShape = inputTensor?.shape()?.joinToString()
            val inputType = inputTensor?.dataType()
            Log.d("SignLanguageModel", "TFLite model loaded. Expected Input Shape: [${inputShape}], Input Type: ${inputType}")

        } catch (e: Exception) {
            Log.e("SignLanguageModel", "Error loading TFLite model", e)
        }
    }

    private fun loadModelFile(assetManager: AssetManager, modelName: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun predict(input: ByteBuffer): FloatArray? {
        if (interpreter == null) {
            Log.e("SignLanguageModel", "Interpreter is null, can't run prediction.")
            return null
        }
        return try {
            // The output array shape must match the model's output tensor shape.
            // For 2 classes, it's [1, 2].
            val output = Array(1) { FloatArray(2) }
            interpreter?.run(input, output)
            output[0]
        } catch (e: Exception) {
            Log.e("SignLanguageModel", "Error during prediction. Input buffer capacity: ${input.capacity()}", e)
            // The exception 'e' is now logged, which will give us the full error trace.
            null
        }
    }
}