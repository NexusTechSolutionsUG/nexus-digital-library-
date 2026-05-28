package com.example.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthRepository
import com.example.data.auth.AuthRepositoryImpl
import com.example.data.auth.AuthResponse
import com.example.data.auth.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthState {
    object Unauthenticated : AuthState
    object Loading : AuthState
    data class Authenticated(val session: AuthResponse) : AuthState
    data class Error(val message: String) : AuthState
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository: AuthRepository = AuthRepositoryImpl(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentSession.collect { session ->
                if (session != null) {
                    _authState.value = AuthState.Authenticated(session)
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.login(email, password)
                .onSuccess { session ->
                    _authState.value = AuthState.Authenticated(session)
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Invalid email or password.")
                }
        }
    }

    fun signUp(fullName: String, email: String, password: String, role: UserRole) {
        if (fullName.isBlank()) {
            _authState.value = AuthState.Error("Full Name cannot be empty.")
            return
        }
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty.")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.signUp(fullName, email, password, role)
                .onSuccess { session ->
                    _authState.value = AuthState.Authenticated(session)
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Authentication failed.")
                }
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
