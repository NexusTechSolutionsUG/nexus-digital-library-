package com.example.data.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SupabaseAuthApi {

    @POST("auth/v1/signup")
    suspend fun signUp(
        @Header("apikey") apiKey: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: SignUpRequest
    ): Response<AuthResponse>

    @POST("auth/v1/token?grant_type=password")
    suspend fun login(
        @Header("apikey") apiKey: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: LoginRequest
    ): Response<AuthResponse>
}
