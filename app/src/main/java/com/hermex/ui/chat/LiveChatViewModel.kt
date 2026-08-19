package com.hermex.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hermex.data.gateway.HermesSessionGateway
import com.hermex.domain.chat.AgentGateway
import com.hermex.domain.chat.AgentRunEvent
import com.hermex.domain.chat.AgentRunState
import com.hermex.domain.chat.AttachmentRef
import com.hermex.domain.chat.reduce
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class LiveChatUiState(
    val sessionId: String? = null,
    val model: String = "Hermes",
    val profile: String = "default",
    val messages: List<ConversationLine> = emptyList(),
    val loadingSession: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class LiveChatViewModel @Inject constructor(
    private val agentGateway: AgentGateway,
    private val sessions: HermesSessionGateway
) : ViewModel() {
    private val _uiState = MutableStateFlow(LiveChatUiState())
    val uiState: StateFlow<LiveChatUiState> = _uiState.asStateFlow()

    private val _run = MutableStateFlow<AgentRunState>(AgentRunState.Idle)
    val run: StateFlow<AgentRunState> = _run.asStateFlow()
    private var job: Job? = null

    init {
        createSession()
    }

    fun createSession() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loadingSession = true, error = null)
            runCatching { sessions.createSession() }
                .onSuccess { created ->
                    _uiState.value = LiveChatUiState(
                        sessionId = created.id,
                        model = created.model ?: "Hermes",
                        profile = created.profile ?: "default",
                        loadingSession = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(loadingSession = false, error = error.message ?: "Could not create Hermes session")
                }
        }
    }

    fun send(text: String, attachments: List<AttachmentRef> = emptyList()) {
        val sessionId = _uiState.value.sessionId ?: return
        val current = text.trim()
        if (current.isBlank()) return
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + ConversationLine("u-${System.nanoTime()}", Role.User, current)
        )
        _run.value = AgentRunState.Submitting(System.currentTimeMillis())
        job?.cancel()
        job = viewModelScope.launch {
            runCatching {
                agentGateway.submitPrompt(sessionId, current, attachments).collect(::applyEvent)
            }.onFailure { error ->
                _run.value = AgentRunState.Failed(sessionId, error.message ?: "Hermes run failed", true)
            }
        }
    }

    fun stop() {
        val id = _uiState.value.sessionId ?: return
        viewModelScope.launch {
            runCatching { agentGateway.stopRun(id) }
            job?.cancel()
            _run.value = _run.value.reduce(AgentRunEvent.Cancelled)
        }
    }

    fun approve(requestId: String, approved: Boolean) {
        val id = _uiState.value.sessionId ?: return
        job?.cancel()
        job = viewModelScope.launch {
            runCatching { agentGateway.respondToApproval(id, requestId, approved).collect(::applyEvent) }
                .onFailure { _run.value = AgentRunState.Failed(id, it.message ?: "Approval response failed", true) }
        }
    }

    fun clarify(requestId: String, answer: String) {
        val id = _uiState.value.sessionId ?: return
        job?.cancel()
        job = viewModelScope.launch {
            runCatching { agentGateway.respondToClarification(id, requestId, answer).collect(::applyEvent) }
                .onFailure { _run.value = AgentRunState.Failed(id, it.message ?: "Clarification failed", true) }
        }
    }

    private fun applyEvent(event: AgentRunEvent) {
        _run.value = _run.value.reduce(event)
        if (event is AgentRunEvent.Completed) {
            val state = _run.value as? AgentRunState.Completed ?: return
            if (state.assistantText.isNotBlank()) {
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + ConversationLine("a-${System.nanoTime()}", Role.Assistant, state.assistantText)
                )
            }
        }
    }
}
