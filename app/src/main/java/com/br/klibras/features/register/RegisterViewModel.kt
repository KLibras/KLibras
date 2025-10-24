package com.br.klibras.features.register

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.br.klibras.core.service.api.AuthService
import com.br.klibras.core.utils.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

private const val TAG = "RegisterViewModel"

// Sealed class para gerenciar o estado da UI de forma segura
sealed class RegisterUiState {
    object Idle : RegisterUiState() // Estado inicial
    object Loading : RegisterUiState() // Estado de carregamento
    data class Success(val message: String) : RegisterUiState() // Estado de sucesso
    data class Error(val message: String) : RegisterUiState() // Estado de erro
}

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val _registerState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val registerState: StateFlow<RegisterUiState> = _registerState

    private val api: AuthService = RetrofitInstance.getUserAuthApi(application)

    fun register(email: String, username: String, password: String) {
        viewModelScope.launch {
            _registerState.value = RegisterUiState.Loading
            try {
                val jsonObject = JSONObject().apply {
                    put("email", email)
                    put("username", username)
                    put("password", password)
                    put("points", 0)
                }

                val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

                val response = api.register(requestBody)

                if (response.isSuccessful) {
                    _registerState.value = RegisterUiState.Success("Usuário registrado com sucesso!")
                } else {
                    // Mantém o log detalhado para depuração.
                    Log.e(TAG, "Falha no registro: ${response.code()} - ${response.errorBody()?.string()}")
                    // Mostra uma mensagem genérica para o usuário.
                    _registerState.value = RegisterUiState.Error("Falha no registro. Verifique os dados ou tente novamente.")
                }
            } catch (e: Exception) {
                // Mantém o log detalhado para depuração.
                Log.e(TAG, "Exceção no registro", e)
                // Mostra uma mensagem genérica para o usuário.
                _registerState.value = RegisterUiState.Error("Falha na conexão. Verifique sua internet e tente novamente.")
            }
        }
    }
}