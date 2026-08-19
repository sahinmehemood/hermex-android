package com.hermex.domain.chat

import kotlinx.coroutines.flow.Flow

/**
 * Stable domain-facing gateway contract.
 * Concrete REST/WebSocket adapters can evolve with Hermes without forcing UI changes.
 */
interface AgentGateway {
    suspend fun submitPrompt(
        sessionId: String,
        prompt: String,
        attachments: List<AttachmentRef> = emptyList(),
        model: String? = null,
        profile: String? = null
    ): Flow<AgentRunEvent>

    suspend fun stopRun(sessionId: String)
    suspend fun steerRun(sessionId: String, instruction: String): Flow<AgentRunEvent>
    suspend fun respondToApproval(sessionId: String, requestId: String, approved: Boolean): Flow<AgentRunEvent>
    suspend fun respondToClarification(sessionId: String, requestId: String, answer: String): Flow<AgentRunEvent>
}

data class AttachmentRef(
    val id: String,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
    val localUri: String? = null,
    val remotePath: String? = null
)
