package com.revzion.siitglobe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revzion.siitglobe.data.model.User
import com.revzion.siitglobe.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isSuccess: Boolean = false,
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(error = "Email and password are required")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            repository.signIn(email, password)
                .onSuccess { user ->
                    _state.value = AuthUiState(user = user, isSuccess = true)
                }
                .onFailure { e ->
                    _state.value = AuthUiState(error = e.message ?: "Sign in failed")
                }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(error = "All fields are required")
            return
        }
        if (password.length < 8) {
            _state.value = _state.value.copy(error = "Password must be at least 8 characters")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            repository.signUp(name, email, password)
                .onSuccess { user ->
                    _state.value = AuthUiState(user = user, isSuccess = true)
                }
                .onFailure { e ->
                    _state.value = AuthUiState(error = e.message ?: "Registration failed")
                }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
