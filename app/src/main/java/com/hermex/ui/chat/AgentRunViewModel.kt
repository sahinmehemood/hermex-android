package com.hermex.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hermex.domain.chat.AgentGateway
import com.hermex.domain.chat.AgentRunEvent
import com.hermex.domain.chat.AgentRunState
import com.hermex.domain.chat.AttachmentRef
import com.hermex.domain.chat.reduce
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@HiltViewModel
class AgentRunViewModel @Inject constructor(
    private val gateway: AgentGateway
) : ViewModel() {
    private val _state = MutableStateFlow<AgentRunState>(AgentRunState.Idle)
    val state: StateFlow<AgentRunState> = _state.asStateFlow()

    private var activeJob: Job? = null

    fun send(
        sessionId: String,
        prompt: String,
        attachments: List<AttachmentRef> = emptyList(),
        model: String? = null,
        profile: String? = null
    ) {
        activeJob?.cancel()
        _state.value = AgentRunState.Submitting(System.currentTimeMillis())
        activeJob = viewModelScope.launch {
            try {
                gateway.submitPrompt(sessionId, prompt, attachments, model, profile).collect(::applyEvent)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (t: Throwable) {
                _state.value = AgentRunState.Failed(null, t.message ?: "Unable to start Hermes run", recoverable = true)
            }
        }
    }

    fun stop(sessionId: String) {
        viewModelScope.launch {
            runCatching { gateway.stopRun(sessionId) }
            activeJob?.cancel()
            _state.value = _state.value.reduce(AgentRunEvent.Cancelled)
        }
    }

    fun steer(sessionId: String, instruction: String) {
        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            runCatching { gateway.steerRun(sessionId, instruction).collect(::applyEvent) }
                .onFailure { error ->
                    _state.value = AgentRunState.Failed(null, error.message ?: "Unable to steer run", true)
                }
        }
    }

    fun approve(sessionId: String, requestId: String, approved: Boolean) {
        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            runCatching { gateway.respondToApproval(sessionId, requestId, approved).collect(::applyEvent) }
                .onFailure { error ->
                    _state.value = AgentRunState.Failed(null, error.message ?: "Approval response failed", true)
                }
        }
    }

    fun clarify(sessionId: String, requestId: String, answer: String) {
        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            runCatching { gateway.respondToClarification(sessionId, requestId, answer).collect(::applyEvent) }
                .onFailure { error ->
                    _state.value = AgentRunState.Failed(null, error.message ?: "Clarification failed", true)
                }
        }
    }

    fun reset() {
        activeJob?.cancel()
        _state.value = AgentRunState.Idle
    }

    private fun applyEvent(event: AgentRunEvent) {
        _state.value = _state.value.reduce(event)
    }

    override fun onCleared() {
        activeJob?.cancel()
        super.onCleared()
    }
}
