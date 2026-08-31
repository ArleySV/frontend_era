package com.era.app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences by lazy {
        try {
            crearPrefs()
        } catch (e: Exception) {
            // Si falla (p.ej. por AEADBadTagException tras una actualización o reinstalación),
            // limpiamos los archivos corruptos y reintentamos una vez.
            context.deleteSharedPreferences(PREFS_FILE_NAME)
            crearPrefs()
        }
    }

    private fun crearPrefs(): SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_SESSION_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_SESSION_TOKEN, null)

    fun saveEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    fun getEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun clearToken() {
        prefs.edit().remove(KEY_SESSION_TOKEN).remove(KEY_USER_EMAIL).apply()
    }

    fun hasToken(): Boolean = getToken() != null

    companion object {
        private const val PREFS_FILE_NAME = "era_encrypted_prefs"
        private const val KEY_SESSION_TOKEN = "session_token"
        private const val KEY_USER_EMAIL = "user_email"
    }
}
