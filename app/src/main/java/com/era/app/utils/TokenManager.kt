package com.era.app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("deprecation")
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        PREFS_FILE_NAME,
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_SESSION_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_SESSION_TOKEN, null)

    fun clearToken() {
        prefs.edit().remove(KEY_SESSION_TOKEN).apply()
    }

    fun hasToken(): Boolean = getToken() != null

    companion object {
        private const val PREFS_FILE_NAME = "era_encrypted_prefs"
        private const val KEY_SESSION_TOKEN = "session_token"
    }
}
