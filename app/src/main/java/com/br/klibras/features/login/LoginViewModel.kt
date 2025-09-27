package com.br.klibras.features.login

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.br.klibras.core.service.api.LoginRequest
import com.br.klibras.core.service.api.UserService
import com.br.klibras.util.RetrofitInstance
import com.br.klibras.core.utils.TokenManager
import kotlinx.coroutines.launch


sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Error(val message: String) : LoginUiState()
    object Success : LoginUiState()
}


class LoginViewModel(application: Application) : AndroidViewModel(application) {


    var loginUiState: LoginUiState by mutableStateOf(LoginUiState.Idle)
        private set


    private val userApiService: UserService = RetrofitInstance.userApiService


    fun login(context: Context, email: String, password: String) {
        if (loginUiState is LoginUiState.Loading) {
            return
        }

        loginUiState = LoginUiState.Loading

        viewModelScope.launch {
            try {
                val response = userApiService.login(LoginRequest(username = email, password = password))

                if (response.isSuccessful && response.code() == 200 && response.body() != null) {
                    val loginResponse = response.body()!!
                    TokenManager.saveTokens(
                        getApplication(),
                        loginResponse.accessToken,
                        loginResponse.refreshToken
                    )
                    loginUiState = LoginUiState.Success
                } else {
                    val errorMsg = "Login failed: Code ${response.code()} - ${response.message()}"
                    loginUiState = LoginUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "An unexpected error occurred: ${e.message}"
                loginUiState = LoginUiState.Error(errorMsg)
            }
        }
    }

    fun dismissError() {
        loginUiState = LoginUiState.Idle
    }
}

