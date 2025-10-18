package com.br.klibras.features.camera

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.br.klibras.core.utils.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

sealed class ProcessingState {
    object Idle : ProcessingState()
    object Processing : ProcessingState()
    data class Success(val isMatch: Boolean) : ProcessingState()
    data class Error(val message: String) : ProcessingState()
}

class CameraViewModel : ViewModel() {

    private val _processingState = MutableStateFlow<ProcessingState>(ProcessingState.Idle)
    val processingState: StateFlow<ProcessingState> = _processingState.asStateFlow()

    fun processVideo(
        videoFile: File,
        signName: String,
        signId: Int,
        context: Context,
        onComplete: (Boolean?) -> Unit
    ) {
        _processingState.value = ProcessingState.Processing

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val normalizedSignName = signName.lowercase().replace(" ", "_")

                val videoService = RetrofitInstance.getVideoProcessingService(context)
                val videoPart = MultipartBody.Part.createFormData(
                    "video",
                    videoFile.name,
                    videoFile.asRequestBody("video/mp4".toMediaType())
                )

                Log.d("VideoUpload", "Uploading video for sign: $normalizedSignName (original: $signName)")
                val uploadResponse = videoService.uploadVideo(normalizedSignName, videoPart).execute()

                if (!uploadResponse.isSuccessful) {
                    _processingState.value = ProcessingState.Error("Upload failed: ${uploadResponse.code()}")
                    onComplete(null)
                    return@launch
                }

                val jobId = uploadResponse.body()?.jobId
                if (jobId == null) {
                    _processingState.value = ProcessingState.Error("No job ID returned")
                    onComplete(null)
                    return@launch
                }

                Log.d("VideoUpload", "Video uploaded with job ID: $jobId")
                Log.d("VideoUpload", "Waiting 2 seconds before checking result...")
                Thread.sleep(2000)

                val result = pollJobResult(videoService, jobId)
                if (result != null) {
                    _processingState.value = ProcessingState.Success(result)

                    if (result) {
                        addSignToUser(context, signId)
                    }

                    onComplete(result)
                } else {
                    _processingState.value = ProcessingState.Error("Failed to get result")
                    onComplete(null)
                }

            } catch (e: java.net.SocketTimeoutException) {
                Log.e("VideoUpload", "Network timeout: ${e.message}")
                _processingState.value = ProcessingState.Error("Timeout de rede. Verifique sua conexão.")
                onComplete(null)
            } catch (e: Exception) {
                Log.e("VideoUpload", "Error: ${e.message}", e)
                _processingState.value = ProcessingState.Error(e.message ?: "Erro desconhecido")
                onComplete(null)
            } finally {
                videoFile.delete()
            }
        }
    }

    private fun addSignToUser(context: Context, signId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userService = RetrofitInstance.getUserServiceApi(context)
                val response = userService.addKnownSign(signId)

                if (response.isSuccessful) {
                    Log.d("VideoUpload", "Sign added to user successfully")
                } else {
                    Log.e("VideoUpload", "Failed to add sign to user: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("VideoUpload", "Error adding sign to user: ${e.message}")
            }
        }
    }

    fun addModuleToUser(context: Context, moduleId: Int, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userService = RetrofitInstance.getUserServiceApi(context)
                val response = userService.addCompletedModule(moduleId)

                if (response.isSuccessful) {
                    Log.d("CameraViewModel", "Module added to user successfully")
                    onComplete(true)
                } else {
                    Log.e("CameraViewModel", "Failed to add module to user: ${response.code()}")
                    onComplete(false)
                }
            } catch (e: Exception) {
                Log.e("CameraViewModel", "Error adding module to user: ${e.message}")
                onComplete(false)
            }
        }
    }

    private fun pollJobResult(
        videoService: com.br.klibras.core.service.api.RecognitionService,
        jobId: String
    ): Boolean? {
        val maxAttempts = 3

        for (attempt in 1..maxAttempts) {
            try {
                Log.d("VideoUpload", "Checking result (attempt $attempt/$maxAttempts)...")

                val resultResponse = videoService.getResult(
                    jobId = jobId,
                    wait = false
                ).execute()

                if (!resultResponse.isSuccessful) {
                    Log.e("VideoUpload", "Failed to get result: ${resultResponse.code()}")
                    if (attempt == maxAttempts) {
                        return null
                    }
                    Thread.sleep(1000)
                    continue
                }

                val jobResult = resultResponse.body()

                when (jobResult?.status) {
                    "completed" -> {
                        val result = jobResult.actionFound ?: false
                        Log.d("VideoUpload", "Result: $result (confidence: ${jobResult.confidence})")
                        return result
                    }
                    "failed" -> {
                        Log.e("VideoUpload", "Job failed: ${jobResult.error}")
                        return null
                    }
                    "processing", "pending" -> {
                        Log.d("VideoUpload", "Still processing...")
                        if (attempt < maxAttempts) {
                            Thread.sleep(1000)
                        } else {
                            Log.e("VideoUpload", "Timeout after $maxAttempts attempts")
                            return null
                        }
                    }
                    else -> {
                        Log.e("VideoUpload", "Unknown status: ${jobResult?.status}")
                        return null
                    }
                }
            } catch (e: Exception) {
                Log.e("VideoUpload", "Polling error on attempt $attempt: ${e.message}")
                if (attempt == maxAttempts) {
                    return null
                }
                Thread.sleep(1000)
            }
        }
        return null
    }

    fun resetState() {
        _processingState.value = ProcessingState.Idle
    }
}