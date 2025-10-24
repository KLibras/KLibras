package com.br.klibras.features.account

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.br.klibras.core.service.api.PasswordUpdate
import com.br.klibras.core.utils.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "ChangePasswordViewModel"

sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    data class Success(val message: String) : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}

class ChangePasswordViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val state: StateFlow<ChangePasswordState> = _state

    private val userService = RetrofitInstance.getUserServiceApi(application)

    fun updatePassword(newPassword: String) {
        if (newPassword.isBlank()) {
            _state.value = ChangePasswordState.Error("A senha não pode estar vazia")
            return
        }

        if (newPassword.length < 6) {
            _state.value = ChangePasswordState.Error("A senha deve ter pelo menos 6 caracteres")
            return
        }

        viewModelScope.launch {
            _state.value = ChangePasswordState.Loading
            try {
                val response = userService.updatePassword(PasswordUpdate(new_password = newPassword))

                if (response.isSuccessful) {
                    _state.value = ChangePasswordState.Success("Senha alterada com sucesso!")
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "Senha inválida"
                        401 -> "Sessão expirada. Faça login novamente"
                        else -> "Erro ao atualizar senha: ${response.code()}"
                    }
                    Log.e(TAG, "Erro ao atualizar senha: ${response.code()} - ${response.message()}")
                    _state.value = ChangePasswordState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exceção ao atualizar senha", e)
                _state.value = ChangePasswordState.Error("Falha na conexão. Verifique sua internet.")
            }
        }
    }

    fun resetState() {
        _state.value = ChangePasswordState.Idle
    }
}