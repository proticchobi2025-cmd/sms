package com.example.smssheettracker.data.remote

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureCredentialStore(context: Context) {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "secure_credentials",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveServiceAccountJson(json: String) {
        prefs.edit().putString(KEY_SERVICE_JSON, json).apply()
    }

    fun getServiceAccountJson(): String? = prefs.getString(KEY_SERVICE_JSON, null)

    companion object {
        private const val KEY_SERVICE_JSON = "service_account_json"
    }
}
