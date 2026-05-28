package com.example.data.auth

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class UserRole {
    SCHOOL,
    STUDENT,
    TEACHER,
    LIBRARIAN,
    ADMIN;

    val displayName: String
        get() = name.lowercase().replaceFirstChar { it.uppercase() }
}

@JsonClass(generateAdapter = true)
data class UserMetadata(
    @Json(name = "first_name") val firstName: String?,
    @Json(name = "last_name") val lastName: String?,
    val role: String?
)

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: String,
    val email: String?,
    @Json(name = "user_metadata") val userMetadata: UserMetadata?
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "expires_in") val expiresIn: Long,
    val user: UserDto
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class SignUpRequest(
    val email: String,
    val password: String,
    val data: Map<String, String>
)
