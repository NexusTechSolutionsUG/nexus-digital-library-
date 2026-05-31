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
    data class SuccessMessage(val message: String) : AuthState
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

    fun login(email: String, password: String, loginTab: LoginTab? = null) {
        val cleanEmail = email.lowercase().trim()
        if (cleanEmail.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty.")
            return
        }
        // Strict upfront email domain check for staff portals
        if (loginTab != null && loginTab != LoginTab.STUDENT) {
            if (!cleanEmail.endsWith("@nexustech.edu")) {
                _authState.value = AuthState.Error("Designated portal access denied: Staff must sign in using a verified school email address ending in @nexustech.edu.")
                return
            }
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.login(cleanEmail, password)
                .onSuccess { session ->
                    // Post-auth security validation & compartment check
                    val userRoleStr = session.user?.userMetadata?.role ?: "student"
                    val actualRole = when(userRoleStr.lowercase()) {
                        "teacher" -> LoginTab.TEACHER
                        "librarian" -> LoginTab.LIBRARIAN
                        "admin", "super_admin" -> LoginTab.ADMINISTRATOR
                        else -> LoginTab.STUDENT
                    }

                    if (loginTab != null && actualRole != loginTab) {
                        val expectedPortal = when (loginTab) {
                            LoginTab.STUDENT -> "Student Portal"
                            LoginTab.TEACHER -> "Teacher Desk"
                            LoginTab.LIBRARIAN -> "Librarian Terminal"
                            LoginTab.ADMINISTRATOR -> "Admin Center"
                        }
                        val registeredRoleName = actualRole.name.lowercase().replaceFirstChar { it.uppercase() }
                        _authState.value = AuthState.Error("Unauthorized role access: Access Denied. Your account is registered as $registeredRoleName and is denied access to the $expectedPortal. Please use the appropriate portal.")
                        authRepository.logout()
                    } else {
                        _authState.value = AuthState.Authenticated(session)
                    }
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error("Invalid credentials: ${exception.message ?: "The credentials provided are invalid."}")
                }
        }
    }

    fun loginWithStudentId(studentId: String, password: String) {
        val cleanId = studentId.trim()
        if (cleanId.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Student ID and password cannot be empty.")
            return
        }
        val email = authRepository.lookupEmailForStudentId(cleanId)
        if (email == null) {
            _authState.value = AuthState.Error("Invalid Student ID: This Student ID is not registered in our database. Please contact an authorized administrator.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.login(email, password)
                .onSuccess { session ->
                    val userRoleStr = session.user?.userMetadata?.role ?: "student"
                    val isStudent = userRoleStr.equals("student", ignoreCase = true)
                    if (!isStudent) {
                        _authState.value = AuthState.Error("Unauthorized role access: This academic account possesses Staff privileges and cannot access the Student Portal.")
                        authRepository.logout()
                    } else {
                        _authState.value = AuthState.Authenticated(session)
                    }
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error("Invalid credentials: ${exception.message ?: "Invalid Student ID or password."}")
                }
        }
    }

    fun signUpStudent(fullName: String, studentId: String, password: String) {
        val cleanId = studentId.trim()
        if (fullName.isBlank()) {
            _authState.value = AuthState.Error("Full Name cannot be empty.")
            return
        }
        if (!fullName.trim().contains(" ")) {
            _authState.value = AuthState.Error("Full Name must contain both first and last name.")
            return
        }
        if (cleanId.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Student ID and password cannot be empty.")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters.")
            return
        }
        val email = "${cleanId.lowercase()}@student.nexus.edu"
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.signUp(fullName, email, password, UserRole.STUDENT)
                .onSuccess { session ->
                    authRepository.registerStudentIdMapping(cleanId, email)
                    _authState.value = AuthState.SuccessMessage(
                        "Student profile registered successfully! Students bypass email verification. You can sign in immediately using your Student ID: $cleanId."
                    )
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Authentication failed.")
                }
        }
    }

    fun signUpStaff(fullName: String, email: String, password: String, role: UserRole, staffAccessCode: String) {
        if (fullName.isBlank()) {
            _authState.value = AuthState.Error("Full Name cannot be empty.")
            return
        }
        if (!fullName.trim().contains(" ")) {
            _authState.value = AuthState.Error("Full Name must contain both first and last name.")
            return
        }
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty.")
            return
        }
        if (!cleanEmail.endsWith("@nexustech.edu")) {
            _authState.value = AuthState.Error("Registration Denied: High School Staff profiles must be created using a verified school domain ending in @nexustech.edu.")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters.")
            return
        }
        if (staffAccessCode.trim() != "NEXUSTECH2026") {
            _authState.value = AuthState.Error("Security Code Invalid: Please enter the active organization-approved Staff Registration Security Code to authorize your staff profile role.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.signUp(fullName, cleanEmail, password, role)
                .onSuccess { session ->
                    if (session.accessToken.isNullOrEmpty()) {
                        _authState.value = AuthState.SuccessMessage(
                            "Profile Registered Successfully! Please check your nexustech.edu email inbox to confirm your account before logging in."
                        )
                    } else {
                        _authState.value = AuthState.Authenticated(session)
                    }
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Authentication failed.")
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
                    if (session.accessToken.isNullOrEmpty()) {
                        _authState.value = AuthState.SuccessMessage(
                            "Profile Registered Successfully! Please check your email inbox to confirm your account before logging in."
                        )
                    } else {
                        _authState.value = AuthState.Authenticated(session)
                    }
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "Authentication failed.")
                }
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error || _authState.value is AuthState.SuccessMessage) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
