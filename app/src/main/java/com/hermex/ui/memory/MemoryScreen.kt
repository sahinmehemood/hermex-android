package com.hermex.ui.memory

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hermex.data.model.MemorySection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    viewModel: MemoryViewModel = hiltViewModel()
) {
    val memory by viewModel.memory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val activeSection by viewModel.activeSection.collectAsState()
    val editText by viewModel.editText.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = MemorySection.entries.indexOf(activeSection)
        ) {
            MemorySection.entries.forEach { section ->
                Tab(
                    selected = section == activeSection,
                    onClick = { viewModel.selectSection(section) },
                    text = {
                        Text(
                            section.name.replaceFirstChar { it.uppercase() }
                        )
                    }
                )
            }
        }

        if (isLoading && memory == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isEditing) {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = viewModel::updateEditText,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    val content = when (activeSection) {
                        MemorySection.memory -> memory?.memory
                        MemorySection.user -> memory?.user
                        MemorySection.soul -> memory?.soul
                    }
                    Text(
                        text = content ?: "No content",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (isEditing) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FloatingActionButton(
                            onClick = viewModel::cancelEditing,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                        FloatingActionButton(onClick = viewModel::save) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                    }
                } else {
                    FloatingActionButton(
                        onClick = viewModel::startEditing,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            }
        }
    }
}
