package com.br.klibras.features.camera

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.br.klibras.core.service.api.RecognitionService
import com.br.klibras.core.utils.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

sealed class UiState {
    data class Ready(val message: String = "Clique pra gravar") : UiState()
    object Recording : UiState()
    data class Uploading(val message: String = "Analisando") : UiState()
    data class Success(val result: String) : UiState()
    data class Error(val message: String) : UiState()
}

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<UiState>(UiState.Ready())
    val uiState: StateFlow<UiState> = _uiState

    private val recognitionApi: RecognitionService = RetrofitInstance.getRecognitionApi(application)

    fun uploadVideoForAnalysis(videoUri: Uri?) {
        if (videoUri == null) {
            _uiState.value = UiState.Error("Falhou")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Uploading()
            var file: File? = null
            try {
                val context = getApplication<Application>().applicationContext
                file = File(context.cacheDir, "upload.mp4")
                context.contentResolver.openInputStream(videoUri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }

                val videoPart = MultipartBody.Part.createFormData(
                    "video",
                    file.name,
                    file.asRequestBody("video/mp4".toMediaTypeOrNull())
                )
                val expectedActionPart = MultipartBody.Part.createFormData(
                    "expected_action", "obrigado"
                )

                val response = recognitionApi.uploadForAnalysis(expectedActionPart, videoPart)

                if (response.isSuccessful && response.body() != null) {
                    val jobId = response.body()!!.job_id
                    pollForResult(jobId)
                } else {
                    val errorBody = response.errorBody()?.string()
                    _uiState.value = UiState.Error("API Error: ${response.code()} - $errorBody")
                }

            } catch (e: Exception) {
                _uiState.value = UiState.Error("Upload failed: ${e.message}")
                Log.e("MainViewModel", "Error uploading video", e)
            } finally {
                file?.let {
                    if (it.exists()) {
                        it.delete()
                        Log.d("MainViewModel", "Cache file deleted: ${it.absolutePath}")
                    }
                }
            }
        }
    }

    private fun pollForResult(jobId: String) {
        viewModelScope.launch {
            val maxAttempts = 10
            var attempts = 0
            while (attempts < maxAttempts) {
                try {
                    val resultResponse = recognitionApi.getAnalysisResult(jobId)
                    if (resultResponse.isSuccessful && resultResponse.body() != null) {
                        when (resultResponse.body()!!.status) {
                            "completed" -> {
                                val result = resultResponse.body()!!.result!!
                                val resultMessage = """
                                    Ação Encontrada: ${result.action_found}
                                    Previsto: ${result.predicted_action}
                                    Confiança: ${result.confidence}
                                """.trimIndent()
                                _uiState.value = UiState.Success(resultMessage)
                                return@launch
                            }
                            "failed" -> {
                                _uiState.value = UiState.Error("Análise falhou: ${resultResponse.body()!!.error}")
                                return@launch
                            }
                            "processing" -> {
                                Log.d("MainViewModel", "Job $jobId ainda está processando...")
                            }
                        }
                    } else {
                        _uiState.value = UiState.Error("Erro ao buscar resultado: ${resultResponse.code()}")
                        return@launch
                    }
                } catch (e: Exception) {
                    _uiState.value = UiState.Error("Polling falhou: ${e.message}")
                    Log.e("MainViewModel", "Error polling for result", e)
                    return@launch
                }

                attempts++
                delay(2000)
            }

            _uiState.value = UiState.Error("A análise expirou. Tente novamente.")
        }
    }

    fun resetToReadyState() {
        _uiState.value = UiState.Ready()
    }
}
