package com.hermex.ui.skills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hermex.data.api.HermesApi
import com.hermex.data.model.SkillSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SkillsViewModel @Inject constructor(
    private val api: HermesApi
) : ViewModel() {

    private val _skills = MutableStateFlow<List<SkillSummary>>(emptyList())
    val skills: StateFlow<List<SkillSummary>> = _skills.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var allSkills = listOf<SkillSummary>()

    val filteredSkills = combine(_skills, _searchQuery) { skills, query ->
        if (query.isBlank()) skills
        else skills.filter {
            it.name?.contains(query, ignoreCase = true) == true ||
            it.category?.contains(query, ignoreCase = true) == true ||
            it.description?.contains(query, ignoreCase = true) == true
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        loadSkills()
    }

    fun loadSkills() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getSkills()
                allSkills = response.skills ?: emptyList()
                _skills.value = allSkills
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleSkill(name: String, enabled: Boolean) {
        viewModelScope.launch {
            try {
                api.toggleSkill(com.hermex.data.model.ToggleSkillRequest(name, enabled))
                loadSkills()
            } catch (e: Exception) {}
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
    }
}
