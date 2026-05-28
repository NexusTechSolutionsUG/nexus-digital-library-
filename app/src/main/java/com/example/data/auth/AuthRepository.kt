package com.example.data.auth

import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentSession: StateFlow<AuthResponse?>

    suspend fun signUp(
        fullName: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<AuthResponse>

    suspend fun login(
        email: String,
        password: String
    ): Result<AuthResponse>

    suspend fun logout()

    fun loadSavedSession(): AuthResponse?
}
