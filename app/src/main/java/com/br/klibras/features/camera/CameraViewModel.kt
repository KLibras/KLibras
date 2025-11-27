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

// TAG para filtrar os logs no Logcat
private const val TAG = "VideoUpload"

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
        Log.d(TAG, "--- Início do Processamento ---")
        Log.d(TAG, "Arquivo de vídeo: ${videoFile.absolutePath}, Tamanho: ${videoFile.length()} bytes")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val normalizedSignName = signName.lowercase().replace(" ", "_")
<<<<<<< Updated upstream
=======
                Log.d(TAG, "Sinal esperado (normalizado): '$normalizedSignName'")
>>>>>>> Stashed changes

                val videoService = RetrofitInstance.getVideoProcessingService(context)
                val videoPart = MultipartBody.Part.createFormData(
                    "video",
                    videoFile.name,
                    videoFile.asRequestBody("video/mp4".toMediaType())
                )

<<<<<<< Updated upstream
                Log.d("VideoUpload", "Uploading video for sign: $normalizedSignName (original: $signName)")
=======
                Log.d(TAG, "[1/3] Fazendo upload do vídeo...")
>>>>>>> Stashed changes
                val uploadResponse = videoService.uploadVideo(normalizedSignName, videoPart).execute()

                if (!uploadResponse.isSuccessful) {
                    val errorBody = uploadResponse.errorBody()?.string()
                    Log.e(TAG, "[FALHA] Upload falhou. Código: ${uploadResponse.code()}. Corpo do Erro: $errorBody")
                    _processingState.value = ProcessingState.Error("Upload falhou: ${uploadResponse.code()}")
                    onComplete(null)
                    return@launch
                }

                val jobId = uploadResponse.body()?.jobId
                if (jobId == null) {
                    Log.e(TAG, "[FALHA] Upload bem-sucedido, mas nenhum job ID foi retornado.")
                    _processingState.value = ProcessingState.Error("No job ID returned")
                    onComplete(null)
                    return@launch
                }

<<<<<<< Updated upstream
                Log.d("VideoUpload", "Video uploaded with job ID: $jobId")
                Log.d("VideoUpload", "Waiting 2 seconds before checking result...")
=======
                Log.d(TAG, "[2/3] Upload bem-sucedido! Job ID: $jobId")
                Log.d(TAG, "Aguardando 2 segundos antes de consultar o resultado...")
>>>>>>> Stashed changes
                Thread.sleep(2000)

                Log.d(TAG, "[3/3] Iniciando consulta (polling) do resultado...")
                val result = pollJobResult(videoService, jobId)

                if (result != null) {
                    Log.d(TAG, "--- Processamento Concluído. Resultado Final: $result ---")
                    _processingState.value = ProcessingState.Success(result)

                    if (result) {
                        Log.d(TAG, "Resultado foi SUCESSO. Adicionando sinal ao usuário...")
                        addSignToUser(context, signId)
                    }

                    onComplete(result)
                } else {
                    Log.e(TAG, "[FALHA] Não foi possível obter o resultado do processamento após várias tentativas.")
                    _processingState.value = ProcessingState.Error("Failed to get result")
                    onComplete(null)
                }

            } catch (e: java.net.SocketTimeoutException) {
                Log.e(TAG, "[FALHA] Timeout de rede: ${e.message}")
                _processingState.value = ProcessingState.Error("Timeout de rede. Verifique sua conexão.")
                onComplete(null)
            } catch (e: Exception) {
                Log.e(TAG, "[FALHA] Exceção inesperada: ${e.message}", e)
                _processingState.value = ProcessingState.Error(e.message ?: "Erro desconhecido")
                onComplete(null)
            } finally {
                Log.d(TAG, "Limpando arquivo de vídeo temporário.")
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
                    Log.d(TAG, "Sinal (ID: $signId) adicionado ao usuário com sucesso.")
                } else {
                    Log.e(TAG, "Falha ao adicionar sinal (ID: $signId) ao usuário. Código: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exceção ao adicionar sinal ao usuário: ${e.message}")
            }
        }
    }

    fun addModuleToUser(context: Context, moduleId: Int, onComplete: (Boolean) -> Unit) {
        // ... (código existente)
    }

    private fun pollJobResult(
        videoService: com.br.klibras.core.service.api.RecognitionService,
        jobId: String
    ): Boolean? {
<<<<<<< Updated upstream
        val maxAttempts = 3
=======
        val maxAttempts = 5 // Aumentado para mais chances
>>>>>>> Stashed changes

        for (attempt in 1..maxAttempts) {
            try {
                Log.d(TAG, "Consultando resultado... (Tentativa $attempt de $maxAttempts)")

<<<<<<< Updated upstream
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
=======
                val resultResponse = videoService.getResult(jobId = jobId, wait = false).execute()

                if (!resultResponse.isSuccessful) {
                    Log.e(TAG, "Falha na consulta. Código: ${resultResponse.code()}")
                    if (attempt < maxAttempts) Thread.sleep(1500) // Espera um pouco mais
>>>>>>> Stashed changes
                    continue
                }

                val jobResult = resultResponse.body()
                Log.d(TAG, "Resposta da consulta: $jobResult")

                when (jobResult?.status) {
                    "completed" -> {
                        val isMatch = jobResult.isMatch ?: false
                        Log.d(TAG, "Status: COMPLETED. Resultado do match: $isMatch. Ação prevista: ${jobResult.predictedAction}, Confiança: ${jobResult.confidence}")
                        return isMatch
                    }
                    "failed" -> {
                        Log.e(TAG, "Status: FAILED. Erro retornado pela API: ${jobResult.error}")
                        return null // Retorna falha
                    }
                    "processing", "pending" -> {
<<<<<<< Updated upstream
                        Log.d("VideoUpload", "Still processing...")
                        if (attempt < maxAttempts) {
                            Thread.sleep(1000)
                        } else {
                            Log.e("VideoUpload", "Timeout after $maxAttempts attempts")
                            return null
                        }
=======
                        Log.d(TAG, "Status: ${jobResult.status}. Aguardando...")
                        if (attempt < maxAttempts) Thread.sleep(1500)
>>>>>>> Stashed changes
                    }
                    else -> {
                        Log.e(TAG, "Status desconhecido: '${jobResult?.status}'")
                        return null // Status inesperado
                    }
                }
            } catch (e: Exception) {
<<<<<<< Updated upstream
                Log.e("VideoUpload", "Polling error on attempt $attempt: ${e.message}")
                if (attempt == maxAttempts) {
                    return null
                }
                Thread.sleep(1000)
=======
                Log.e(TAG, "Exceção na consulta (Tentativa $attempt): ${e.message}")
                if (attempt < maxAttempts) Thread.sleep(1500)
>>>>>>> Stashed changes
            }
        }
        Log.e(TAG, "Timeout! O trabalho não foi concluído após $maxAttempts tentativas.")
        return null // Timeout
    }

    fun resetState() {
        _processingState.value = ProcessingState.Idle
    }
}