package com.br.klibras.features.account

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.br.klibras.core.service.api.UsernameUpdate
import com.br.klibras.core.utils.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "ChangeUsernameViewModel"

sealed class ChangeUsernameState {
    object Idle : ChangeUsernameState()
    object Loading : ChangeUsernameState()
    data class Success(val message: String) : ChangeUsernameState()
    data class Error(val message: String) : ChangeUsernameState()
}

class ChangeUsernameViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow<ChangeUsernameState>(ChangeUsernameState.Idle)
    val state: StateFlow<ChangeUsernameState> = _state

    private val userService = RetrofitInstance.getUserServiceApi(application)

    fun updateUsername(newUsername: String) {
        if (newUsername.isBlank()) {
            _state.value = ChangeUsernameState.Error("O username não pode estar vazio")
            return
        }

        if (newUsername.length < 3) {
            _state.value = ChangeUsernameState.Error("O username deve ter pelo menos 3 caracteres")
            return
        }

        viewModelScope.launch {
            _state.value = ChangeUsernameState.Loading
            try {
                val response = userService.updateUsername(UsernameUpdate(new_username = newUsername))

                if (response.isSuccessful) {
                    _state.value = ChangeUsernameState.Success("Username alterado com sucesso!")
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "Username inválido ou já existe"
                        401 -> "Sessão expirada. Faça login novamente"
                        else -> "Erro ao atualizar username: ${response.code()}"
                    }
                    Log.e(TAG, "Erro ao atualizar username: ${response.code()} - ${response.message()}")
                    _state.value = ChangeUsernameState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exceção ao atualizar username", e)
                _state.value = ChangeUsernameState.Error("Falha na conexão. Verifique sua internet.")
            }
        }
    }

    fun resetState() {
        _state.value = ChangeUsernameState.Idle
    }
}