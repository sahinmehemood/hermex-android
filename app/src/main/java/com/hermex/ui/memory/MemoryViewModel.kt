package com.hermex.ui.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hermex.data.api.HermesApi
import com.hermex.data.model.MemoryResponse
import com.hermex.data.model.MemorySection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val api: HermesApi
) : ViewModel() {

    private val _memory = MutableStateFlow<MemoryResponse?>(null)
    val memory: StateFlow<MemoryResponse?> = _memory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _activeSection = MutableStateFlow(MemorySection.memory)
    val activeSection: StateFlow<MemorySection> = _activeSection.asStateFlow()

    private val _editText = MutableStateFlow("")
    val editText: StateFlow<String> = _editText.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    init {
        loadMemory()
    }

    fun loadMemory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getMemory()
                _memory.value = response
                updateEditText()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectSection(section: MemorySection) {
        _activeSection.value = section
        updateEditText()
    }

    private fun updateEditText() {
        val mem = _memory.value ?: return
        _editText.value = when (_activeSection.value) {
            MemorySection.memory -> mem.memory ?: ""
            MemorySection.user -> mem.user ?: ""
            MemorySection.soul -> mem.soul ?: ""
        }
    }

    fun updateEditText(text: String) {
        _editText.value = text
    }

    fun startEditing() {
        _isEditing.value = true
        updateEditText()
    }

    fun cancelEditing() {
        _isEditing.value = false
        updateEditText()
    }

    fun save() {
        viewModelScope.launch {
            try {
                val section = _activeSection.value.name
                api.writeMemory(mapOf("section" to section, "content" to _editText.value))
                _isEditing.value = false
                loadMemory()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
