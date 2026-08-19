package com.hermex.ui.workspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hermex.data.model.WorkspaceEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    viewModel: WorkspaceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val workspaces by viewModel.workspaces.collectAsState()
    val selectedWorkspace by viewModel.selectedWorkspace.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Workspace selector
        if (workspaces.size > 1) {
            ScrollableTabRow(
                selectedTabIndex = workspaces.indexOfFirst { it.path == selectedWorkspace }.coerceAtLeast(0)
            ) {
                workspaces.forEach { ws ->
                    Tab(
                        selected = ws.path == selectedWorkspace,
                        onClick = { ws.path?.let { viewModel.selectWorkspace(it) } },
                        text = { Text(ws.name ?: ws.path ?: "Unknown") }
                    )
                }
            }
        }

        when (val state = uiState) {
            is WorkspaceUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is WorkspaceUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text((state as WorkspaceUiState.Error).message)
                    Button(onClick = viewModel::loadWorkspaces) { Text("Retry") }
                }
            }
            is WorkspaceUiState.Browse -> {
                val browse = state as WorkspaceUiState.Browse
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(browse.entries) { entry ->
                        FileEntryRow(
                            entry = entry,
                            onClick = {
                                if (entry.isBrowsableDirectory) {
                                    viewModel.navigateInto(entry)
                                } else {
                                    entry.path?.let { viewModel.openFile(it) }
                                }
                            }
                        )
                    }
                }
            }
            is WorkspaceUiState.ViewFile -> {
                val file = (state as WorkspaceUiState.ViewFile).file
                FileViewer(
                    file = file,
                    onBack = viewModel::backToBrowse
                )
            }
        }
    }
}

@Composable
fun FileEntryRow(
    entry: WorkspaceEntry,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = entry.name ?: "Unknown",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                entry.size?.let {
                    Text(
                        formatFileSize(it),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        leadingContent = {
            Icon(
                if (entry.isBrowsableDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                contentDescription = null,
                tint = if (entry.isBrowsableDirectory)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileViewer(
    file: com.hermex.data.model.FileResponse,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(file.name ?: "File") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            file.language?.let {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Text(
                text = file.content ?: "",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}
