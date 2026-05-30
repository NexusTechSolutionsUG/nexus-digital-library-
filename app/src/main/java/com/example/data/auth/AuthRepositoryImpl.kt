package com.example.data.auth

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

class AuthRepositoryImpl(context: Context) : AuthRepository {

    private val sharedPreferences = context.getSharedPreferences("nexus_auth_prefs", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val authResponseAdapter = moshi.adapter(AuthResponse::class.java)

    private val _currentSession = MutableStateFlow<AuthResponse?>(null)
    override val currentSession: StateFlow<AuthResponse?> = _currentSession.asStateFlow()

    private val supabaseUrl: String by lazy {
        try {
            // Read from BuildConfig, which is automatically configured via Secrets from the .env file
            val url = BuildConfig.SUPABASE_URL
            if (url.isNullOrBlank()) "https://your-project.supabase.co" else url
        } catch (e: Throwable) {
            "https://your-project.supabase.co"
        }
    }

    private val supabaseKey: String by lazy {
        try {
            val key = BuildConfig.SUPABASE_KEY
            if (key.isNullOrBlank()) "placeholder-anon-key-12345" else key
        } catch (e: Throwable) {
            "placeholder-anon-key-12345"
        }
    }

    private val api: SupabaseAuthApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val baseUrl = if (supabaseUrl.endsWith("/")) supabaseUrl else "$supabaseUrl/"

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseAuthApi::class.java)
    }

    init {
        prepopulateDefaultMappings()
        loadSavedSession()
    }

    override suspend fun signUp(
        fullName: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<AuthResponse> {
        return try {
            // STEP 3 - FIX YOUR ANDROID SIGNUP CODE
            val userMetadata = buildJsonObject {
                put("first_name", fullName.substringBefore(" "))
                put("last_name", fullName.substringAfter(" ", "User"))
                put("role", role.name.lowercase())
            }

            // Map standard keys to Map<String, String> for serializing inside SignUpRequest
            val stringMetadata = userMetadata.mapValues { it.value.toString() }

            if (supabaseUrl == "https://your-project.supabase.co" || supabaseKey == "placeholder-anon-key-12345") {
                // Config placeholder fallback for local sandbox demo
                val dummyUser = UserDto(
                    id = "usr_" + System.currentTimeMillis().toString(),
                    email = email,
                    userMetadata = UserMetadata(
                        firstName = fullName.substringBefore(" "),
                        lastName = fullName.substringAfter(" ", "User"),
                        role = role.name.lowercase()
                    )
                )
                val response = AuthResponse(
                    accessToken = "dummy_token_" + System.currentTimeMillis(),
                    tokenType = "bearer",
                    expiresIn = 3600,
                    user = dummyUser
                )
                saveSession(response)
                return Result.success(response)
            }

            val request = SignUpRequest(email, password, stringMetadata)
            val response = api.signUp(supabaseKey, request = request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                if (!authResponse.accessToken.isNullOrEmpty()) {
                    saveSession(authResponse)
                }
                Result.success(authResponse)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Signup failed"
                Result.failure(Exception("Supabase Error: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            if (supabaseUrl == "https://your-project.supabase.co" || supabaseKey == "placeholder-anon-key-12345") {
                // If demo mode, verify with fallback sandbox credentials
                val dummyUser = UserDto(
                    id = "usr_demo",
                    email = email,
                    userMetadata = UserMetadata(
                        firstName = "Demo",
                        lastName = "User",
                        role = "student"
                    )
                )
                val response = AuthResponse(
                    accessToken = "dummy_token_demo",
                    tokenType = "bearer",
                    expiresIn = 3600,
                    user = dummyUser
                )
                saveSession(response)
                return Result.success(response)
            }

            val request = LoginRequest(email, password)
            val response = api.login(supabaseKey, request = request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                saveSession(authResponse)
                Result.success(authResponse)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Login failed"
                Result.failure(Exception("Supabase Error: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        _currentSession.value = null
        sharedPreferences.edit().remove("saved_session").apply()
    }

    override fun loadSavedSession(): AuthResponse? {
        val savedJson = sharedPreferences.getString("saved_session", null)
        return try {
            if (savedJson != null) {
                val response = authResponseAdapter.fromJson(savedJson)
                _currentSession.value = response
                response
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun saveSession(response: AuthResponse) {
        _currentSession.value = response
        try {
            val json = authResponseAdapter.toJson(response)
            sharedPreferences.edit().putString("saved_session", json).apply()
        } catch (e: Exception) {
            // Save gracefully fallback
        }
    }

    private fun prepopulateDefaultMappings() {
        val editor = sharedPreferences.edit()
        val defaultMappings = mapOf(
            "S4A-023" to "wanchaaaron@gmail.com",
            "S4B-101" to "alex.rivera@oakridge.edu",
            "S4B-102" to "marcus.chen@oakridge.edu",
            "S3A-015" to "sarah.j@oakridge.edu",
            "S4B-104" to "taylor.vance@oakridge.edu",
            "S4B-105" to "emily.r@oakridge.edu"
        )
        defaultMappings.forEach { (studentId, email) ->
            val idKeyForEmail = "email_to_id_$email"
            val emailKeyForId = "id_to_email_${studentId.uppercase().trim()}"
            if (!sharedPreferences.contains(idKeyForEmail)) {
                editor.putString(idKeyForEmail, studentId)
            }
            if (!sharedPreferences.contains(emailKeyForId)) {
                editor.putString(emailKeyForId, email)
            }
        }
        editor.apply()
    }

    override fun lookupEmailForStudentId(studentId: String): String? {
        val cleanId = studentId.uppercase().trim()
        val storedEmail = sharedPreferences.getString("id_to_email_$cleanId", null)
        if (storedEmail != null) return storedEmail
        return "${cleanId.lowercase()}@student.nexus.edu"
    }

    override fun lookupStudentIdForEmail(email: String): String? {
        val storedId = sharedPreferences.getString("email_to_id_$email", null)
        if (storedId != null) return storedId
        if (email.contains("@")) {
            val prefix = email.substringBefore("@")
            if (prefix.matches(Regex("^[a-zA-Z0-9\\-]+$"))) {
                return prefix.uppercase()
            }
        }
        return null
    }

    override fun registerStudentIdMapping(studentId: String, email: String) {
        val cleanId = studentId.uppercase().trim()
        sharedPreferences.edit()
            .putString("id_to_email_$cleanId", email)
            .putString("email_to_id_$email", studentId)
            .apply()
    }
}
