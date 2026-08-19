package com.hermex.ui.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hermex.data.gateway.ConnectionSettings
import com.hermex.data.gateway.ConnectionStore
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
    private val store: ConnectionStore
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
            runCatching { store.save(url, token, label) }
                .onSuccess { refresh() }
                .onFailure { _state.value = _state.value.copy(saving = false, error = it.message ?: "Could not save connection") }
        }
    }

    fun clear() {
        viewModelScope.launch {
            runCatching { store.clear() }
            refresh()
        }
    }
}
