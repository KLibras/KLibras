package com.br.klibras.features.ranking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.br.klibras.core.service.api.User
import com.br.klibras.core.utils.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RankingUiState(
    val isLoading: Boolean = true,
    val users: List<User> = emptyList(),
    val currentUserRank: Int? = null,
    val error: String? = null
)

class RankingViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    private val userService = RetrofitInstance.getUserServiceApi(application)

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val leaderboardResponse = userService.getLeaderboard()
                val currentUserResponse = userService.getCurrentUser()

                if (leaderboardResponse.isSuccessful && currentUserResponse.isSuccessful) {
                    val allUsers = leaderboardResponse.body() ?: emptyList()
                    val currentUser = currentUserResponse.body()


                    val topUsers = allUsers.take(10)


                    val currentUserRank = allUsers.indexOfFirst { it.id == currentUser?.id }.let {
                        if (it >= 0) it + 1 else null
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            users = topUsers,
                            currentUserRank = currentUserRank,
                            error = null
                        )
                    }
                } else {
                    val errorMessage = "Erro ao carregar ranking: ${leaderboardResponse.code()}"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Falha na conexão: ${e.message}"
                    )
                }
            }
        }
    }

}