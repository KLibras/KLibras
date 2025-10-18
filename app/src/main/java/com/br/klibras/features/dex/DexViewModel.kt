package com.br.klibras.features.dex

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.br.klibras.core.service.api.Sign
import com.br.klibras.core.utils.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DexState {
    object Loading : DexState()
    data class Success(val knownSigns: List<Sign>) : DexState()
    data class Error(val message: String) : DexState()
}

class DexViewModel : ViewModel() {

    private val _dexState = MutableStateFlow<DexState>(DexState.Loading)
    val dexState: StateFlow<DexState> = _dexState.asStateFlow()

    fun loadKnownSigns(context: Context) {
        viewModelScope.launch {
            try {
                _dexState.value = DexState.Loading

                val userService = RetrofitInstance.getUserServiceApi(context)
                val response = userService.getKnownSigns()

                if (response.isSuccessful) {
                    val signs = response.body() ?: emptyList()
                    _dexState.value = DexState.Success(signs)
                } else {
                    _dexState.value = DexState.Error("Falha ao carregar sinais: ${response.code()}")
                }
            } catch (e: Exception) {
                _dexState.value = DexState.Error("Erro: ${e.message}")
            }
        }
    }
}