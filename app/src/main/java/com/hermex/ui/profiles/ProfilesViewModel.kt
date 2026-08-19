package com.hermex.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hermex.data.api.HermesApi
import com.hermex.data.model.ProfileInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfilesViewModel @Inject constructor(
    private val api: HermesApi
) : ViewModel() {

    private val _profiles = MutableStateFlow<List<ProfileInfo>>(emptyList())
    val profiles: StateFlow<List<ProfileInfo>> = _profiles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadProfiles()
    }

    fun loadProfiles() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getProfiles()
                _profiles.value = response.profiles ?: emptyList()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun switchProfile(name: String) {
        viewModelScope.launch {
            try {
                api.switchProfile(mapOf("profile" to name))
                loadProfiles()
            } catch (e: Exception) {}
        }
    }
}
