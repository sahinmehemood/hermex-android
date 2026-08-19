package com.hermex.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hermex.data.api.HermesApi
import com.hermex.data.model.DailyUsage
import com.hermex.data.model.InsightsResponse
import com.hermex.data.model.ModelUsage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val api: HermesApi
) : ViewModel() {

    private val _insights = MutableStateFlow<InsightsResponse?>(null)
    val insights: StateFlow<InsightsResponse?> = _insights.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadInsights()
    }

    fun loadInsights() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getInsights()
                _insights.value = response
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
