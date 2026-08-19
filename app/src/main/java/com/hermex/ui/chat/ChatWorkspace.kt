package com.hermex.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hermex.domain.chat.AgentRunState
import com.hermex.domain.chat.ToolRun
import com.hermex.domain.chat.ToolStatus
import com.hermex.ui.components.OptimusApprovalCard
import com.hermex.ui.components.OptimusComposerHint
import com.hermex.ui.components.OptimusGlassCard
import com.hermex.ui.components.OptimusRunControls
import com.hermex.ui.components.OptimusStatusPill
import com.hermex.ui.components.OptimusToolCard
import com.hermex.ui.theme.OptimusBackground
import com.hermex.ui.theme.OptimusPrimary
import com.hermex.ui.theme.OptimusSurface
import com.hermex.ui.theme.OptimusTextPrimary
import com.hermex.ui.theme.OptimusTextSecondary

@Composable
fun ChatWorkspace(
    state: AgentRunState,
    messages: List<ConversationLine>,
    model: String,
    profile: String,
    onSend: (String) -> Unit,
    onStop: () -> Unit,
    onApprove: (String, Boolean) -> Unit,
    onAttach: () -> Unit,
    modifier: Modifier = Modifier
) {
    var composer by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val running = state is AgentRunState.Running || state is AgentRunState.Submitting || state is AgentRunState.AwaitingApproval || state is AgentRunState.AwaitingClarification

    Column(modifier.fillMaxSize().background(OptimusBackground)) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OptimusComposerHint(model = model, profile = profile)
                Spacer(Modifier.weight(1f))
                OptimusRunControls(running = running, onStop = onStop, onPause = {}, onResume = {})
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { line ->
                ConversationBubble(line)
            }

            when (state) {
                is AgentRunState.Running -> {
                    item {
                        if (state.thinkingText.isNotBlank()) {
                            OptimusGlassCard(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(14.dp)) {
                                    OptimusStatusPill("Reasoning", OptimusStatusPillTone.Info)
                                    Spacer(Modifier.height(8.dp))
                                    Text(state.thinkingText, color = OptimusTextSecondary, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    items(state.toolHistory, key = { it.id }) { tool ->
                        OptimusToolCard(
                            title = tool.name,
                            subtitle = tool.inputSummary,
                            outputPreview = tool.outputPreview,
                            running = tool.status == ToolStatus.Running,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (state.assistantText.isNotBlank()) {
                        item { ConversationBubble(ConversationLine("stream", Role.Assistant, state.assistantText)) }
                    }
                }
                is AgentRunState.AwaitingApproval -> item {
                    OptimusApprovalCard(
                        title = state.approval.title,
                        detail = state.approval.detail,
                        onApprove = { onApprove(state.approval.id, true) },
                        onReject = { onApprove(state.approval.id, false) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is AgentRunState.AwaitingClarification -> item {
                    OptimusGlassCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            OptimusStatusPill("Needs input", OptimusStatusPillTone.Warning)
                            Spacer(Modifier.height(8.dp))
                            Text(state.question.question, color = OptimusTextPrimary, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                is AgentRunState.Failed -> item {
                    OptimusStatusPill("Run failed", OptimusStatusPillTone.Error, Modifier.fillMaxWidth())
                }
                else -> Unit
            }
        }

        Composer(
            value = composer,
            onValueChange = { composer = it },
            running = running,
            onSend = {
                val value = composer.trim()
                if (value.isNotEmpty()) {
                    onSend(value)
                    composer = ""
                }
            },
            onStop = onStop,
            onAttach = onAttach
        )
    }
}

typealias OptimusStatusPillTone = com.hermex.ui.components.OptimusTone

@Composable
private fun ConversationBubble(line: ConversationLine) {
    val assistant = line.role == Role.Assistant
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (assistant) Arrangement.Start else Arrangement.End) {
        SurfaceBubble(assistant = assistant) {
            Text(line.text, color = OptimusTextPrimary, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun SurfaceBubble(assistant: Boolean, content: @Composable () -> Unit) {
    OptimusGlassCard(
        modifier = Modifier.fillMaxWidth(if (assistant) 0.92f else 0.86f),
        onClick = null
    ) {
        Box(Modifier.padding(14.dp)) { content() }
    }
}

@Composable
private fun Composer(
    value: String,
    onValueChange: (String) -> Unit,
    running: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onAttach: () -> Unit
) {
    Column(Modifier.fillMaxWidth().imePadding().navigationBarsPadding().background(OptimusSurface).padding(12.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            IconButton(onClick = onAttach) { Icon(Icons.Outlined.AttachFile, contentDescription = "Attach") }
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message Hermes…") },
                maxLines = 6,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
            )
            Spacer(Modifier.padding(2.dp))
            IconButton(onClick = if (running) onStop else onSend) {
                Icon(if (running) Icons.Outlined.Stop else Icons.Outlined.Send, contentDescription = if (running) "Stop" else "Send", tint = if (running) Color(0xFFFF7B86) else OptimusPrimary)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = {}) { Icon(Icons.Outlined.Add, contentDescription = null); Text("Context") }
            Text("Hermes can act on your connected workspace", style = MaterialTheme.typography.labelSmall, color = OptimusTextSecondary)
        }
    }
}

data class ConversationLine(
    val id: String,
    val role: Role,
    val text: String
)

enum class Role { User, Assistant }
