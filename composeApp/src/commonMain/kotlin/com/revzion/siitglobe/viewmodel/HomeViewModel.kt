package com.revzion.siitglobe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revzion.siitglobe.data.model.User
import com.revzion.siitglobe.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: String? = null,
    val isSignedOut: Boolean = false,
)

class HomeViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _state.value = HomeUiState(isLoading = true)
            repository.getUser()
                .onSuccess { user ->
                    _state.value = HomeUiState(isLoading = false, user = user)
                }
                .onFailure { e ->
                    _state.value = HomeUiState(isLoading = false, error = e.message)
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            _state.value = _state.value.copy(isSignedOut = true)
        }
    }
}
