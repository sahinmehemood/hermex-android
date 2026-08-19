package com.hermex.domain.chat

/**
 * Transport-independent state model for one Hermes agent run.
 * UI renders this state; transport adapters only emit events.
 */
sealed interface AgentRunState {
    data object Idle : AgentRunState
    data class Submitting(val startedAtEpochMs: Long) : AgentRunState
    data class Running(
        val runId: String,
        val assistantText: String = "",
        val thinkingText: String = "",
        val activeTool: ToolRun? = null,
        val toolHistory: List<ToolRun> = emptyList()
    ) : AgentRunState
    data class AwaitingApproval(
        val runId: String,
        val approval: ApprovalRequest,
        val assistantText: String,
        val toolHistory: List<ToolRun> = emptyList()
    ) : AgentRunState
    data class AwaitingClarification(
        val runId: String,
        val question: ClarificationRequest,
        val assistantText: String,
        val toolHistory: List<ToolRun> = emptyList()
    ) : AgentRunState
    data class Completed(
        val runId: String,
        val assistantText: String,
        val toolHistory: List<ToolRun> = emptyList()
    ) : AgentRunState
    data class Failed(
        val runId: String?,
        val message: String,
        val recoverable: Boolean,
        val partialText: String = ""
    ) : AgentRunState
    data class Cancelled(
        val runId: String?,
        val partialText: String = ""
    ) : AgentRunState
}

data class ToolRun(
    val id: String,
    val name: String,
    val inputSummary: String = "",
    val outputPreview: String = "",
    val status: ToolStatus = ToolStatus.Running,
    val startedAtEpochMs: Long? = null,
    val completedAtEpochMs: Long? = null
)

enum class ToolStatus { Running, AwaitingApproval, Completed, Failed }

data class ApprovalRequest(
    val id: String,
    val title: String,
    val detail: String,
    val danger: Boolean = false
)

data class ClarificationRequest(
    val id: String,
    val question: String,
    val options: List<String> = emptyList()
)

sealed interface AgentRunEvent {
    data class Started(val runId: String) : AgentRunEvent
    data class TextDelta(val text: String) : AgentRunEvent
    data class ThinkingDelta(val text: String) : AgentRunEvent
    data class ToolStarted(val tool: ToolRun) : AgentRunEvent
    data class ToolOutput(val toolId: String, val output: String) : AgentRunEvent
    data class ToolCompleted(val toolId: String, val output: String = "") : AgentRunEvent
    data class ToolFailed(val toolId: String, val output: String = "") : AgentRunEvent
    data class ApprovalRequired(val request: ApprovalRequest) : AgentRunEvent
    data class ClarificationRequired(val request: ClarificationRequest) : AgentRunEvent
    data object Completed : AgentRunEvent
    data object Cancelled : AgentRunEvent
    data class Failed(val message: String, val recoverable: Boolean = true) : AgentRunEvent
}

fun AgentRunState.reduce(event: AgentRunEvent): AgentRunState {
    return when (event) {
        is AgentRunEvent.Started -> AgentRunState.Running(event.runId)
        is AgentRunEvent.TextDelta -> when (this) {
            is AgentRunState.Running -> copy(assistantText = assistantText + event.text)
            is AgentRunState.AwaitingApproval -> copy(assistantText = assistantText + event.text)
            is AgentRunState.AwaitingClarification -> copy(assistantText = assistantText + event.text)
            else -> this
        }
        is AgentRunEvent.ThinkingDelta -> when (this) {
            is AgentRunState.Running -> copy(thinkingText = thinkingText + event.text)
            else -> this
        }
        is AgentRunEvent.ToolStarted -> when (this) {
            is AgentRunState.Running -> copy(
                activeTool = event.tool.copy(status = ToolStatus.Running),
                toolHistory = toolHistory + event.tool.copy(status = ToolStatus.Running)
            )
            else -> this
        }
        is AgentRunEvent.ToolOutput -> when (this) {
            is AgentRunState.Running -> {
                val updatedActive = activeTool?.takeIf { it.id == event.toolId }?.copy(outputPreview = event.output)
                copy(
                    activeTool = updatedActive ?: activeTool,
                    toolHistory = toolHistory.map { if (it.id == event.toolId) it.copy(outputPreview = event.output) else it }
                )
            }
            else -> this
        }
        is AgentRunEvent.ToolCompleted -> when (this) {
            is AgentRunState.Running -> copy(
                activeTool = null,
                toolHistory = toolHistory.map {
                    if (it.id == event.toolId) it.copy(status = ToolStatus.Completed, outputPreview = event.output) else it
                }
            )
            else -> this
        }
        is AgentRunEvent.ToolFailed -> when (this) {
            is AgentRunState.Running -> copy(
                activeTool = null,
                toolHistory = toolHistory.map {
                    if (it.id == event.toolId) it.copy(status = ToolStatus.Failed, outputPreview = event.output) else it
                }
            )
            else -> this
        }
        is AgentRunEvent.ApprovalRequired -> when (this) {
            is AgentRunState.Running -> AgentRunState.AwaitingApproval(runId, event.request, assistantText, toolHistory)
            else -> this
        }
        is AgentRunEvent.ClarificationRequired -> when (this) {
            is AgentRunState.Running -> AgentRunState.AwaitingClarification(runId, event.request, assistantText, toolHistory)
            else -> this
        }
        AgentRunEvent.Completed -> when (this) {
            is AgentRunState.Running -> AgentRunState.Completed(runId, assistantText, toolHistory)
            is AgentRunState.AwaitingApproval -> AgentRunState.Completed(runId, assistantText, toolHistory)
            is AgentRunState.AwaitingClarification -> AgentRunState.Completed(runId, assistantText, toolHistory)
            else -> this
        }
        AgentRunEvent.Cancelled -> when (this) {
            is AgentRunState.Running -> AgentRunState.Cancelled(runId, assistantText)
            is AgentRunState.AwaitingApproval -> AgentRunState.Cancelled(runId, assistantText)
            is AgentRunState.AwaitingClarification -> AgentRunState.Cancelled(runId, assistantText)
            else -> AgentRunState.Cancelled(null)
        }
        is AgentRunEvent.Failed -> when (this) {
            is AgentRunState.Running -> AgentRunState.Failed(runId, event.message, event.recoverable, assistantText)
            is AgentRunState.AwaitingApproval -> AgentRunState.Failed(runId, event.message, event.recoverable, assistantText)
            is AgentRunState.AwaitingClarification -> AgentRunState.Failed(runId, event.message, event.recoverable, assistantText)
            else -> AgentRunState.Failed(null, event.message, event.recoverable)
        }
    }
}
