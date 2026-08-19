package com.hermex.ui.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hermex.data.api.HermesApi
import com.hermex.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WorkspaceUiState {
    object Loading : WorkspaceUiState()
    data class Browse(
        val entries: List<WorkspaceEntry>,
        val currentPath: String,
        val workspace: String?
    ) : WorkspaceUiState()
    data class ViewFile(val file: FileResponse) : WorkspaceUiState()
    data class Error(val message: String) : WorkspaceUiState()
}

@HiltViewModel
class WorkspaceViewModel @Inject constructor(
    private val api: HermesApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkspaceUiState>(WorkspaceUiState.Loading)
    val uiState: StateFlow<WorkspaceUiState> = _uiState.asStateFlow()

    private val _workspaces = MutableStateFlow<List<WorkspaceRoot>>(emptyList())
    val workspaces: StateFlow<List<WorkspaceRoot>> = _workspaces.asStateFlow()

    private val _selectedWorkspace = MutableStateFlow<String?>(null)
    val selectedWorkspace: StateFlow<String?> = _selectedWorkspace.asStateFlow()

    private val navigationStack = mutableListOf<String>("")

    init {
        loadWorkspaces()
    }

    fun loadWorkspaces() {
        viewModelScope.launch {
            try {
                val response = api.getWorkspaces()
                _workspaces.value = response.workspaces ?: emptyList()
                if (_workspaces.value.isNotEmpty() && _selectedWorkspace.value == null) {
                    _selectedWorkspace.value = _workspaces.value.first().path
                    listDirectory("")
                }
            } catch (e: Exception) {
                _uiState.value = WorkspaceUiState.Error(e.message ?: "Failed to load workspaces")
            }
        }
    }

    fun selectWorkspace(path: String) {
        _selectedWorkspace.value = path
        navigationStack.clear()
        navigationStack.add("")
        listDirectory("")
    }

    fun listDirectory(path: String) {
        viewModelScope.launch {
            _uiState.value = WorkspaceUiState.Loading
            try {
                val response = api.listWorkspace(path, _selectedWorkspace.value)
                _uiState.value = WorkspaceUiState.Browse(
                    entries = response.entries ?: emptyList(),
                    currentPath = path,
                    workspace = _selectedWorkspace.value
                )
            } catch (e: Exception) {
                _uiState.value = WorkspaceUiState.Error(e.message ?: "Failed to list directory")
            }
        }
    }

    fun navigateInto(entry: WorkspaceEntry) {
        entry.path?.let {
            navigationStack.add(it)
            listDirectory(it)
        }
    }

    fun navigateUp(): Boolean {
        if (navigationStack.size > 1) {
            navigationStack.removeAt(navigationStack.size - 1)
            listDirectory(navigationStack.last())
            return true
        }
        return false
    }

    fun openFile(path: String) {
        viewModelScope.launch {
            _uiState.value = WorkspaceUiState.Loading
            try {
                val response = api.getFile(path, _selectedWorkspace.value)
                _uiState.value = WorkspaceUiState.ViewFile(response)
            } catch (e: Exception) {
                _uiState.value = WorkspaceUiState.Error(e.message ?: "Failed to open file")
            }
        }
    }

    fun backToBrowse() {
        listDirectory(navigationStack.lastOrNull() ?: "")
    }
}
