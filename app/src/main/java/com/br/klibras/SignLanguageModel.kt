package com.br.klibras

import android.content.res.AssetManager
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SignLanguageModel(assetManager: AssetManager) {
    var interpreter: Interpreter? = null
    private var isModelLoaded = false

    init {
        try {
            interpreter = Interpreter(loadModelFile(assetManager, "SlModel.tflite"))
            isModelLoaded = true
            Log.d("SignLanguageModel", "Modelo carregado com sucesso!")
        } catch (e: Exception) {
            Log.e("SignLanguageModel", "Erro ao carregar modelo: ${e.message}")
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

    fun predict(input: Array<Array<FloatArray>>): FloatArray? {
        if (!isModelLoaded) {
            Log.e("SignLanguageModel", "Modelo não foi carregado corretamente")
            return null
        }

        return try {
            val output = Array(1) { FloatArray(2) }
            interpreter?.run(input, output)
            output[0]
        } catch (e: Exception) {
            Log.e("SignLanguageModel", "Erro na predição: ${e.message}")
            null
        }
    }

    fun close() {
        interpreter?.close()
    }
}