package com.br.klibras.features.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.br.klibras.core.service.api.User
import com.br.klibras.core.utils.RetrofitInstance
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Data class para representar o estado da UI da tela de conta
data class AccountUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val conqueredSigns: Int = 0,
    val error: String? = null
)

class AccountViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private val userService = RetrofitInstance.getUserServiceApi(application)

    fun loadAccountData() {
        // Evita recarregar os dados se eles já foram carregados com sucesso.
        if (!_uiState.value.isLoading && _uiState.value.user != null) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // Usa coroutineScope e async para fazer as duas chamadas de API em paralelo.
                coroutineScope {
                    val userDeferred = async { userService.getCurrentUser() }
                    val signsDeferred = async { userService.getKnownSigns() }

                    val userResponse = userDeferred.await()
                    val signsResponse = signsDeferred.await()

                    if (userResponse.isSuccessful && signsResponse.isSuccessful) {
                        // Ambas as chamadas foram bem-sucedidas.
                        val user = userResponse.body()
                        val signsCount = signsResponse.body()?.size ?: 0

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                user = user,
                                conqueredSigns = signsCount,
                                error = null
                            )
                        }
                    } else {
                        // Se uma das chamadas falhou, define o estado de erro.
                        val errorMessage = userResponse.message().takeIf { !userResponse.isSuccessful } 
                            ?: signsResponse.message()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Erro ao carregar dados: $errorMessage"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Falha na conexão: ${e.message}")
                }
            }
        }
    }
}