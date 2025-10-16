package com.br.klibras.features.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.br.klibras.core.service.api.AuthService
import com.br.klibras.core.utils.RetrofitInstance
import com.br.klibras.core.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

private const val TAG = "LoginViewModel"

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState

    private val api: AuthService = RetrofitInstance.getUserAuthApi(application)

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginUiState.value = LoginUiState.Loading
            try {
                // Monta o corpo da requisição para o login
                val userRequestBody = username.toRequestBody("text/plain".toMediaTypeOrNull())
                val passRequestBody = password.toRequestBody("text/plain".toMediaTypeOrNull())

                // Faz a chamada para a API
                val response = api.login(userRequestBody, passRequestBody)

                if (response.isSuccessful && response.body() != null) {
                    val accessToken = response.body()!!.accessToken
                    val refreshToken = response.body()!!.refreshToken

                    // Salva os tokens usando o TokenManager
                    TokenManager.saveTokens(getApplication(), accessToken, refreshToken)

                    _loginUiState.value = LoginUiState.Success("Login bem-sucedido!")

                } else {
                    // Loga o erro da API antes de notificar a UI
                    val errorMsg = "Login falhou: ${response.code()} - ${response.message()}"
                    Log.e(TAG, errorMsg)
                    _loginUiState.value = LoginUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                // Loga a exceção completa (incluindo a causa) no Logcat
                Log.e(TAG, "Falha na conexão ou erro inesperado", e)
                _loginUiState.value = LoginUiState.Error(e.message ?: "Ocorreu um erro desconhecido")
            }
        }
    }
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val message: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

