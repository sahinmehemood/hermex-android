package com.hermex.data.gateway

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hermex.security.SecureStore
import kotlinx.coroutines.flow.first
import androidx.datastore.preferences.core.edit

private val Context.optimusDataStore by preferencesDataStore(name = "optimus_connection")

class ConnectionStore(
    private val context: Context,
    private val secureStore: SecureStore
) {
    private val wsUrlKey = stringPreferencesKey("hermes_ws_url")
    private val labelKey = stringPreferencesKey("server_label")
    private val tokenKey = "hermes_ws_token"

    suspend fun save(url: String, token: String?, label: String = "Hermes") {
        context.optimusDataStore.edit { prefs ->
            prefs[wsUrlKey] = url.trim()
            prefs[labelKey] = label.trim().ifEmpty { "Hermes" }
        }
        if (token.isNullOrBlank()) secureStore.remove(tokenKey) else secureStore.put(tokenKey, token)
    }

    suspend fun clear() {
        context.optimusDataStore.edit { it.clear() }
        secureStore.remove(tokenKey)
    }

    suspend fun settings(): ConnectionSettings {
        val prefs = context.optimusDataStore.data.first()
        return ConnectionSettings(
            wsUrl = prefs[wsUrlKey].orEmpty(),
            label = prefs[labelKey].orEmpty().ifBlank { "Hermes" },
            token = secureStore.get(tokenKey)
        )
    }
}

data class ConnectionSettings(
    val wsUrl: String,
    val label: String,
    val token: String?
)
