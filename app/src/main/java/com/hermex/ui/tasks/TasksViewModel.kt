package com.hermex.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hermex.data.api.HermesApi
import com.hermex.data.model.CronJob
import com.hermex.data.model.CronJobEditorDraft
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TasksUiState {
    object Loading : TasksUiState()
    data class Success(val jobs: List<CronJob>) : TasksUiState()
    data class Error(val message: String) : TasksUiState()
}

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val api: HermesApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<TasksUiState>(TasksUiState.Loading)
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _editorDraft = MutableStateFlow(CronJobEditorDraft())
    val editorDraft: StateFlow<CronJobEditorDraft> = _editorDraft.asStateFlow()

    private var allJobs = listOf<CronJob>()

    init {
        loadJobs()
    }

    fun loadJobs() {
        viewModelScope.launch {
            _uiState.value = TasksUiState.Loading
            try {
                val response = api.getCronJobs()
                allJobs = response.jobs ?: emptyList()
                _uiState.value = TasksUiState.Success(allJobs)
            } catch (e: Exception) {
                _uiState.value = TasksUiState.Error(e.message ?: "Failed to load tasks")
            }
        }
    }

    fun runJob(jobId: String) {
        viewModelScope.launch {
            try {
                api.runCronJob(jobId)
                loadJobs()
            } catch (e: Exception) {
                // Show error
            }
        }
    }

    fun pauseJob(jobId: String) {
        viewModelScope.launch {
            try {
                api.pauseCronJob(jobId)
                loadJobs()
            } catch (e: Exception) {}
        }
    }

    fun resumeJob(jobId: String) {
        viewModelScope.launch {
            try {
                api.resumeCronJob(jobId)
                loadJobs()
            } catch (e: Exception) {}
        }
    }

    fun deleteJob(jobId: String) {
        viewModelScope.launch {
            try {
                api.deleteCronJob(jobId)
                loadJobs()
            } catch (e: Exception) {}
        }
    }

    fun openEditor(job: CronJob? = null) {
        _editorDraft.value = job?.let { CronJobEditorDraft(it) } ?: CronJobEditorDraft()
        _isEditing.value = true
    }

    fun closeEditor() {
        _isEditing.value = false
    }

    fun updateDraft(update: (CronJobEditorDraft) -> CronJobEditorDraft) {
        _editorDraft.update(update)
    }

    fun saveDraft() {
        val draft = _editorDraft.value
        val validation = draft.validationMessage
        if (validation != null) return

        viewModelScope.launch {
            try {
                val body = mapOf(
                    "name" to draft.trimmedName,
                    "prompt" to draft.trimmedPrompt,
                    "schedule" to draft.trimmedSchedule,
                    "deliver" to draft.trimmedDeliver,
                    "skills" to draft.skills.takeIf { it.isNotEmpty() },
                    "model" to draft.trimmedModel,
                    "provider" to draft.trimmedProvider,
                    "profile" to draft.trimmedProfile,
                    "toast_notifications" to draft.toastNotifications
                )
                api.createCronJob(body)
                _isEditing.value = false
                loadJobs()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
