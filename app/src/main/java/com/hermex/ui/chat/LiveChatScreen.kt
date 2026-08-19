package com.hermex.ui.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LiveChatScreen(
    viewModel: LiveChatViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycleCompat()
    val run by viewModel.run.collectAsStateWithLifecycleCompat()

    if (ui.loadingSession) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (ui.error != null || ui.sessionId == null) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(ui.error ?: "Unable to create a Hermes session", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    ChatWorkspace(
        state = run,
        messages = ui.messages,
        model = ui.model,
        profile = ui.profile,
        onSend = viewModel::send,
        onStop = viewModel::stop,
        onApprove = { id, approved -> viewModel.approve(id, approved) },
        onAttach = { /* Attachment provider arrives in next slice. */ }
    )
}

@Composable
private fun <T> kotlinx.coroutines.flow.StateFlow<T>.collectAsStateWithLifecycleCompat(): androidx.compose.runtime.State<T> =
    androidx.compose.runtime.collectAsState(this)
