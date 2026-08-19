package com.hermex.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hermex.data.api.HermesApi
import com.hermex.data.model.ModelInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModelsViewModel @Inject constructor(
    private val api: HermesApi
) : ViewModel() {

    private val _models = MutableStateFlow<List<ModelInfo>>(emptyList())
    val models: StateFlow<List<ModelInfo>> = _models.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredModels = combine(_models, _searchQuery) { models, query ->
        if (query.isBlank()) models
        else models.filter {
            it.name?.contains(query, ignoreCase = true) == true ||
            it.provider?.contains(query, ignoreCase = true) == true
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        loadModels()
    }

    fun loadModels() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getModels()
                _models.value = response.models ?: emptyList()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
    }
}
