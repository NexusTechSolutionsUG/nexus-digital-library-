package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

interface AuthSessionStore {
    fun saveSession(session: AuthResponse)
    fun loadSession(): AuthResponse?
    fun clearSession()
}

class EncryptedAuthSessionStore(private val context: Context) : AuthSessionStore {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val authResponseAdapter = moshi.adapter(AuthResponse::class.java)

    private val sharedPreferences: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "nexus_secure_auth_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("EncryptedAuthSession", "EncryptedSharedPreferences initialization failed, falling back to standard sandbox storage", e)
            // Use fallback name that can be excluded in backup
            context.getSharedPreferences("nexus_auth_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    override fun saveSession(session: AuthResponse) {
        try {
            val json = authResponseAdapter.toJson(session)
            sharedPreferences.edit()
                .putString("saved_secure_session", json)
                .putLong("saved_session_time_ms", System.currentTimeMillis())
                .apply()
        } catch (e: Exception) {
            Log.e("EncryptedAuthSession", "Failed to save session", e)
        }
    }

    override fun loadSession(): AuthResponse? {
        val savedJson = sharedPreferences.getString("saved_secure_session", null) ?: return null
        val savedTimeMs = sharedPreferences.getLong("saved_session_time_ms", 0L)
        
        try {
            val session = authResponseAdapter.fromJson(savedJson) ?: return null
            
            // Validate session and token expiry
            val expiresInSeconds = session.expiresIn ?: 3600L
            val expiryTimeMs = savedTimeMs + (expiresInSeconds * 1000L)
            
            if (System.currentTimeMillis() > expiryTimeMs) {
                Log.w("EncryptedAuthSession", "Persisted session has expired. Clearing storage.")
                clearSession()
                return null
            }
            return session
        } catch (e: Exception) {
            Log.e("EncryptedAuthSession", "Failed to parse session or verify expiry", e)
            clearSession()
            return null
        }
    }

    override fun clearSession() {
        sharedPreferences.edit()
            .remove("saved_secure_session")
            .remove("saved_session_time_ms")
            .apply()
    }
}
