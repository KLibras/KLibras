package com.br.klibras.features.main

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.br.klibras.core.service.api.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream

sealed class UiState {
    data class Ready(val message: String = "Tap the button to record a 3-second video.") : UiState()
    object Recording : UiState()
    data class Uploading(val message: String = "Uploading and analyzing...") : UiState()
    data class Success(val result: String) : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<UiState>(UiState.Ready())
    val uiState: StateFlow<UiState> = _uiState

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.15.8:8000") // tem que mudar isso aqui também
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun uploadVideoForAnalysis(videoUri: Uri?) {
        if (videoUri == null) {
            _uiState.value = UiState.Error("Recording failed. Please try again.")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Uploading()
            try {
                val context = getApplication<Application>()
                val file = File(context.cacheDir, "upload.mp4")
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

                val response = apiService.checkAction(expectedActionPart, videoPart)

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    val resultMessage = """
                        Action Found: ${result.action_found}
                        Predicted: ${result.predicted_action}
                        Confidence: ${result.confidence}
                    """.trimIndent()
                    _uiState.value = UiState.Success(resultMessage)
                } else {
                    val errorBody = response.errorBody()?.string()
                    _uiState.value = UiState.Error("API Error: ${response.code()} - $errorBody")
                }

            } catch (e: Exception) {
                _uiState.value = UiState.Error("Upload failed: ${e.message}")
                Log.e("MainViewModel", "Error uploading video", e)
            }
        }
    }

    fun resetToReadyState() {
        _uiState.value = UiState.Ready()
    }
}