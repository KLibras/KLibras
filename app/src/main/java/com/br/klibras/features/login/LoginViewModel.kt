package com.br.klibras.features.login

import android.app.Application
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

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState

    private val api: AuthService = RetrofitInstance.getUserAuthApi(application)

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginUiState.value = LoginUiState.Loading
            _loginUiState.value = LoginUiState.Success("Login Sucessful!") //pulei o login por enquanto //
            /*try {
                val userRequestBody = username.toRequestBody("text/plain".toMediaTypeOrNull())
                val passRequestBody = password.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = api.login(userRequestBody, passRequestBody)

                if (response.isSuccessful && response.body() != null) {
                    val accessToken = response.body()!!.accessToken
                    val refreshToken = response.body()!!.refreshToken

                    TokenManager.saveTokens(getApplication(), accessToken, refreshToken)

                    _loginUiState.value = LoginUiState.Success("Login and token save successful!")

                } else {
                    _loginUiState.value = LoginUiState.Error("Login failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _loginUiState.value = LoginUiState.Error(e.message ?: "An unknown error occurred")
            }*/
        }
    }
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val message: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}