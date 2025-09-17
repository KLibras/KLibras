package com.br.klibras

import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SignLanguageModel(assetManager: AssetManager) {
    private var interpreter: Interpreter? = null

    init {
        try {
            interpreter = Interpreter(loadModelFile(assetManager, "SlModel.tflite"))
            Log.d("SignLanguageModel", "✅ Modelo carregado com sucesso!")
        } catch (e: Exception) {
            Log.e("SignLanguageModel", "❌ Erro ao carregar modelo: ${e.message}")
        }
    }

    private fun loadModelFile(assetManager: AssetManager, modelName: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = assetManager.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun testRun(): Boolean {
        return try {
            val input = Array(1) { Array(30) { FloatArray(258) } } // 30 frames, 258 features
            val output = Array(1) { FloatArray(2) } // 2 classes: "obrigado" e "nada"
            interpreter?.run(input, output)
            Log.d("SignLanguageModel", "✅ Teste de inferência executado, saída: ${output[0].contentToString()}")
            true
        } catch (e: Exception) {
            Log.e("SignLanguageModel", "❌ Erro na inferência: ${e.message}")
            false
        }
    }

    fun close() {
        interpreter?.close()
    }
}
