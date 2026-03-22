package com.example.parloenglish.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parloenglish.auth.AuthRepository
import com.example.parloenglish.auth.model.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(email, password)
            result.onSuccess { session ->
                _authState.value = AuthState.Authenticated(session)
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Errore durante il login")
            }
        }
    }

    fun register(email: String, password: String, name: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.register(email, password, name)
            result.onSuccess { session ->
                _authState.value = AuthState.Authenticated(session)
            }.onFailure { exception ->
                _authState.value = AuthState.Error(exception.message ?: "Errore durante la registrazione")
            }
        }
    }

    fun clearError() {
        _authState.value = AuthState.Unauthenticated
    }
}
