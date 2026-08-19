package com.hermex.ui.tasks

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
import com.hermex.data.model.CronJob
import com.hermex.data.model.CronJobStatus

@Composable
fun TasksScreen(
    viewModel: TasksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is TasksUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is TasksUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text((state as TasksUiState.Error).message)
                    Button(onClick = viewModel::loadJobs) { Text("Retry") }
                }
            }
            is TasksUiState.Success -> {
                val jobs = (state as TasksUiState.Success).jobs
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(jobs) { job ->
                        CronJobCard(
                            job = job,
                            onRun = { viewModel.runJob(it) },
                            onPause = { viewModel.pauseJob(it) },
                            onResume = { viewModel.resumeJob(it) },
                            onDelete = { viewModel.deleteJob(it) },
                            onEdit = { viewModel.openEditor(job) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.openEditor() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Task")
        }

        if (isEditing) {
            CronJobEditor(
                draft = viewModel.editorDraft.collectAsState().value,
                onDraftChange = viewModel::updateDraft,
                onSave = viewModel::saveDraft,
                onDismiss = viewModel::closeEditor
            )
        }
    }
}

@Composable
fun CronJobCard(
    job: CronJob,
    onRun: (String) -> Unit,
    onPause: (String) -> Unit,
    onResume: (String) -> Unit,
    onDelete: (String) -> Unit,
    onEdit: () -> Unit
) {
    val status = job.status
    val statusColor = when (status) {
        CronJobStatus.active -> MaterialTheme.colorScheme.primary
        CronJobStatus.paused -> MaterialTheme.colorScheme.tertiary
        CronJobStatus.off -> MaterialTheme.colorScheme.outline
        CronJobStatus.error -> MaterialTheme.colorScheme.error
        CronJobStatus.needsAttention -> MaterialTheme.colorScheme.errorContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = job.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                AssistChip(
                    onClick = {},
                    label = { Text(status.label) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = statusColor.copy(alpha = 0.12f),
                        labelColor = statusColor
                    )
                )
            }

            job.prompt?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = job.scheduleText ?: "No schedule",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                job.model?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (job.enabled == true) {
                    IconButton(onClick = { job.jobId?.let(onPause) }) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause")
                    }
                } else {
                    IconButton(onClick = { job.jobId?.let(onResume) }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                    }
                }
                IconButton(onClick = { job.jobId?.let(onRun) }) {
                    Icon(Icons.Default.PlayCircleOutline, contentDescription = "Run now")
                }
                IconButton(onClick = { job.jobId?.let(onDelete) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CronJobEditor(
    draft: com.hermex.data.model.CronJobEditorDraft,
    onDraftChange: ((com.hermex.data.model.CronJobEditorDraft) -> com.hermex.data.model.CronJobEditorDraft) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("New Scheduled Task", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = draft.name,
                onValueChange = { onDraftChange { it.copy(name = it.name) } },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = draft.prompt,
                onValueChange = { onDraftChange { it.copy(prompt = it.prompt) } },
                label = { Text("Prompt") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = draft.schedule,
                onValueChange = { onDraftChange { it.copy(schedule = it.schedule) } },
                label = { Text("Schedule (cron expression)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = draft.model,
                onValueChange = { onDraftChange { it.copy(model = it.model) } },
                label = { Text("Model") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onSave) { Text("Save") }
            }
        }
    }
}
