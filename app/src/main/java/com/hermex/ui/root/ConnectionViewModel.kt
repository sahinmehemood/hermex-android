package com.hermex.ui.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hermex.data.gateway.ConnectionSettings
import com.hermex.data.gateway.ConnectionStore
import com.hermex.data.gateway.HermesGatewayTransport
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class ConnectionUiState(
    val loading: Boolean = true,
    val settings: ConnectionSettings? = null,
    val saving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val store: ConnectionStore,
    private val transport: HermesGatewayTransport
) : ViewModel() {
    private val _state = MutableStateFlow(ConnectionUiState())
    val state: StateFlow<ConnectionUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { store.settings() }
                .onSuccess { _state.value = ConnectionUiState(loading = false, settings = it) }
                .onFailure { _state.value = ConnectionUiState(loading = false, error = it.message ?: "Could not load connection") }
        }
    }

    fun save(url: String, token: String?, label: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true, error = null)
            runCatching {
                require(url.startsWith("ws://") || url.startsWith("wss://")) { "Use a ws:// or wss:// Hermes gateway URL" }
                store.save(url, token, label)
                transport.connect(withToken(url, token))
            }.onSuccess {
                _state.value = ConnectionUiState(
                    loading = false,
                    settings = ConnectionSettings(url, label.ifBlank { "Hermes" }, token)
                )
            }.onFailure {
                runCatching { store.clear() }
                _state.value = _state.value.copy(saving = false, error = it.message ?: "Could not connect to Hermes")
            }
        }
    }

    fun clear() {
        viewModelScope.launch {
            transport.disconnect()
            runCatching { store.clear() }
            refresh()
        }
    }

    private fun withToken(url: String, token: String?): String {
        if (token.isNullOrBlank()) return url
        return runCatching {
            val uri = java.net.URI(url)
            val query = uri.rawQuery.orEmpty()
            if (query.contains("token=") || query.contains("ticket=")) return@runCatching url
            val encoded = java.net.URLEncoder.encode(token, Charsets.UTF_8.name())
            val separator = if (query.isEmpty()) "" else "&"
            java.net.URI(uri.scheme, uri.userInfo, uri.host, uri.port, uri.path, "$query${separator}token=$encoded", uri.fragment).toString()
        }.getOrDefault(url)
    }
}
