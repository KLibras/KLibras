package com.br.klibras.features.gesture

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.br.klibras.core.service.api.Module
import com.br.klibras.core.utils.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ModuleUiState(
    val isLoading: Boolean = true,
    val module: Module? = null,
    val error: String? = null
)

class GestureViewModel(application: Application) : AndroidViewModel(application) {

    private val userService = RetrofitInstance.getUserServiceApi(application)

    private val _uiState = MutableStateFlow(ModuleUiState())
    val uiState: StateFlow<ModuleUiState> = _uiState.asStateFlow()

    fun fetchModule(moduleName: String) {
        _uiState.value = ModuleUiState(isLoading = true)

        viewModelScope.launch {
            try {
                val response = userService.getModuleByName(moduleName)

                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(isLoading = false, module = response.body())
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Erro ao buscar módulo: ${response.code()}")
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