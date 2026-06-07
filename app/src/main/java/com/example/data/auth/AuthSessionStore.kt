package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.squareup.moshi.JsonAdapter

interface AuthSessionStore {
    fun save(response: AuthResponse)
    fun load(): AuthResponse?
    fun clear()
}

class EncryptedAuthSessionStore(
    context: Context,
    private val authResponseAdapter: JsonAdapter<AuthResponse>
) : AuthSessionStore {

    private val preferences: SharedPreferences = EncryptedSharedPreferences.create(
        context.applicationContext,
        AUTH_PREFS_NAME,
        MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun save(response: AuthResponse) {
        val json = authResponseAdapter.toJson(response)
        preferences.edit()
            .putString(KEY_SESSION, json)
            .putLong(KEY_SAVED_AT_MILLIS, System.currentTimeMillis())
            .apply()
    }

    override fun load(): AuthResponse? {
        val json = preferences.getString(KEY_SESSION, null) ?: return null
        val savedAtMillis = preferences.getLong(KEY_SAVED_AT_MILLIS, 0L)
        return try {
            val response = authResponseAdapter.fromJson(json)
            if (response == null || response.isExpired(savedAtMillis)) {
                clear()
                null
            } else {
                response
            }
        } catch (e: Exception) {
            clear()
            null
        }
    }

    override fun clear() {
        preferences.edit()
            .remove(KEY_SESSION)
            .remove(KEY_SAVED_AT_MILLIS)
            .apply()
    }

    private fun AuthResponse.isExpired(savedAtMillis: Long): Boolean {
        if (accessToken.isNullOrBlank()) return true
        if (savedAtMillis <= 0L) return true
        val lifetimeMillis = (expiresIn ?: return true) * MILLIS_PER_SECOND
        return System.currentTimeMillis() - savedAtMillis >= lifetimeMillis
    }

    companion object {
        const val AUTH_PREFS_NAME = "nexus_secure_auth_prefs"
        private const val KEY_SESSION = "saved_session"
        private const val KEY_SAVED_AT_MILLIS = "saved_at_millis"
        private const val MILLIS_PER_SECOND = 1_000L
    }
}

class NoOpAuthSessionStore : AuthSessionStore {
    override fun save(response: AuthResponse) = Unit
    override fun load(): AuthResponse? = null
    override fun clear() = Unit
}
